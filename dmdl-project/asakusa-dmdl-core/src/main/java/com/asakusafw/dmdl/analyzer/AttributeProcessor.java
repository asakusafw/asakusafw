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
package com.asakusafw.dmdl.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.AttributeDriver;

final class AttributeProcessor {

    static final Logger LOG = LoggerFactory.getLogger(AttributeProcessor.class);

    private final Context context;

    private AttributeProcessor(Context context) {
        this.context = context;
    }

    static void resolve(Context context, ModelDeclaration model) {
        new AttributeProcessor(context).resolveAttributes(model);
    }

    static void verify(Context context, ModelDeclaration model) {
        new AttributeProcessor(context).verifyAttributes(model);
    }

    private void resolveAttributes(ModelDeclaration model) {
        LOG.debug("resolving attributes: {}", model.getName()); //$NON-NLS-1$
        doResolveAttributes(model);
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            doResolveAttributes(property);
        }
        // NOTE: we never process attributes for property references
    }

    private void doResolveAttributes(Declaration declaration) {
        assert declaration != null;
        AttributeDriver.Context local = getDriverContext();
        for (AstAttribute attribute : declaration.getAttributes()) {
            String name = attribute.name.toString();
            LOG.debug("resolving attribute: {} -> {}", declaration.getName(), name); //$NON-NLS-1$
            AttributeDriver driver = context.findAttributeDriver(attribute);
            if (driver == null) {
                context.error(
                        attribute.name,
                        Messages.getString("DmdlAnalyzer.diagnosticUnknownAttribute"), //$NON-NLS-1$
                        name);
                continue;
            }
            LOG.debug("processing attribute: {} -> {}", name, driver); //$NON-NLS-1$
            driver.process(local, declaration, attribute);
        }
    }

    private void verifyAttributes(ModelDeclaration model) {
        LOG.debug("verifying attributes: {}", model.getName()); //$NON-NLS-1$
        doVerifyAttributes(model);
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            doVerifyAttributes(property);
        }
    }

    private void doVerifyAttributes(Declaration declaration) {
        assert declaration != null;
        AttributeDriver.Context local = getDriverContext();
        for (AstAttribute attribute : declaration.getAttributes()) {
            String name = attribute.name.toString();
            AttributeDriver driver = context.findAttributeDriver(attribute);
            if (driver == null) {
                // may not occur
                continue;
            }
            LOG.debug("verifying attribute: {} -> {}", name, driver); //$NON-NLS-1$
            driver.verify(local, declaration, attribute);
        }
    }

    private AttributeDriver.Context getDriverContext() {
        DmdlSemantics environment = context.getWorld();
        AttributeDriver.Context local = new AttributeDriver.Context() {
            @Override
            public DmdlSemantics getEnvironment() {
                return environment;
            }
        };
        return local;
    }
}
