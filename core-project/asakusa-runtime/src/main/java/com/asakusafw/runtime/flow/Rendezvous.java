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
package com.asakusafw.runtime.flow;

import org.apache.hadoop.io.Writable;

/**
 * An abstract super interface for processing co-group like operations.
 * @param <V> the data type
 */
public abstract class Rendezvous<V extends Writable> {

    /**
     * The method name of {@link #begin()}.
     */
    public static final String BEGIN = "begin"; //$NON-NLS-1$

    /**
     * The method name of {@link #process(Writable)}.
     */
    public static final String PROCESS = "process"; //$NON-NLS-1$

    /**
     * The method name of {@link #end()}.
     */
    public static final String END = "end"; //$NON-NLS-1$

    /**
     * Begins processing an input group.
     */
    public abstract void begin();

    /**
     * Processes an input data.
     * This must be invoked between {@link #begin()} and {@link #end()},
     * and the input data must be a member of the current group.
     * @param value the input data
     */
    public abstract void process(V value);

    /**
     * Ends processing the current input group.
     */
    public abstract void end();
}
