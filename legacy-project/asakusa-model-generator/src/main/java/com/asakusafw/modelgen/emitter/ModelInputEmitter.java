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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import com.asakusafw.modelgen.Constants;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * TSVを読み出すプログラムを生成する。
 */
public class ModelInputEmitter extends BaseEmitter<ModelDescription> {

    /**
     * インスタンスを生成する。
     * @param factory ソースコードを生成するファクトリ
     * @param output 出力先のベースディレクトリ
     * @param packageName 出力先のパッケージ名
     * @param headerComment ファイルのヘッダコメント、不要の場合は{@code null}
     */
    public ModelInputEmitter(
            ModelFactory factory,
            File output,
            String packageName,
            List<String> headerComment) {
        super(factory, output, packageName, headerComment);
    }

    @Override
    protected PackageDeclaration createPackageDeclaration(ModelDescription model) {
        return f.newPackageDeclaration(
                common.getPackageNameOf(
                        model.getReference(),
                        Constants.CATEGORY_IO));
    }

    @Override
    protected TypeDeclaration createTypeDeclaration(ModelDescription model) {
        SimpleName name = createTypeName(model);
        return f.newClassDeclaration(
                new JavadocBuilder(f)
                    .text("TSVファイルなどのレコードを表すファイルを入力として")
                    .linkType(createModelType(model))
                    .text("を読み出す。")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .annotation(
                            bless(Generated.class),
                            Models.toLiteral(f, MessageFormat.format("{0}:{1}",
                                    getClass().getSimpleName(),
                                    Constants.VERSION)))
                    .annotation(bless(SuppressWarnings.class),
                            Models.toLiteral(f, "deprecation"))
                    .Public()
                    .Final()
                    .toAttributes(),
                name,
                Collections.<TypeParameterDeclaration>emptyList(),
                null,
                Collections.singletonList(f.newParameterizedType(
                        bless(ModelInput.class),
                        createModelType(model))),
                createBodyDeclarations(model));
    }

    private List<TypeBodyDeclaration> createBodyDeclarations(ModelDescription model) {
        List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
        results.add(createParserField(model));
        results.add(createConstructor(model));
        results.add(createReader(model));
        results.add(createCloser(model));
        return results;
    }

    private TypeBodyDeclaration createParserField(ModelDescription model) {
        return f.newFieldDeclaration(
                new JavadocBuilder(f)
                    .text("内部で利用するパーサー")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Private()
                    .Final()
                    .toAttributes(),
                createParserType(),
                createParserFieldName(),
                null);
    }

    private TypeBodyDeclaration createConstructor(ModelDescription model) {
        return f.newConstructorDeclaration(
                new JavadocBuilder(f)
                    .text("インスタンスを生成する。")
                    .param(createParserFieldName())
                        .text("利用するパーサー")
                    .exception(createInvalidArgumentExceptionType())
                        .text("引数にnullが指定された場合")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                createTypeName(model),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        createParserType(),
                        createParserFieldName())),
                createConstructorBody(model));
    }

    private List<Statement> createConstructorBody(ModelDescription model) {
        List<Statement> results = new ArrayList<Statement>();

        results.add(f.newIfStatement(
                new ExpressionBuilder(f, createParserFieldName())
                    .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                    .toExpression(),
                f.newBlock(new TypeBuilder(f, createInvalidArgumentExceptionType())
                    .newObject()
                    .toThrowStatement())));

        results.add(new ExpressionBuilder(f, f.newThis(null))
            .field(createParserFieldName())
            .assignFrom(createParserFieldName())
            .toStatement());

        return results;
    }

    private TypeBodyDeclaration createReader(ModelDescription model) {
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(bless(Override.class))
                    .Public()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                bless(boolean.class),
                f.newSimpleName("readTo"),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        createModelType(model),
                        createModelParameterName())),
                0,
                Collections.singletonList(bless(IOException.class)),
                f.newBlock(createReaderBody(model)));
    }

    private List<Statement> createReaderBody(ModelDescription model) {
        List<Statement> results = new ArrayList<Statement>();

        results.add(f.newIfStatement(
                new ExpressionBuilder(f, createParserFieldName())
                    .method("next")
                    .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                    .toExpression(),
                f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                    .toReturnStatement())));

        for (ModelProperty property : model.getProperties()) {
            results.add(createReaderStatement(property));
        }
        results.add(f.newReturnStatement(Models.toLiteral(f, true)));
        return results;
    }

    private Statement createReaderStatement(ModelProperty property) {
        SimpleName optionGetterName = common.getOptionGetterNameOf(
                property.getName(),
                property.getType());

        Expression option = new ExpressionBuilder(f, createModelParameterName())
            .method(optionGetterName)
            .toExpression();

        Statement fill = new ExpressionBuilder(f, createParserFieldName())
            .method("fill", option)
            .toStatement();

        return fill;
    }

    private TypeBodyDeclaration createCloser(ModelDescription model) {
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(bless(Override.class))
                    .Public()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                bless(void.class),
                f.newSimpleName("close"),
                Collections.<FormalParameterDeclaration>emptyList(),
                0,
                Collections.singletonList(bless(IOException.class)),
                f.newBlock(createCloserBody()));
    }

    private List<Statement> createCloserBody() {
        List<Statement> results = new ArrayList<Statement>();
        results.add(new ExpressionBuilder(f, createParserFieldName())
            .method("close")
            .toStatement());
        return results;
    }

    private SimpleName createTypeName(ModelDescription model) {
        SimpleName original = common.getTypeNameOf(model.getReference());
        SimpleName name = f.newSimpleName(MessageFormat.format(
                Constants.FORMAT_NAME_MODEL_INPUT,
                original.getToken()));
        return name;
    }

    private Type createModelType(ModelDescription model) {
        return bless(common.getModelType(model.getReference()));
    }

    private Type createParserType() {
        return bless(RecordParser.class);
    }

    private SimpleName createParserFieldName() {
        return f.newSimpleName("parser");
    }

    private SimpleName createModelParameterName() {
        return f.newSimpleName("model");
    }

    private Type createInvalidArgumentExceptionType() {
        return bless(IllegalArgumentException.class);
    }
}
