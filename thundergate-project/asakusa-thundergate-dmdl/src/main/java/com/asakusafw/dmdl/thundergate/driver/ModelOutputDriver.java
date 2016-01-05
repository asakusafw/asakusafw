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
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
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
 * Generates {@link ModelOutput} for each data model.
 */
public class ModelOutputDriver extends JavaDataModelDriver {

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
                "{0}Output"); //$NON-NLS-1$
        Generator.emit(next, model);
        return context.resolve(next.getQualifiedTypeName());
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
                        .text("Outputs a sequence of <code>{0}</code> using <code>RecordEmitter</code>.", //$NON-NLS-1$
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
            List<TypeBodyDeclaration> results = Lists.create();
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
                        .text("Creates a new instance.") //$NON-NLS-1$
                        .param(createEmitterFieldName())
                            .text("the record emitter") //$NON-NLS-1$
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
            List<Statement> results = Lists.create();
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
                    f.newSimpleName("write"), //$NON-NLS-1$
                    Collections.singletonList(f.newFormalParameterDeclaration(
                            context.resolve(model.getSymbol()),
                            createModelParameterName())),
                    0,
                    Collections.singletonList(context.resolve(IOException.class)),
                    f.newBlock(createWriterBody()));
        }

        private List<Statement> createWriterBody() {
            List<Statement> results = Lists.create();
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                results.add(createWriterStatement(property));
            }
            results.add(new ExpressionBuilder(f, createEmitterFieldName())
                .method("endRecord") //$NON-NLS-1$
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
                .method("emit", option) //$NON-NLS-1$
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
                    f.newSimpleName("close"), //$NON-NLS-1$
                    Collections.<FormalParameterDeclaration>emptyList(),
                    0,
                    Collections.singletonList(context.resolve(IOException.class)),
                    f.newBlock(createCloserBody()));
        }

        private List<Statement> createCloserBody() {
            List<Statement> results = Lists.create();
            results.add(new ExpressionBuilder(f, createEmitterFieldName())
                .method("close") //$NON-NLS-1$
                .toStatement());
            return results;
        }

        private SimpleName createEmitterFieldName() {
            return f.newSimpleName("emitter"); //$NON-NLS-1$
        }

        private SimpleName createModelParameterName() {
            return f.newSimpleName("model"); //$NON-NLS-1$
        }
    }
}
