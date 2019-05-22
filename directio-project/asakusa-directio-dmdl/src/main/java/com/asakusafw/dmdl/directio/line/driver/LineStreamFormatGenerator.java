/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.line.driver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.hadoop.io.compress.CompressionCodec;

import com.asakusafw.dmdl.directio.util.CharsetUtil;
import com.asakusafw.dmdl.directio.util.CodecNames;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.io.line.LineConfiguration;
import com.asakusafw.runtime.io.line.LineInput;
import com.asakusafw.runtime.io.line.LineOutput;
import com.asakusafw.runtime.io.line.directio.AbstractLineStreamFormat;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.io.util.InputSplitters;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.UnaryOperator;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Generates an implementation of {@link AbstractLineStreamFormat} class.
 * @since 0.10.3
 */
public class LineStreamFormatGenerator {

    private final EmitContext context;

    private final ModelDeclaration model;

    private final LineFormatTrait.Configuration conf;

    private final ModelFactory f;

    /**
     * creates a new instance.
     * @param context the current context
     * @param model the target model
     * @param configuration the format configuration
     */
    public LineStreamFormatGenerator(
            EmitContext context,
            ModelDeclaration model,
            LineFormatTrait.Configuration configuration) {
        assert context != null;
        assert model != null;
        assert configuration != null;
        this.context = context;
        this.model = model;
        this.conf = configuration;
        this.f = context.getModelFactory();
    }

    /**
     * Emits an implementation of {@link AbstractLineStreamFormat} class as a Java compilation unit.
     * @throws IOException if I/O error was occurred while emitting the compilation unit
     */
    public void emit() throws IOException {
        ClassDeclaration decl = f.newClassDeclaration(
                new JavadocBuilder(f)
                    .inline("Line format for {0}.",
                            d -> d.linkType(context.resolve(model.getSymbol())))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                context.getTypeName(),
                f.newParameterizedType(
                        context.resolve(AbstractLineStreamFormat.class),
                        context.resolve(model.getSymbol())),
                Collections.emptyList(),
                createMembers());
        context.emit(decl);
    }

    private List<TypeBodyDeclaration> createMembers() {
        List<TypeBodyDeclaration> results = new ArrayList<>();
        results.add(createGetSupportedType());
        results.add(createGetConfiguration());
        results.add(createParse());
        results.add(createEmit());
        createGetInputSplitter().ifPresent(results::add);
        createGetCompressionCodecClass().ifPresent(results::add);
        return results;
    }

