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
package com.asakusafw.dmdl.directio.text.csv;

import static com.asakusafw.dmdl.directio.text.TextFormatConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.text.AbstractTextStreamFormatGenerator;
import com.asakusafw.dmdl.directio.text.QuoteSettings;
import com.asakusafw.dmdl.directio.text.TextFormatSettings;
import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.runtime.io.text.csv.CsvTextFormat;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits Direct I/O data format classes about CSV text.
 * @since 0.9.1
 */
public class CsvTextEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(CsvTextEmitter.class);

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (CsvTextTrait.find(model).isPresent() == false) {
            return;
        }
        Name supportName = generateFormat(context, model);
        generateInputDescription(context, supportName, model);
        generateOutputDescription(context, supportName, model);
    }

    private static Name generateFormat(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        CsvTextTrait trait = CsvTextTrait.get(model);
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                PACKAGE_SEGMENT,
                "{0}CsvTextFormat"); //$NON-NLS-1$
        LOG.debug("Generating CSV format for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        FormatGenerator.emit(next, model, trait);
        LOG.debug("Generated CSV format for {}: {}", //$NON-NLS-1$
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
                "Abstract{0}CsvTextInputDescription"); //$NON-NLS-1$
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "CSV text file input", context.getQualifiedTypeName()); //$NON-NLS-1$
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
                "Abstract{0}CsvTextOutputDescription"); //$NON-NLS-1$
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "CSV text file output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
    }

    private static final class FormatGenerator extends AbstractTextStreamFormatGenerator {

        private final CsvTextTrait root;

        private final ModelFactory f;

        private FormatGenerator(EmitContext context, ModelDeclaration model, CsvTextTrait root) {
            super(context, model, root.getFormatSettings(), root.getFieldSettings());
            this.root = root;
            this.f = context.getModelFactory();
        }

        static void emit(EmitContext context, ModelDeclaration model, CsvTextTrait trait) throws IOException {
            new FormatGenerator(context, model, trait).emit(Messages.getString("CsvTextEmitter.javadocTitle")); //$NON-NLS-1$
        }

        @Override
        protected List<Statement> createGetTextFormatInternal() {
            SimpleName builder = f.newSimpleName("builder"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(new TypeBuilder(f, context.resolve(CsvTextFormat.class))
                    .method("builder") //$NON-NLS-1$
                    .toLocalVariableDeclaration(context.resolve(CsvTextFormat.Builder.class), builder));
            buildTextFormat(statements, builder);
            statements.add(new ExpressionBuilder(f, builder)
                    .method("build") //$NON-NLS-1$
                    .toReturnStatement());
            return statements;
        }

        private void buildTextFormat(List<Statement> statements, SimpleName builder) {
            TextFormatSettings formats = root.getFormatSettings();
            formats.getCharset().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withCharset", resolve(v.name())) //$NON-NLS-1$
                    .toStatement()));
            formats.getLineSeparator().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withLineSeparator", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
            formats.getFieldSeparator().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withFieldSeparator", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
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

            QuoteSettings quotes = root.getQuoteSettings();
            quotes.getCharacter().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withQuoteCharacter", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
            quotes.getAllowLineFeedInField().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withAllowLineFeedInField", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
            quotes.getDefaultStyle().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withDefaultQuoteStyle", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
            quotes.getHeaderStyle().ifPresent(v -> statements.add(new ExpressionBuilder(f, builder)
                    .method("withHeaderQuoteStyle", resolve(v)) //$NON-NLS-1$
                    .toStatement()));
        }

        @Override
        protected boolean isSplittableInternal() {
            return root.getQuoteSettings().getAllowLineFeedInField()
                    .orElse(CsvTextFormat.DEFAULT_ALLOW_LINE_FEED_IN_FIELD) == false;
        }
    }
}
