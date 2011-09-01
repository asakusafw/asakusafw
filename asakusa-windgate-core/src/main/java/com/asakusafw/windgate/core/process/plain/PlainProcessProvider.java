/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.process.plain;

import java.io.IOException;

import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.DriverFactory;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * A plain implementation of {@link ProcessProvider}.
 * This provider ignores any configurations specified in profile,
 * and performs as a default gate process.
 * @since 0.2.3
 */
public class PlainProcessProvider extends ProcessProvider {

    @Override
    protected void configure(ProcessProfile profile) {
        return;
    }

    @Override
    public <T> void execute(DriverFactory drivers, ProcessScript<T> script) throws IOException {
        IOException exception = null;
        SourceDriver<T> source = null;
        DrainDriver<T> drain = null;
        try {
            source = drivers.createSource(script);
            drain = drivers.createDrain(script);
            source.prepare();
            drain.prepare();
            while (source.next()) {
                T obj = source.get();
                drain.put(obj);
            }
        } catch (IOException e) {
            exception = e;
            // TODO logging
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
            } catch (IOException e) {
                exception = exception == null ? e : exception;
                // TODO logging
            }
            try {
                if (drain != null) {
                    drain.close();
                }
            } catch (IOException e) {
                exception = exception == null ? e : exception;
                // TODO logging
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
