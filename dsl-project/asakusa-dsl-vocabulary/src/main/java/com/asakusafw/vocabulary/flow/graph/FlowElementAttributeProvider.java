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
package com.asakusafw.vocabulary.flow.graph;

/**
 * Provides {@link FlowElementAttribute}s.
 * @since 0.4.0
 */
public interface FlowElementAttributeProvider {

    /**
     * Returns the attribute of this.
     * @param <T> target class
     * @param attributeClass target class
     * @return attribute, or {@code null} if not defined
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass);
}
