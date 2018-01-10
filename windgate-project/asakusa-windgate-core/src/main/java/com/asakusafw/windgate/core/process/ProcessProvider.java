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

import com.asakusafw.windgate.core.BaseProvider;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DriverFactory;

/**
 * An abstract super class of process factory.
 * Clients can inherit this class to provide new data sources.
 * Each subclass must provide a public constructor with no parameters.
 * @since 0.2.2
 */
public abstract class ProcessProvider extends BaseProvider<ProcessProfile> {

    /**
     * Execute a gate process for the specified process script.
     * @param <T> the type of data model object to be processed
     * @param drivers provides drivers for the execution
     * @param script target script
     * @throws IOException if failed to execute gate process
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public abstract <T> void execute(DriverFactory drivers, ProcessScript<T> script) throws IOException;
}
