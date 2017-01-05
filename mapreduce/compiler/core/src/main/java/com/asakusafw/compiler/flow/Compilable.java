/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

/**
 * Represents an element which can be compiled.
 * @param <T> the compiled element type
 */
public interface Compilable<T> {

    /**
     * Returns whether this element has been already compiled or not.
     * @return {@code true} if this element has been already compiled, otherwise {@code false}
     */
    boolean isCompiled();

    /**
     * Returns the compiled element for this.
     * @return the compiled element
     * @throws IllegalStateException this element has not been compiled yet
     * @see #isCompiled()
     */
    T getCompiled();

    /**
     * Sets the compiled element.
     * @param object the compiled element
     * @throws IllegalStateException this element has been already compiled
     * @throws IllegalArgumentException if the parameter is {@code null}
     * {@link #isCompiled()}
     */
    void setCompiled(T object);

    /**
     * A basic implementation of {@link Compilable}.
     * @param <T> the compiled element type
     */
    class Trait<T> implements Compilable<T> {

        private T compiled;

        @Override
        public boolean isCompiled() {
            return compiled != null;
        }

        @Override
        public T getCompiled() {
            if (isCompiled() == false) {
                throw new IllegalStateException();
            }
            return compiled;
        }

        @Override
        public void setCompiled(T object) {
            if (isCompiled()) {
                throw new IllegalStateException();
            }
            this.compiled = object;
        }
    }
}
