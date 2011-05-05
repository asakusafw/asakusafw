/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.emitter.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.emitter.NameConstants;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.ashigeru.lang.java.model.syntax.Annotation;
import com.ashigeru.lang.java.model.syntax.ClassDeclaration;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.InfixOperator;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * Generates {@link ModelOutput} for each data model.
 */
public class ModelOutputDriver implements JavaDataModelDriver {

    @Override
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) {
        return Collections.emptyList();
    }

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
        return Collections.emptyList();
    }

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) throws IOException {
        Type type = generate(context, model);
        ModelFactory f = context.getModelFactory();
        return new AttributeBuilder(f)
            .annotation(context.resolve(ModelOutputLocation.class),
                    f.newClassLiteral(context.resolve(type)))
            .toAnnotations();
    }

    private Type generate(EmitContext context, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                NameConstants.CATEGORY_IO,
                "{0}Output");
        Generator.emit(next, model);
        return context.resolve(next.getQualifiedTypeName());
    }

    @Override
    public List<Annotation> getMemberAnnotations(EmitContext context, PropertyDeclaration property) {
        return Collections.emptyList();
    }

    private static class Generator {

        private final EmitContext context;

        private final ModelDeclaration model;

        private final ModelFactory f;

        private Generator(EmitContext context, ModelDeclaration model) {
            assert context != null;
            assert model != null;
            this.context = context;
            this.model = model;
            this.f = context.getModelFactory();
        }

        static void emit(EmitContext context, ModelDeclaration model) throws IOException {
            assert context != null;
            assert model != null;
            Generator emitter = new Generator(context, model);
            emitter.emit();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .text("<code>{0}</code>をTSVなどのレコード形式で出力する。",
                                model.getSymbol().getName())
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Final()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    null,
                    Collections.singletonList(f.newParameterizedType(
                            context.resolve(ModelOutput.class),
                            context.resolve(model.getSymbol()))),
                    createBodyDeclarations());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createBodyDeclarations() {
            List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
            results.add(createEmitterField());
            results.add(createConstructor());
            results.add(createWriter());
            results.add(createCloser());
            return results;
        }

        private TypeBodyDeclaration createEmitterField() {
            return f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Final()
                        .toAttributes(),
                        context.resolve(RecordEmitter.class),
                    createEmitterFieldName(),
                    null);
        }

        private TypeBodyDeclaration createConstructor() {
            return f.newConstructorDeclaration(
                    new JavadocBuilder(f)
                        .text("インスタンスを生成する。")
                        .param(createEmitterFieldName())
                            .text("利用するエミッター")
                        .exception(context.resolve(IllegalArgumentException.class))
                            .text("引数にnullが指定された場合")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.singletonList(f.newFormalParameterDeclaration(
                            context.resolve(RecordEmitter.class),
                            createEmitterFieldName())),
                    createConstructorBody());
        }

        private List<Statement> createConstructorBody() {
            List<Statement> results = new ArrayList<Statement>();
            results.add(f.newIfStatement(
                    new ExpressionBuilder(f, createEmitterFieldName())
                        .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                        .toExpression(),
                    f.newBlock(new TypeBuilder(f, context.resolve(IllegalArgumentException.class))
                        .newObject()
                        .toThrowStatement())));
            results.add(new ExpressionBuilder(f, f.newThis(null))
                .field(createEmitterFieldName())
                .assignFrom(createEmitterFieldName())
                .toStatement());
            return results;
        }

        private TypeBodyDeclaration createWriter() {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("write"),
                    Collections.singletonList(f.newFormalParameterDeclaration(
                            context.resolve(model.getSymbol()),
                            createModelParameterName())),
                    0,
                    Collections.singletonList(context.resolve(IOException.class)),
                    f.newBlock(createWriterBody()));
        }

        private List<Statement> createWriterBody() {
            List<Statement> results = new ArrayList<Statement>();
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                results.add(createWriterStatement(property));
            }
            results.add(new ExpressionBuilder(f, createEmitterFieldName())
                .method("endRecord")
                .toStatement());
            return results;
        }

        private Statement createWriterStatement(PropertyDeclaration property) {
            assert property != null;
            SimpleName optionGetterName = context.getOptionGetterName(property);
            Expression option = new ExpressionBuilder(f, createModelParameterName())
                .method(optionGetterName)
                .toExpression();
            Statement fill = new ExpressionBuilder(f, createEmitterFieldName())
                .method("emit", option)
                .toStatement();
            return fill;
        }

        private TypeBodyDeclaration createCloser() {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("close"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    0,
                    Collections.singletonList(context.resolve(IOException.class)),
                    f.newBlock(createCloserBody()));
        }

        private List<Statement> createCloserBody() {
            List<Statement> results = new ArrayList<Statement>();
            results.add(new ExpressionBuilder(f, createEmitterFieldName())
                .method("close")
                .toStatement());
            return results;
        }

        private SimpleName createEmitterFieldName() {
            return f.newSimpleName("emitter");
        }

        private SimpleName createModelParameterName() {
            return f.newSimpleName("model");
        }
    }
}
