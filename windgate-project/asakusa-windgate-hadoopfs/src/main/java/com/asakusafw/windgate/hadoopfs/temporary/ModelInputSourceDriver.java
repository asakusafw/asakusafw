/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.windgate.hadoopfs.temporary;

import java.io.EOFException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * An implementation of {@link SourceDriver} using {@link ModelInputProvider}.
 * @param <T> the value type
 * @since 0.2.5
 */
public class ModelInputSourceDriver<T> implements SourceDriver<T> {

    static final Logger LOG = LoggerFactory.getLogger(ModelInputSourceDriver.class);

    private final ModelInputProvider<T> provider;

    private final T value;

    private ModelInput<T> currentInput;

    private boolean sawNext;

    /**
     * Creates a new instance.
     * @param provider the source model inputs
     * @param value the value object used for buffer
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ModelInputSourceDriver(ModelInputProvider<T> provider, T value) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null"); //$NON-NLS-1$
        }
        this.provider = provider;
        this.value = value;
    }

    @Override
    public void prepare() {
        sawNext = false;
        currentInput = null;
    }

    @Override
    public boolean next() throws IOException {
        while (true) {
            if (currentInput == null) {
                if (provider.next()) {
                    currentInput = provider.open();
                } else {
                    sawNext = false;
                    return false;
                }
            }
            if (currentInput.readTo(value)) {
                sawNext = true;
                return true;
            } else {
                currentInput.close();
                currentInput = null;
                // continue
            }
        }
    }

    @Override
    public T get() throws IOException {
        if (sawNext) {
            return value;
        }
        throw new EOFException();
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing temporary file source");
        sawNext = false;
        try {
            if (currentInput != null) {
                currentInput.close();
                currentInput = null;
            }
        } finally {
            provider.close();
        }
    }
}
