/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.windgate;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.windgate.core.resource.DrainDriver;

/**
 * An implementation of {@link ModelOutput} using {@link DrainDriver}.
 * @param <T> the target model
 * @since 0.2.2
 */
public class WindGateOutput<T> implements ModelOutput<T> {

    private final DrainDriver<T> driver;

    /**
     * Creates a new instance.
     * @param driver the drain driver, this must be prepared
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WindGateOutput(DrainDriver<T> driver) {
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        this.driver = driver;
    }

    @Override
    public void write(T model) throws IOException {
        driver.put(model);
    }

    @Override
    public void close() throws IOException {
        driver.close();
    }
}