    private MethodDeclaration createGetSupportedType() {
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                f.newParameterizedType(
                        context.resolve(Class.class),
                        context.resolve(model.getSymbol())),
                f.newSimpleName("getSupportedType"), //$NON-NLS-1$
                Collections.emptyList(),
                Arrays.asList(new TypeBuilder(f, context.resolve(model.getSymbol()))
                    .dotClass()
                    .toReturnStatement()));
    }

    private MethodDeclaration createGetConfiguration() {
        ExpressionBuilder builder = new TypeBuilder(f, context.resolve(LineConfiguration.class))
            .newObject()
            .method("withCharset", new TypeBuilder(f, context.resolve(Charset.class)) //$NON-NLS-1$
                .method("forName", Models.toLiteral(f, conf.getCharsetName())) //$NON-NLS-1$
                .toExpression());
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("Returns this line format configuration.") //$NON-NLS-1$
                    .returns()
                        .text("line format configuration") //$NON-NLS-1$
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                context.resolve(LineConfiguration.class),
                f.newSimpleName("getConfiguration"),  //$NON-NLS-1$
                Collections.emptyList(),
                Arrays.asList(builder.toReturnStatement()));
    }

    private MethodDeclaration createParse() {
        SimpleName input = f.newSimpleName("input");
        SimpleName object = f.newSimpleName("model");
        SimpleName path = f.newSimpleName("path");
        List<Statement> statements = new ArrayList<>();

        PropertyDeclaration body = model.getDeclaredProperties().stream()
            .filter(it -> LineFieldTrait.getKind(it) == LineFieldTrait.Kind.BODY)
            .findFirst()
            .get();
        statements.add(f.newIfStatement(
                new ExpressionBuilder(f, input)
                    .method("readTo", new ExpressionBuilder(f, object)
                            .method(context.getOptionGetterName(body))
                            .toExpression())
                    .apply(UnaryOperator.NOT)
                    .toExpression(),
                f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false)).toReturnStatement())));

        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            switch (LineFieldTrait.getKind(property)) {
            case FILE_NAME:
                statements.add(new ExpressionBuilder(f, object)
                    .method(context.getOptionSetterName(property), path)
                    .toStatement());
                break;

            case LINE_NUMBER:
                statements.add(new ExpressionBuilder(f, object)
                    .method(context.getValueSetterName(property),
                            castIfInt(property.getType(), new ExpressionBuilder(f, input)
                                .method("getLineNumber") //$NON-NLS-1$
                                .toExpression()))
                    .toStatement());
                break;

            default:
                break;
            }
        }
        statements.add(new ExpressionBuilder(f, Models.toLiteral(f, true)).toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                Collections.emptyList(),
                context.resolve(boolean.class),
                f.newSimpleName("parse"),
                Arrays.asList(
                        f.newFormalParameterDeclaration(context.resolve(LineInput.class), input),
                        f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object),
                        f.newFormalParameterDeclaration(context.resolve(StringOption.class), path)),
                0,
                Arrays.asList(context.resolve(IOException.class)),
                f.newBlock(statements));
    }

    private TypeBodyDeclaration createEmit() {
        SimpleName output = f.newSimpleName("output");
        SimpleName object = f.newSimpleName("model");
        SimpleName path = f.newSimpleName("path");
        List<Statement> statements = new ArrayList<>();

        PropertyDeclaration body = model.getDeclaredProperties().stream()
            .filter(it -> LineFieldTrait.getKind(it) == LineFieldTrait.Kind.BODY)
            .findFirst()
            .get();
        statements.add(new ExpressionBuilder(f, output)
                .method("write", new ExpressionBuilder(f, object)
                            .method(context.getOptionGetterName(body))
                            .toExpression())
                .toStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Protected()
                    .toAttributes(),
                Collections.emptyList(),
                context.resolve(void.class),
                f.newSimpleName("emit"),
                Arrays.asList(
                        f.newFormalParameterDeclaration(context.resolve(LineOutput.class), output),
                        f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object),
                        f.newFormalParameterDeclaration(context.resolve(StringOption.class), path)),
                0,
                Arrays.asList(context.resolve(IOException.class)),
                f.newBlock(statements));
    }

    private Optional<MethodDeclaration> createGetInputSplitter() {
        if (isSplittable()) {
            return Optional.of(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Protected()
                        .toAttributes(),
                    context.resolve(InputSplitter.class),
                    f.newSimpleName("getInputSplitter"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Arrays.asList(new TypeBuilder(f, context.resolve(InputSplitters.class))
                            .method("byLineFeed") //$NON-NLS-1$
                            .toReturnStatement())));
        } else {
            return Optional.empty();
        }
    }

    private boolean isSplittable() {
        if (conf.getCodecName() != null) {
            return false;
        }
        if (conf.getCharsetName() != null) {
            Charset cs = Charset.forName(conf.getCharsetName());
            if (!CharsetUtil.isAsciiCompatible(cs)) {
                return false;
            }
        }
        if (model.getDeclaredProperties().stream()
                .map(LineFieldTrait::getKind)
                .anyMatch(it -> it == LineFieldTrait.Kind.LINE_NUMBER)) {
            return false;
        }
        return true;
    }

    private Optional<MethodDeclaration> createGetCompressionCodecClass() {
        String codecClass = CodecNames.resolveCodecName(conf.getCodecName());
        if (codecClass != null) {
            return Optional.of(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Protected()
                        .toAttributes(),
                    new TypeBuilder(f, context.resolve(Class.class))
                        .parameterize(f.newWildcardExtends(context.resolve(CompressionCodec.class)))
                        .toType(),
                    f.newSimpleName("getCompressionCodecClass"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Arrays.asList(new TypeBuilder(f, context.resolve(Models.toName(f, codecClass)))
                            .dotClass()
                            .toReturnStatement())));
        } else {
            return Optional.empty();
        }
    }

    private Expression castIfInt(Type type, Expression expression) {
        assert type instanceof BasicType;
        BasicTypeKind kind = ((BasicType) type).getKind();
        assert kind == BasicTypeKind.INT || kind == BasicTypeKind.LONG;
        if (kind == BasicTypeKind.LONG) {
            return expression;
        } else {
            return new ExpressionBuilder(f, expression).castTo(context.resolve(int.class)).toExpression();
        }
    }
}