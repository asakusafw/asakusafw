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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.windgate.core.resource.DrainDriver;

/**
 * An implementation of {@link DrainDriver} using {@link ModelOutput}.
 * @param <T> the data model type
 * @since 0.2.5
 */
public class ModelOutputDrainDriver<T> implements DrainDriver<T> {

    static final Logger LOG = LoggerFactory.getLogger(ModelOutputDrainDriver.class);

    private final ModelOutput<T> output;

    /**
     * Creates a new instance.
     * @param output target output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ModelOutputDrainDriver(ModelOutput<T> output) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        this.output = output;
    }

    @Override
    public void prepare() throws IOException {
        return;
    }

    @Override
    public void put(T object) throws IOException {
        output.write(object);
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing temporary file drain");
        output.close();
    }
}
