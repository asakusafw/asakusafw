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
package com.asakusafw.dmdl.java.emitter;

import java.io.IOException;

import com.asakusafw.dmdl.java.Configuration;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Generates the Java data model classes for each DMDL model.
 */
public class JavaModelClassGenerator {

    private final DmdlSemantics semantics;

    private final Configuration config;

    private final JavaDataModelDriver driver;

    /**
     * Creates and returns a new instance.
     * @param semantics the root semantics model
     * @param config current configuration
     * @param driver the data model decorator
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JavaModelClassGenerator(
            DmdlSemantics semantics,
            Configuration config,
            JavaDataModelDriver driver) {
        if (semantics == null) {
            throw new IllegalArgumentException("semantics must not be null"); //$NON-NLS-1$
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        this.semantics = semantics;
        this.config = config;
        this.driver = driver;
    }

    /**
     * Emits a Java data model class of the model.
     * @param model target model
     * @throws IOException if failed to output Java programs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void emit(ModelDeclaration model) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        switch (model.getOriginalAst().kind) {
        case PROJECTIVE:
            new ProjectiveModelEmitter(semantics, config, model, driver).emit();
            break;
        case RECORD:
        case JOINED:
        case SUMMARIZED:
            new ConcreteModelEmitter(semantics, config, model, driver).emit();
            break;
        default:
            throw new AssertionError(model.getOriginalAst().kind);
        }
    }
}
