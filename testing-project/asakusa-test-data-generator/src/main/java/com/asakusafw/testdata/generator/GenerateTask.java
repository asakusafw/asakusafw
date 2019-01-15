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
package com.asakusafw.testdata.generator;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.util.AnalyzeTask;

/**
 * Generates test templates from input DMDL scripts.
 * @since 0.2.0
 * @version 0.5.0
 */
public class GenerateTask {

    static final Logger LOG = LoggerFactory.getLogger(GenerateTask.class);

    private final TemplateGenerator generator;

    private final DmdlSourceRepository repository;

    private final ClassLoader serviceClassLoader;

    /**
     * Creates a new instance.
     * @param generator template generator
     * @param repository source repository
     * @param serviceClassLoader class loader to load plug-ins
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public GenerateTask(
            TemplateGenerator generator,
            DmdlSourceRepository repository,
            ClassLoader serviceClassLoader) {
        if (generator == null) {
            throw new IllegalArgumentException("generator must not be null"); //$NON-NLS-1$
        }
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null"); //$NON-NLS-1$
        }
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.generator = generator;
        this.repository = repository;
        this.serviceClassLoader = serviceClassLoader;
    }

    /**
     * Generates all templates from source repository in the current configuration.
     * @throws IOException if failed to process DMDL scripts, or failed to generate templates
     */
    public void process() throws IOException {
        LOG.info(Messages.getString("GenerateTask.infoStartTask")); //$NON-NLS-1$
        DmdlSemantics semantics = analyze();
        for (ModelDeclaration model : semantics.getDeclaredModels()) {
            LOG.info(MessageFormat.format(
                    Messages.getString("GenerateTask.infoGenerateTemplate"), //$NON-NLS-1$
                    model.getName().identifier));
            generator.generate(model);
        }
        LOG.info(Messages.getString("GenerateTask.infoFinishTask")); //$NON-NLS-1$
    }

    private DmdlSemantics analyze() throws IOException {
        AnalyzeTask analyzer = new AnalyzeTask(generator.getTitle(), serviceClassLoader);
        return analyzer.process(repository);
    }
}
