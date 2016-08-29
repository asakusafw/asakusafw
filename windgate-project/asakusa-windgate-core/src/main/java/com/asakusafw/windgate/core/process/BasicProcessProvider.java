/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateCoreLogger;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.DriverFactory;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.util.ExceptionHolder;
import com.asakusafw.windgate.core.util.SafeCloser;

/**
 * A plain implementation of {@link ProcessProvider}.
 * This provider ignores any configurations specified in profile,
 * and performs as a default gate process.
 * @since 0.2.2
 * @version 0.4.0
 */
@SimulationSupport
public class BasicProcessProvider extends ProcessProvider {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(BasicProcessProvider.class);

    static final Logger LOG = LoggerFactory.getLogger(BasicProcessProvider.class);

    @Override
    protected void configure(ProcessProfile profile) {
        return;
    }

    @Override
    public <T> void execute(DriverFactory drivers, ProcessScript<T> script) throws IOException {
        WGLOG.info("I05000",
                script.getName(),
                script.getSourceScript().getResourceName(),
                script.getDrainScript().getResourceName());
        long start = System.currentTimeMillis();
        long count = 0;
        ExceptionHolder<IOException> exceptions = new ExceptionHolder<>();
        try (SafeCloser<SourceDriver<T>> source = new SafeCloser<SourceDriver<T>>() {
            @Override
            protected void handle(IOException exception) throws IOException {
                exceptions.record(exception);
                WGLOG.error(exception, "E05002",
                        script.getName(),
                        script.getSourceScript().getResourceName(),
                        script.getDrainScript().getResourceName());
            }
        }; SafeCloser<DrainDriver<T>> drain = new SafeCloser<DrainDriver<T>>() {
            @Override
            protected void handle(IOException exception) throws IOException {
                exceptions.record(exception);
                WGLOG.error(exception, "E05003",
                        script.getName(),
                        script.getSourceScript().getResourceName(),
                        script.getDrainScript().getResourceName());
            }
        }) {
            LOG.debug("Creating source driver for resource \"{}\" in process \"{}\"",
                    script.getSourceScript().getResourceName(),
                    script.getName());
            source.set(drivers.createSource(script));
            LOG.debug("Creating drain driver for resource \"{}\" in process \"{}\"",
                    script.getDrainScript().getResourceName(),
                    script.getName());
            drain.set(drivers.createDrain(script));
            performPrepare(script, source.get(), drain.get());
            count = performTransfer(script, source.get(), drain.get());
            exceptions.throwRecorded();
            WGLOG.info("I05001",
                    script.getName(),
                    script.getSourceScript().getResourceName(),
                    script.getDrainScript().getResourceName(),
                    count);
        } finally {
            long end = System.currentTimeMillis();
            WGLOG.info("I05999",
                    script.getName(),
                    script.getSourceScript().getResourceName(),
                    script.getDrainScript().getResourceName(),
                    count,
                    end - start);
        }
    }

    private <T> void performPrepare(
            ProcessScript<T> script,
            SourceDriver<T> source,
            DrainDriver<T> drain) throws IOException {
        assert script != null;
        assert source != null;
        assert drain != null;
        LOG.debug("Preparing source driver for resource \"{}\" in process \"{}\"",
                script.getSourceScript().getResourceName(),
                script.getName());
        if (RuntimeContext.get().canExecute(source)) {
            source.prepare();
        } else {
            LOG.info("Skipped preparing source driver (simulation)");
        }
        LOG.debug("Preparing drain driver for resource \"{}\" in process \"{}\"",
                script.getSourceScript().getResourceName(),
                script.getName());
        if (RuntimeContext.get().canExecute(drain)) {
            drain.prepare();
        } else {
            LOG.info("Skipped preparing drain driver (simulation)");
        }
    }

    private <T> long performTransfer(
            ProcessScript<T> script,
            SourceDriver<T> source,
            DrainDriver<T> drain) throws IOException {
        assert script != null;
        assert source != null;
        assert drain != null;
        LOG.debug("Starting transfer \"{}\" -> \"{}\" in process \"{}\"", new Object[] {
                script.getSourceScript().getResourceName(),
                script.getDrainScript().getResourceName(),
                script.getName(),
        });
        long count = 0;
        if (RuntimeContext.get().canExecute(source) && RuntimeContext.get().canExecute(drain)) {
            while (source.next()) {
                T obj = source.get();
                drain.put(obj);
                count++;
            }
        } else if (RuntimeContext.get().canExecute(source)) {
            LOG.info("Skipped transfering to drain (simulation)");
            // drop source info
            while (source.next()) {
                count++;
            }
        } else if (RuntimeContext.get().canExecute(drain)) {
            LOG.info("Skipped transfering from source (simulation)");
        } else {
            LOG.info("Skipped transfering (simulation)");
        }
        return count;
    }
}
