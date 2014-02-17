/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.yaess.paralleljob;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.basic.AbstractJobScheduler;
import com.asakusafw.yaess.basic.JobExecutor;
import com.asakusafw.yaess.core.JobScheduler;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Basic implementation of {@link JobScheduler}.
 * @since 0.2.3
 */
public class ParallelJobScheduler extends AbstractJobScheduler {

    static final Logger LOG = LoggerFactory.getLogger(ParallelJobScheduler.class);

    private volatile JobExecutor executor;

    @Override
    protected void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        try {
            this.executor = ParallelJobExecutor.extract(
                    profile.getPrefix(),
                    profile.getConfiguration(),
                    profile.getContext().getContextParameters());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure job scheduler: {0}",
                    profile.getPrefix()), e);
        }
    }

    @Override
    protected JobExecutor getJobExecutor() {
        return executor;
    }
}
