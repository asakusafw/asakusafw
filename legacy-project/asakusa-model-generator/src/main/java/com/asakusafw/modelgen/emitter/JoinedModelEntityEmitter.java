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

import com.asakusafw.modelgen.model.JoinedModelDescription;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.ModelReference;
import com.asakusafw.modelgen.model.Source;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.JoinedModel;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.ModelRef;
import com.asakusafw.vocabulary.model.Property;

/**
 * {@link JoinedModelDescription}に対するエンティティ情報をファイル上に出力するエミッタ。
 */
public class JoinedModelEntityEmitter extends ModelEntityEmitter<JoinedModelDescription> {

    /**
     * インスタンスを生成する。
     * @param factory ソースコードを生成するファクトリ
     * @param output 出力先のベースディレクトリ
     * @param packageName 出力先のパッケージ名
     * @param headerComment ファイルのヘッダコメント、不要の場合は{@code null}
     */
    public JoinedModelEntityEmitter(
            ModelFactory factory,
            File output,
            String packageName,
            List<String> headerComment) {
        super(factory, output, packageName, headerComment);
    }

    @Override
    public Class<JoinedModelDescription> getEmitTargetType() {
        return JoinedModelDescription.class;
    }

    @Override
    protected List<Annotation> createAnnotationsForModel(
            JoinedModelDescription model) {
        return new AttributeBuilder(f)
            .annotation(bless(DataModel.class))
            .annotation(bless(JoinedModel.class),
                    "from",
                    createModelRefAnnotation(
                            model.getFromModel(),
                            model.getFromCondition()),
                    "join",
                    createModelRefAnnotation(
                            model.getJoinModel(),
                            model.getJoinCondition()))
            .annotation(bless(SuppressWarnings.class), Models.toLiteral(f, "deprecation"))
            .toAnnotations();
    }

    @Override
    protected List<Annotation> createAnnotationsForField(ModelProperty property) {
        List<AnnotationElement> elements = new ArrayList<AnnotationElement>();
        if (property.getFrom() != null) {
            elements.add(f.newAnnotationElement(
                    f.newSimpleName("from"),
                    createSourceAnnotation(property.getFrom())));
        }
        if (property.getJoined() != null) {
            elements.add(f.newAnnotationElement(
                    f.newSimpleName("join"),
                    createSourceAnnotation(property.getJoined())));
        }
        Annotation prop = f.newNormalAnnotation(
                (NamedType) bless(Property.class),
                elements);
        return Collections.singletonList(prop);
    }

    private Annotation createModelRefAnnotation(
            ModelReference reference,
            List<Source> joinCondition) {
        List<String> groupKeys = new ArrayList<String>();
        for (Source s : joinCondition) {
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
    protected List<TypeBodyDeclaration> createMembers(
            JoinedModelDescription model) {
        List<TypeBodyDeclaration> members = super.createMembers(model);
        members.add(createJoiner(model));
        members.add(createSplitter(model));
        return members;
    }

    private TypeBodyDeclaration createJoiner(JoinedModelDescription model) {
        SimpleName left = common.getVariableNameOf(model, "left");
        SimpleName right = common.getVariableNameOf(model, "right");
        List<Statement> statements = new ArrayList<Statement>();
        for (ModelProperty property : model.getProperties()) {
            statements.add(createJoinerFor(model, property, left, right));
        }
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("2つのモデルオブジェクトを結合した結果を、このオブジェクトに設定する。")
                    .param(left)
                        .text("結合されるモデルのオブジェクト")
                    .param(right)
                        .text("結合するモデルのオブジェクト")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                Models.toType(f, void.class),
                common.getJoinerName(),
                Arrays.asList(new FormalParameterDeclaration[] {
                        f.newFormalParameterDeclaration(
                                bless(common.getModelType(model.getFromModel())),
                                left),
                        f.newFormalParameterDeclaration(
                                bless(common.getModelType(model.getJoinModel())),
                                right),
                }),
                statements);
    }

    private Statement createJoinerFor(
            JoinedModelDescription model,
            ModelProperty property,
            SimpleName left,
            SimpleName right) {
        if (property.getFrom() != null) {
            return createImporterFor(
                    left,
                    property.getFrom().getName(),
                    property.getName(),
                    property.getType());
        } else {
            return createImporterFor(
                    right,
                    property.getJoined().getName(),
                    property.getName(),
                    property.getType());
        }
    }

    private TypeBodyDeclaration createSplitter(JoinedModelDescription model) {
        SimpleName left = common.getVariableNameOf(model, "left");
        SimpleName right = common.getVariableNameOf(model, "right");
        List<Statement> statements = new ArrayList<Statement>();
        for (ModelProperty property : model.getProperties()) {
            if (property.getFrom() != null) {
                statements.add(createSplitterFor(model, property, property.getFrom(), left));
            }
            if (property.getJoined() != null) {
                statements.add(createSplitterFor(model, property, property.getJoined(), right));
            }
        }
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("この結合されたモデルを、もとの2つのモデルに分解して書き出す。")
                    .param(left)
                        .text("結合されるモデルのオブジェクト")
                    .param(right)
                        .text("結合するモデルのオブジェクト")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                Models.toType(f, void.class),
                common.getSplitterName(),
                Arrays.asList(new FormalParameterDeclaration[] {
                        f.newFormalParameterDeclaration(
                                bless(common.getModelType(model.getFromModel())),
                                left),
                        f.newFormalParameterDeclaration(
                                bless(common.getModelType(model.getJoinModel())),
                                right),
                }),
                statements);
    }

    private Statement createSplitterFor(
            JoinedModelDescription model,
            ModelProperty property,
            Source original,
            SimpleName param) {
        return createExporterFor(
                property.getName(),
                param,
                original.getName(),
                property.getType());
    }

    @Override
    protected Javadoc createJavadocForModel(JoinedModelDescription model) {
        List<Source> left = model.getFromCondition();
        List<Source> right = model.getJoinCondition();
        assert left.size() == right.size();

        StringBuilder joinCond = new StringBuilder();
        joinCond.append("<ul>");
        for (int i = 0, n = left.size(); i < n; i++) {
            Source a = left.get(i);
            Source b = right.get(i);
            joinCond.append(MessageFormat.format(
                    "<li><code>{0}.{1} == {2}.{3}</code></li>",
                    a.getDeclaring().getSimpleName(),
                    a.getName(),
                    b.getDeclaring().getSimpleName(),
                    b.getName()));
        }
        joinCond.append("</ul>");
        return new JavadocBuilder(f)
            .text("テーブル<code>{0}, {1}</code>を結合した結果のモデルクラス。",
                    model.getFromModel().getSimpleName(),
                    model.getJoinModel().getSimpleName())
            .text("<p>以下のように結合されている:</p>")
            .text(joinCond.toString())
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
        if (property.getFrom() == null || property.getJoined() == null) {
            Source first = property.getSource();
            return MessageFormat.format(
                    "カラム<code>{0}.{1}</code>の内容",
                    first.getDeclaring().getSimpleName(),
                    first.getName());
        } else {
            Source first = property.getFrom();
            Source second = property.getJoined();
            return MessageFormat.format(
                    "カラム<code>{0}.{1}</code>および<code>{2}.{3}</code>の内容",
                    first.getDeclaring().getSimpleName(),
                    first.getName(),
                    second.getDeclaring().getSimpleName(),
                    second.getName());
        }
    }
}
