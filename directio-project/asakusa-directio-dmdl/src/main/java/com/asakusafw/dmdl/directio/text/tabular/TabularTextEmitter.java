/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.text.tabular;

import static com.asakusafw.dmdl.directio.text.TextFormatConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.text.AbstractTextStreamFormatGenerator;
import com.asakusafw.dmdl.directio.text.EscapeSettings;
import com.asakusafw.dmdl.directio.text.TextFormatSettings;
import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.MapValue;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.runtime.io.text.tabular.EscapeSequence;
import com.asakusafw.runtime.io.text.tabular.TabularTextFormat;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits Direct I/O data format classes about tabular text.
 * @since 0.9.1
 */
public class TabularTextEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(TabularTextEmitter.class);

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (TabularTextTrait.find(model).isPresent() == false) {
            return;
        }
        Name supportName = generateFormat(context, model);
        generateInputDescription(context, supportName, model);
        generateOutputDescription(context, supportName, model);
    }

    private static Name generateFormat(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        TabularTextTrait trait = TabularTextTrait.get(model);
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                PACKAGE_SEGMENT,
                "{0}TabularTextFormat"); //$NON-NLS-1$
        LOG.debug("Generating tabular text format for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        FormatGenerator.emit(next, model, trait);
        LOG.debug("Generated tabular text format for {}: {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private static void generateInputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                PACKAGE_SEGMENT,
                "Abstract{0}TabularTextInputDescription"); //$NON-NLS-1$
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "Tabular text file input", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileInputDescriptionGenerator.generate(next, desc);
    }

    private static void generateOutputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                PACKAGE_SEGMENT,
                "Abstract{0}TabularTextOutputDescription"); //$NON-NLS-1$
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "Tabular text file output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
    }

    private static final class FormatGenerator extends AbstractTextStreamFormatGenerator {

        private final TabularTextTrait root;

        private final ModelFactory f;

        private FormatGenerator(EmitContext context, ModelDeclaration model, TabularTextTrait root) {
            super(context, model, root.getFormatSettings(), root.getFieldSettings());
            this.root = root;
            this.f = context.getModelFactory();
        }

        static void emit(EmitContext context, ModelDeclaration model, TabularTextTrait trait) throws IOException {
            new FormatGenerator(context, model, trait).emit(Messages.getString("TabularTextEmitter.javadocTitle")); //$NON-NLS-1$
        }

        @Override
        protected List<Statement> createGetTextFormatInternal() {
            SimpleName builder = f.newSimpleName("builder"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(f, context.resolve(TabularTextFormat.class))
                    .method("builder") //$NON-NLS-1$
                    .toLocalVariableDeclaration(context.resolve(TabularTextFormat.Builder.class), builder));
            buildTextFormat(statements, builder);
            statements.add(new ExpressionBuilder(f, builder)
                    .method("build") //$NON-NLS-1$
                    .toReturnStatement());
            return statements;
        }

        private void buildTextFormat(List<Statement> statements, SimpleName builder) {
            TextFormatSettings formats = root.getFormatSettings();
            EscapeSettings escapes = root.getEscapeSettings();

            formats.getCharset().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withCharset", resolve(v.name())) //$NON-NLS-1$
                    .toStatement()));
            formats.getLineSeparator().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withLineSeparator", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
            formats.getFieldSeparator().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withFieldSeparator", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
            escapes.getCharacter().ifPresent(c -> {
                ExpressionBuilder escapeBuilder = new TypeBuilder(f, context.resolve(EscapeSequence.class))
                        .method("builder", resolve(c)); //$NON-NLS-1$
                escapes.getEscapeLineSeparator().ifPresent(v -> {
                    if (v) {
                        escapeBuilder.method("addLineSeparator"); //$NON-NLS-1$
                    }
                });
                for (MapValue.Entry<Character, Character> entry : escapes.getSequences()) {
                    Character key = entry.getKey();
                    Character value = entry.getValue();
                    if (value == null) {
                        escapeBuilder.method("addNullMapping", resolve(key)); //$NON-NLS-1$
                    } else {
                        escapeBuilder.method("addMapping", resolve(key), resolve(value)); //$NON-NLS-1$
                    }
                }
                statements.add(new ExpressionBuilder(f, builder)
                        .method("withEscapeSequence", escapeBuilder //$NON-NLS-1$
                                .method("build") //$NON-NLS-1$
                                .toExpression())
                        .toStatement());
            });
            formats.getInputTransformerClass().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withInputTransformer", new TypeBuilder(f, resolve(v)) //$NON-NLS-1$
                            .constructorReference()
                            .toExpression())
                    .toStatement()));
            formats.getOutputTransformerClass().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withOutputTransformer", new TypeBuilder(f, resolve(v)) //$NON-NLS-1$
                            .constructorReference()
                            .toExpression())
                    .toStatement()));
        }

        @Override
        protected boolean isSplittableInternal() {
            return root.getEscapeSettings().getEscapeLineSeparator().orElse(false) == false;
        }
    }
}
