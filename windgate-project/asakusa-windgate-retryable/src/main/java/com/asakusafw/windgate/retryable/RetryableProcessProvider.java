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
package com.asakusafw.windgate.retryable;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.resource.DriverFactory;

/**
 * Retryable processes.
 * @since 0.2.4
 * @version 0.5.0
 */
@SimulationSupport
public class RetryableProcessProvider extends ProcessProvider {

    static final WindGateLogger WGLOG = new RetryableProcessLogger(RetryableProcessProvider.class);

    private volatile RetryableProcessProfile processProfile;

    @Override
    protected void configure(ProcessProfile profile) throws IOException {
        try {
            processProfile = RetryableProcessProfile.convert(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure resource \"{0}\"",
                    profile.getName()));
        }
    }

    @Override
    public <T> void execute(DriverFactory drivers, ProcessScript<T> script) throws IOException {
        int maxAttempts = processProfile.getRetryCount() + 1;
        WGLOG.info("I01000",
                script.getName(),
                script.getSourceScript().getResourceName(),
                script.getDrainScript().getResourceName(),
                processProfile.getRetryCount());
        long start = System.currentTimeMillis();
        try {
            int attempt = 1;
            while (true) {
                assert attempt <= maxAttempts;
                try {
                    processProfile.getComponent().execute(drivers, script);
                    break;
                } catch (InterruptedIOException e) {
                    WGLOG.error(e, "E01002",
                            script.getName(),
                            script.getSourceScript().getResourceName(),
                            script.getDrainScript().getResourceName());
                    throw e;
                } catch (IOException e) {
                    if (attempt < maxAttempts) {
                        WGLOG.warn(e, "W01001",
                                script.getName(),
                                script.getSourceScript().getResourceName(),
                                script.getDrainScript().getResourceName(),
                                attempt,
                                processProfile.getRetryCount());
                        attempt++;
                    } else {
                        WGLOG.error(e, "E01001",
                                script.getName(),
                                script.getSourceScript().getResourceName(),
                                script.getDrainScript().getResourceName(),
                                maxAttempts);
                        throw e;
                    }
                }
                if (processProfile.getRetryInterval() > 0) {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(
                                processProfile.getRetryInterval()));
                    } catch (InterruptedException e) {
                        WGLOG.error(e, "E01002",
                                script.getName(),
                                script.getSourceScript().getResourceName(),
                                script.getDrainScript().getResourceName());
                        throw (IOException) new InterruptedIOException().initCause(e);
                    }
                }
            }
            WGLOG.info("I01001",
                    script.getName(),
                    script.getSourceScript().getResourceName(),
                    script.getDrainScript().getResourceName(),
                    attempt);
        } finally {
            long end = System.currentTimeMillis();
            WGLOG.info("I01999",
                    script.getName(),
                    script.getSourceScript().getResourceName(),
                    script.getDrainScript().getResourceName(),
                    end - start);
        }
    }
}
