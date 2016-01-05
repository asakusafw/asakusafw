/**
 * Copyright 2011-2016 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.modelgen.emitter;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.modelgen.Constants;
import com.asakusafw.modelgen.model.Aggregator;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.ModelReference;
import com.asakusafw.modelgen.model.Source;
import com.asakusafw.modelgen.model.SummarizedModelDescription;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.ModelRef;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.SummarizedModel;

/**
 * {@link SummarizedModelDescription}に対するエンティティ情報をファイル上に出力するエミッタ。
 */
public class SummarizedModelEntityEmitter
        extends ModelEntityEmitter<SummarizedModelDescription> {

    /**
     * インスタンスを生成する。
     * @param factory ソースコードを生成するファクトリ
     * @param output 出力先のベースディレクトリ
     * @param packageName 出力先のパッケージ名
     * @param headerComment ファイルのヘッダコメント、不要の場合は{@code null}
     */
    public SummarizedModelEntityEmitter(
            ModelFactory factory,
            File output,
            String packageName,
            List<String> headerComment) {
        super(factory, output, packageName, headerComment);
    }

    @Override
    public Class<SummarizedModelDescription> getEmitTargetType() {
        return SummarizedModelDescription.class;
    }

    @Override
    protected List<TypeBodyDeclaration> createMembers(
            SummarizedModelDescription model) {
        List<TypeBodyDeclaration> members = super.createMembers(model);
        members.add(createStartSummarize(model));
        members.add(createProcessSummarize(model));
        return members;
    }

    private TypeBodyDeclaration createStartSummarize(
            SummarizedModelDescription model) {
        SimpleName param = common.getVariableNameOf(model, "original");
        List<Statement> statements = new ArrayList<Statement>();
        for (ModelProperty property : model.getProperties()) {
            statements.add(createStartSummarizeFor(param, property));
        }
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("指定のモデルを最初の要素として、このモデルの集計結果を初期化する。")
                    .param(param)
                        .text("最初の要素となるモデル")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                Models.toType(f, void.class),
                common.getStartSummarizerName(),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        bless(common.getModelType(model.getOriginalModel())),
                        param)),
                statements);
    }

    private Statement createStartSummarizeFor(
            SimpleName parameterName,
            ModelProperty property) {
        Source source = property.getFrom();
        Expression to = f.newFieldAccessExpression(
                f.newThis(),
                common.getFieldNameOf(property.getName(), property.getType()));
        switch (source.getAggregator()) {
        case IDENT:
        case MAX:
        case MIN:
        case SUM:
            return new ExpressionBuilder(f, to)
                .method(Constants.NAME_OPTION_MODIFIER,
                        new ExpressionBuilder(f, parameterName)
                            .method(common.getGetterNameOf(source.getName(), source.getType()))
                            .toExpression())
                .toStatement();
        case COUNT:
            return new ExpressionBuilder(f, to)
                .method(Constants.NAME_OPTION_MODIFIER, Models.toLiteral(f, 1))
                .toStatement();
        default:
            throw new AssertionError();
        }
    }

    private TypeBodyDeclaration createProcessSummarize(
            SummarizedModelDescription model) {
        SimpleName param = common.getVariableNameOf(model, "original");
        List<Statement> statements = new ArrayList<Statement>();
        for (ModelProperty property : model.getProperties()) {
            // IDENTは一度だけ
            if (property.getFrom().getAggregator() == Aggregator.IDENT) {
                continue;
            }
            statements.add(createAddSummarizeFor(param.getToken(), property));
        }
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("このモデルに、指定のモデルの集計結果を合成する。")
                    .param(param)
                        .text("合成するモデル")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                Models.toType(f, void.class),
                common.getCombineSummarizerName(),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        bless(common.getModelType(model.getReference())),
                        param)),
                statements);
    }

    private Statement createAddSummarizeFor(
            String parameterName,
            ModelProperty property) {
        Source source = property.getFrom();
        Expression self = f.newFieldAccessExpression(
                f.newThis(),
                common.getFieldNameOf(property.getName(), property.getType()));
        Expression other = f.newFieldAccessExpression(
                f.newSimpleName(parameterName),
                common.getFieldNameOf(property.getName(), property.getType()));
        Aggregator aggregator = source.getAggregator();
        switch (aggregator) {
        case MAX:
        case MIN:
            return new ExpressionBuilder(f, self)
                .method(aggregator.name().toLowerCase(), other)
                .toStatement();
        case SUM:
        case COUNT:
            return new ExpressionBuilder(f, self)
                .method(Constants.NAME_OPTION_ADDER, other)
                .toStatement();
        default:
            throw new AssertionError();
        }
    }

    @Override
    protected List<Annotation> createAnnotationsForModel(
            SummarizedModelDescription model) {
        return new AttributeBuilder(f)
            .annotation(bless(DataModel.class))
            .annotation(bless(SummarizedModel.class),
                "from",
                createModelRefAnnotation(
                        model.getOriginalModel(),
                        model.getGroupBy()))
            .annotation(bless(SuppressWarnings.class), Models.toLiteral(f, "deprecation"))
            .toAnnotations();
    }

    @Override
    protected List<Annotation> createAnnotationsForField(ModelProperty property) {
        return new AttributeBuilder(f)
            .annotation(bless(Property.class),
                    "from", createSourceAnnotation(property.getFrom()),
                    "aggregator", Models.append(
                            f,
                            ((NamedType) bless(Property.Aggregator.class)).getName(),
                            property.getFrom().getAggregator().name()))
            .toAnnotations();
    }

    private Annotation createModelRefAnnotation(
            ModelReference reference,
            List<Source> groupSources) {
        List<String> groupKeys = new ArrayList<String>();
        for (Source s : groupSources) {
            groupKeys.add(common.getFieldNameOf(
                    s.getName(),
                    s.getType()).getToken());
        }
        String[] group = groupKeys.toArray(new String[groupKeys.size()]);
        Annotation key = f.newNormalAnnotation(
                (NamedType) bless(Key.class),
                Collections.singletonList(f.newAnnotationElement(
                        f.newSimpleName("group"),
                        Models.toArrayInitializer(f, group))));
        return f.newNormalAnnotation(
                (NamedType) bless(ModelRef.class),
                Arrays.asList(new AnnotationElement[] {
                        f.newAnnotationElement(
                                f.newSimpleName("type"),
                                f.newClassLiteral(bless(
                                        common.getModelType(reference)))),
                        f.newAnnotationElement(
                                f.newSimpleName("key"),
                                key),
                }));
    }

    private Annotation createSourceAnnotation(Source source) {
        AnnotationElement declaring = f.newAnnotationElement(
                f.newSimpleName("declaring"),
                f.newClassLiteral(bless(common.getModelType(source.getDeclaring()))));
        AnnotationElement name = f.newAnnotationElement(
                f.newSimpleName("name"),
                Models.toLiteral(f, common.getFieldNameOf(
                        source.getName(),
                        source.getType()).getToken()));
        return f.newNormalAnnotation(
                (NamedType) bless(Property.Source.class),
                Arrays.asList(declaring, name));
    }

    @Override
    protected Javadoc createJavadocForModel(SummarizedModelDescription model) {
        List<String> groupByNames = new ArrayList<String>();
        for (Source source : model.getGroupBy()) {
            groupByNames.add(source.getName());
        }
        String groupByComment;
        if (groupByNames.isEmpty()) {
            groupByComment = "<p>グループ化は指定されておらず、全てのレコードが同一グループになる。</p>";
        } else {
            groupByComment = MessageFormat.format(
                    "<p>グループ化はそれぞれ <code>{0}</code> で行っている。</p>",
                    groupByNames);
        }
        return new JavadocBuilder(f)
            .text("テーブル<code>{0}</code>を集計した結果のモデルクラス。",
                    model.getReference().getSimpleName())
            .text(groupByComment)
            .toJavadoc();
    }

    @Override
    protected Javadoc createJavadocForField(ModelProperty property) {
        return new JavadocBuilder(f)
            .text("{0}のフィールド。", getColumnDescription(property))
            .toJavadoc();
    }

    @Override
    protected Javadoc createJavadocForGetter(ModelProperty property) {
        return new JavadocBuilder(f)
            .text("{0}を返す。", getColumnDescription(property))
            .returns()
                .text(getColumnDescription(property))
            .exception(bless(NullPointerException.class))
                .text("値に")
                .code("null")
                .text("が格納されていた場合")
            .toJavadoc();
    }

    @Override
    protected Javadoc createJavadocForSetter(ModelProperty property) {
        return new JavadocBuilder(f)
            .text("{0}を変更する。", getColumnDescription(property))
            .param(createNameForParameter(property))
                .text("設定する値")
            .toJavadoc();
    }

    private String getColumnDescription(ModelProperty property) {
        Source source = property.getFrom();
        if (source.getAggregator() == Aggregator.IDENT) {
            return MessageFormat.format(
                    "グループ化したカラム<code>{0}</code>の内容",
                    source.getName());
        } else {
            return MessageFormat.format(
                    "カラム<code>{0}</code>を<code>{1}()</code>で集約した結果",
                    source.getName(),
                    source.getAggregator());
        }
    }
}
