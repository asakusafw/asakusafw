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
package com.asakusafw.dmdl.java;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.java.analyzer.JavaDataModelAnalyzerEnhancer;
import com.asakusafw.dmdl.java.emitter.CompositeDataModelDriver;
import com.asakusafw.dmdl.java.emitter.JavaModelClassGenerator;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.util.AnalyzeTask;

/**
 * Generates Java model classes from input DMDL scripts.
 */
public class GenerateTask {

    static final Logger LOG = LoggerFactory.getLogger(GenerateTask.class);

    private final Configuration conf;

    /**
     * Creates and returns a new instance.
     * @param conf emitter configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public GenerateTask(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
    }

    /**
     * Generates all models from source repository in the current configuration.
     * @throws IOException if failed to process DMDL scripts
     */
    public void process() throws IOException {
        JavaDataModelDriver driver = new CompositeDataModelDriver(conf.getServiceClassLoader());
        process(driver);
    }

    /**
     * Generates all models from source repository in the current configuration.
     * @param driver the Java program generator driver
     * @throws IOException if failed to process DMDL scripts
     */
    public void process(JavaDataModelDriver driver) throws IOException {
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        DmdlSemantics semantics = analyze();
        JavaModelClassGenerator generator = new JavaModelClassGenerator(semantics, conf, driver);
        Collection<ModelDeclaration> models = semantics.getDeclaredModels();
        LOG.info(MessageFormat.format(
                Messages.getString("GenerateTask.monitorGenerateStarting"), //$NON-NLS-1$
                models.size()));
        for (ModelDeclaration model : models) {
            LOG.info(MessageFormat.format(Messages.getString("GenerateTask.monitorGenerateModel"), //$NON-NLS-1$
                    model.getName()));
            generator.emit(model);
        }
        LOG.info(Messages.getString("GenerateTask.monitorGenerateFinishing")); //$NON-NLS-1$
    }

    private DmdlSemantics analyze() throws IOException {
        AnalyzeTask analyzer = new AnalyzeTask(
                Messages.getString("GenerateTask.name"), //$NON-NLS-1$
                new JavaDataModelAnalyzerEnhancer(),
                conf.getServiceClassLoader());
        return analyzer.process(conf.getSource());
    }
}
