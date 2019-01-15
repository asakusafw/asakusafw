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
package com.asakusafw.dmdl.directio.json.driver;

import static com.asakusafw.dmdl.directio.json.driver.JsonFormatConstants.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.directio.util.DirectFileInputDescriptionGenerator;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Emits Direct I/O data format classes about JSON format.
 * @since 0.9.1
 */
public class JsonFormatEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(JsonFormatEmitter.class);

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (JsonFormatTrait.find(model).isPresent() == false) {
            return;
        }
        Name supportName = generateFormat(context, model);
        generateInputDescription(context, supportName, model);
        generateOutputDescription(context, supportName, model);
    }

    private static Name generateFormat(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        JsonFormatTrait trait = JsonFormatTrait.get(model);
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                PACKAGE_SEGMENT,
                "{0}JsonFormat"); //$NON-NLS-1$
        LOG.debug("Generating JSON format for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        JsonStreamFormatGenerator generator = new JsonStreamFormatGenerator(
                next, model,
                trait.getStreamSettings(),
                trait.getFormatSettings(),
                trait.getPropertySettings());
        generator.emit();

        LOG.debug("Generated JSON format for {}: {}", //$NON-NLS-1$
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
                "Abstract{0}JsonInputDescription"); //$NON-NLS-1$
        DirectFileInputDescriptionGenerator.Description desc = new DirectFileInputDescriptionGenerator.Description(
                "JSON file input", context.getQualifiedTypeName()); //$NON-NLS-1$
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
                "Abstract{0}JsonOutputDescription"); //$NON-NLS-1$
        DirectFileOutputDescriptionGenerator.Description desc = new DirectFileOutputDescriptionGenerator.Description(
                "JSON file output", context.getQualifiedTypeName()); //$NON-NLS-1$
        desc.setFormatClassName(formatClassName);
        DirectFileOutputDescriptionGenerator.generate(next, desc);
    }
}
