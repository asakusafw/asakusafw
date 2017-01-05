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
package com.asakusafw.compiler.testing;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.io.ModelInput;

/**
 * An implementation of {@link ModelInput} which can compose the multiple {@link ModelInput}.
 * @param <T> the data type
 */
public class MultipleModelInput<T> implements ModelInput<T> {

    static final Logger LOG = LoggerFactory.getLogger(MultipleModelInput.class);

    private final LinkedList<ModelInput<T>> inputs;

    /**
     * Creates a new instance.
     * @param inputs the internal data model inputs
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public MultipleModelInput(List<? extends ModelInput<T>> inputs) {
        Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
        this.inputs = new LinkedList<>(inputs);
    }

    @Override
    public boolean readTo(T model) throws IOException {
        while (inputs.isEmpty() == false) {
            ModelInput<T> input = inputs.getFirst();
            if (input.readTo(model)) {
                return true;
            }
            inputs.removeFirst().close();
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        IOException first = null;
        while (inputs.isEmpty() == false) {
            try (ModelInput<T> input = inputs.removeFirst()) {
                LOG.trace("closing input: {}", input); //$NON-NLS-1$
            } catch (IOException e) {
                if (first == null) {
                    first = e;
                } else {
                    first.addSuppressed(e);
                }
            }
        }
        if (first != null) {
            throw first;
        }
    }
}
