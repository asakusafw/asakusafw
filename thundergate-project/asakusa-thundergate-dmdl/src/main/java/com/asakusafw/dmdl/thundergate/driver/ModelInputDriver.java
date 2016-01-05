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
package com.asakusafw.dmdl.thundergate.driver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.emitter.NameConstants;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Generates {@link ModelInput} for each data model.
 */
public class ModelInputDriver extends JavaDataModelDriver {

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) throws IOException {
        Type type = generate(context, model);
        ModelFactory f = context.getModelFactory();
        return new AttributeBuilder(f)
            .annotation(context.resolve(ModelInputLocation.class),
                    f.newClassLiteral(context.resolve(type)))
            .toAnnotations();
    }

    private Type generate(EmitContext context, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                NameConstants.CATEGORY_IO,
                "{0}Input"); //$NON-NLS-1$
        Generator.emit(next, model);
        return context.resolve(next.getQualifiedTypeName());
    }

    @Override
    public List<Annotation> getMemberAnnotations(EmitContext context, PropertyDeclaration property) {
        return Collections.emptyList();
    }

    private static final class Generator {

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
                        .text("Provides a sequence of <code>{0}</code> using <code>RecordParser</code>.", //$NON-NLS-1$
                                model.getName())
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Final()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    null,
                    Collections.singletonList(f.newParameterizedType(
                            context.resolve(ModelInput.class),
                            context.resolve(model.getSymbol()))),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = Lists.create();
            results.add(createParserField());
            results.add(createConstructor());
            results.add(createReader());
            results.add(createCloser());
            return results;
        }


        private TypeBodyDeclaration createParserField() {
            return f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Final()
                        .toAttributes(),
                    context.resolve(RecordParser.class),
                    createParserFieldName(),
                    null);
        }

        private TypeBodyDeclaration createConstructor() {
            return f.newConstructorDeclaration(
                    new JavadocBuilder(f)
                        .text("Creates a new instance.") //$NON-NLS-1$
                        .param(createParserFieldName())
                            .text("the record parser") //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.singletonList(f.newFormalParameterDeclaration(
                            context.resolve(RecordParser.class),
                            createParserFieldName())),
                    createConstructorBody());
        }

        private List<Statement> createConstructorBody() {
            List<Statement> results = Lists.create();
            results.add(f.newIfStatement(
                    new ExpressionBuilder(f, createParserFieldName())
                        .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                        .toExpression(),
                    f.newBlock(new TypeBuilder(f, context.resolve(IllegalArgumentException.class))
                        .newObject(Models.toLiteral(f, createParserFieldName().getToken()))
                        .toThrowStatement())));
            results.add(new ExpressionBuilder(f, f.newThis(null))
                .field(createParserFieldName())
                .assignFrom(createParserFieldName())
                .toStatement());
            return results;
        }

        private MethodDeclaration createReader() {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(boolean.class),
                    f.newSimpleName("readTo"), //$NON-NLS-1$
                    Collections.singletonList(f.newFormalParameterDeclaration(
                            context.resolve(model.getSymbol()),
                            createModelParameterName())),
                    0,
                    Collections.singletonList(context.resolve(IOException.class)),
                    f.newBlock(createReaderBody()));
        }

        private List<Statement> createReaderBody() {
            List<Statement> results = Lists.create();

            results.add(f.newIfStatement(
                    new ExpressionBuilder(f, createParserFieldName())
                        .method("next") //$NON-NLS-1$
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                        .toReturnStatement())));

            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                results.add(createReaderStatement(property));
            }
            results.add(createEndRecordStatement());
            results.add(f.newReturnStatement(Models.toLiteral(f, true)));
            return results;
        }

        private Statement createReaderStatement(PropertyDeclaration property) {
            assert property != null;
            SimpleName optionGetterName = context.getOptionGetterName(property);
            Expression option = new ExpressionBuilder(f, createModelParameterName())
                .method(optionGetterName)
                .toExpression();
            Statement fill = new ExpressionBuilder(f, createParserFieldName())
                .method("fill", option) //$NON-NLS-1$
                .toStatement();
            return fill;
        }

        private Statement createEndRecordStatement() {
            return new ExpressionBuilder(f, createParserFieldName())
                .method("endRecord") //$NON-NLS-1$
                .toStatement();
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
                    f.newSimpleName("close"), //$NON-NLS-1$
                    Collections.<FormalParameterDeclaration>emptyList(),
                    0,
                    Collections.singletonList(context.resolve(IOException.class)),
                    f.newBlock(createCloserBody()));
        }

        private List<Statement> createCloserBody() {
            List<Statement> results = Lists.create();
            results.add(new ExpressionBuilder(f, createParserFieldName())
                .method("close") //$NON-NLS-1$
                .toStatement());
            return results;
        }

        private SimpleName createParserFieldName() {
            return f.newSimpleName("parser"); //$NON-NLS-1$
        }

        private SimpleName createModelParameterName() {
            return f.newSimpleName("model"); //$NON-NLS-1$
        }
    }
}
