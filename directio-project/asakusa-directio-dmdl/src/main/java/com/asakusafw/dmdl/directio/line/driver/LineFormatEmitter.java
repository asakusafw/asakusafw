/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.line.driver.LineFormatTrait.Configuration;
import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Emits {@link BinaryStreamFormat} implementation for line based text.
 * @since 0.7.5
 */
public class LineFormatEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(LineFormatEmitter.class);

    /**
     * Category name for line format.
     */
    public static final String CATEGORY_STREAM = "line"; //$NON-NLS-1$

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
        validate(model);
        Name supportName = generateFormat(context, model);
        generateInputDescription(context, supportName, model);
        generateOutputDescription(context, supportName, model);
    }

    private void validate(ModelDeclaration model) throws IOException {
        boolean sawBody = false;
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            LineFieldTrait.Kind kind = LineFieldTrait.getKind(property);
            switch (kind) {
            case BODY:
                if (sawBody) {
                    throw new IOException(MessageFormat.format(
                            Messages.getString("LineFormatEmitter.errorDuplicateBody"), //$NON-NLS-1$
                            model.getName().identifier));
                }
                sawBody = true;
                checkType(property, BasicTypeKind.TEXT);
                break;
            case FILE_NAME:
                checkType(property, BasicTypeKind.TEXT);
                break;
            case LINE_NUMBER:
                checkType(property, BasicTypeKind.INT, BasicTypeKind.LONG);
                break;
            default:
                break;
            }
        }
        if (sawBody == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("LineFormatEmitter.errorMissingBody"), //$NON-NLS-1$
                    model.getName().identifier));
        }
    }

    private void checkType(PropertyDeclaration property, BasicTypeKind... kinds) throws IOException {
        Set<BasicTypeKind> set = EnumSet.noneOf(BasicTypeKind.class);
        Collections.addAll(set, kinds);
        Type type = property.getType();
        if ((type instanceof BasicType) == false || set.contains(((BasicType) type).getKind()) == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("LineFormatEmitter.errorInconsistentType"), //$NON-NLS-1$
                    kinds.length == 1 ? kinds[0] : set,
                    property.getOwner().getName().identifier,
                    property.getName().identifier));
        }
    }

    static PropertyDeclaration findProperty(ModelDeclaration model, LineFieldTrait.Kind kind) {
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            if (LineFieldTrait.getKind(property) == kind) {
                return property;
            }
        }
        return null;
    }

    private Name generateFormat(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "{0}LineFormat"); //$NON-NLS-1$
        LOG.debug("Generating line format for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        Configuration conf = model.getTrait(LineFormatTrait.class).getConfiguration();
        new LineStreamFormatGenerator(next, model, conf).emit();
        LOG.debug("Generated line format for {}: {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private void generateInputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}LineInputDescription"); //$NON-NLS-1$
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "Line file input", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileInputDescriptionGenerator.generate(next, desc);
    }

    private void generateOutputDescription(
            EmitContext context, Name formatClassName, ModelDeclaration model) throws IOException {
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_STREAM,
                "Abstract{0}LineOutputDescription"); //$NON-NLS-1$
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "Line file output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        LineFormatTrait trait = model.getTrait(LineFormatTrait.class);
        return trait != null;
    }
}
