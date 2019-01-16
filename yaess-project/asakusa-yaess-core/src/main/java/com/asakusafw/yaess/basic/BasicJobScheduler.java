/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.asakusafw.yaess.core.JobScheduler;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Basic implementation of {@link JobScheduler}.
 * @since 0.2.3
 */
public class BasicJobScheduler extends AbstractJobScheduler {

    static final AtomicInteger COUNTER = new AtomicInteger();

    private volatile JobExecutor executor;

    @Override
    protected void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        this.executor = new ThreadedJobExecutor(Executors.newFixedThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName(MessageFormat.format(
                    "BasicJobScheduler-{0}",
                    COUNTER.incrementAndGet()));
            return thread;
        }));
    }

    @Override
    protected JobExecutor getJobExecutor() {
        return executor;
    }
}
