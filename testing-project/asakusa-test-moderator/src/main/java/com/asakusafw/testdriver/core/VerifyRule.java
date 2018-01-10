/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * Strategy of test result verification.
 * @since 0.2.0
 * @version 0.9.0
 */
public interface VerifyRule extends TestRule {

    /**
     * A verify rule that accepts any data.
     * @since 0.9.0
     */
    VerifyRule NULL = new VerifyRule() {
        @Override
        public Object getKey(DataModelReflection target) {
            return new Object();
        }
        @Override
        public Object verify(DataModelReflection expected, DataModelReflection actual) {
            return null;
        }
    };

    /**
     * Returns the key of the target data model.
     * <p>
     * This method must return objects which have
     * both {@link Object#equals(Object)} and {@link Object#hashCode()}.
     * </p>
     * @param target the target
     * @return the key
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    Object getKey(DataModelReflection target);
}
