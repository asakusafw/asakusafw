/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

/**
 * Adapters {@link ModelTester} to {@link TestRule}.
 * @param <T> type of model
 * @since 0.2.0
 */
public class TesterDriver<T> implements TestRule {

    private final ModelTester<? super T> verifier;

    private final DataModelDefinition<? extends T> definition;

    /**
     * Creates a new instance.
     * @param verifier {@link ModelVerifier} to adapt
     * @param definition target model definition
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TesterDriver(
            ModelTester<? super T> verifier,
            DataModelDefinition<? extends T> definition) {
        if (verifier == null) {
            throw new IllegalArgumentException("verifier must not be null"); //$NON-NLS-1$
        }
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        this.verifier = verifier;
        this.definition = definition;
    }

    @Override
    public Object verify(DataModelReflection expected, DataModelReflection actual) {
        return verifier.verify(convert(expected), convert(actual));
    }

    private T convert(DataModelReflection reflection) {
        if (reflection == null) {
            return null;
        }
        return definition.toObject(reflection);
    }
}
