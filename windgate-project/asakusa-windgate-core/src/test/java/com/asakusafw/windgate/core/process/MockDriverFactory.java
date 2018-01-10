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
package com.asakusafw.windgate.core.process;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.DriverFactory;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * Mock {@link DriverFactory}.
 */
public class MockDriverFactory implements DriverFactory {

    private final Map<String, SourceDriver<?>> sources = new HashMap<>();

    private final Map<String, DrainDriver<?>> drains = new HashMap<>();

    /**
     * Adds a driver for the process which has the specified name.
     * @param <T> the type of driver target
     * @param <D> the type of driver
     * @param name target process name
     * @param driver the driver to be added
     * @return the driver
     */
    public <T, D extends SourceDriver<T>> D add(String name, D driver) {
        sources.put(name, driver);
        return driver;
    }
    /**
     * Adds a driver for the process which has the specified name.
     * @param <T> the type of driver target
     * @param <D> the type of driver
     * @param name target process name
     * @param driver the driver to be added
     * @return the driver
     */
    public <T, D extends DrainDriver<T>> D add(String name, D driver) {
        drains.put(name, driver);
        return driver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
        SourceDriver<?> driver = sources.remove(script.getName());
        if (driver == null) {
            throw new IOException(script.getName());
        }
        return (SourceDriver<T>) driver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        DrainDriver<?> driver = drains.remove(script.getName());
        if (driver == null) {
            throw new IOException(script.getName());
        }
        return (DrainDriver<T>) driver;
    }
}
