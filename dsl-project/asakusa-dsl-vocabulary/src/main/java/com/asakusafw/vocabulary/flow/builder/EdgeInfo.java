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
package com.asakusafw.vocabulary.flow.builder;

/**
 * Represents operator edge information.
 * @param <TSelf> the concrete type of this
 * @since 0.9.0
 */
public abstract class EdgeInfo<TSelf> {

    private Integer parameterIndex;

    private KeyInfo key;

    private ExternInfo extern;

    /**
     * Returns this.
     * @return this
     */
    protected abstract TSelf getSelf();

    /**
     * Returns the edge name.
     * @return the edge name
     */
    public abstract String getName();

    /**
     * Returns the index where this appears in the target parameters.
     * @return the parameter index, or {@code -1} if it is not defined
     */
    public Integer getParameterIndex() {
        return parameterIndex;
    }

    /**
     * Returns key information.
     * @return the key information, or {@code null} if this port does not have any keys
     */
    public KeyInfo getKey() {
        return key;
    }

    /**
     * Returns external I/O information.
     * @return the corresponded external I/O information, or {@code null} if this port is not external
     */
    public ExternInfo getExtern() {
        return extern;
    }

    /**
     * Sets the index where this appears in the target parameters.
     * @param newValue the parameter index
     * @return this
     */
    public TSelf withParameterIndex(int newValue) {
        this.parameterIndex = newValue;
        return getSelf();
    }

    /**
     * Sets key information.
     * @param newValue information object
     * @return this
     */
    public TSelf withKey(KeyInfo newValue) {
        this.key = newValue;
        return getSelf();
    }

    /**
     * Sets external I/O information.
     * @param newValue information object
     * @return this
     */
    public TSelf withExtern(ExternInfo newValue) {
        this.extern = newValue;
        return getSelf();
    }
}
