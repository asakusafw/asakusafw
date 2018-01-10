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
package com.asakusafw.workflow.cli.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.basic.BasicCommandLauncher;
import com.asakusafw.workflow.executor.basic.BasicCommandLauncher.OutputChannel;

/**
 * An command output consumer for workflow executions.
 * @see #LOGGER_NAME
 * @since 0.10.0
 */
public class ExecutorLogger implements BasicCommandLauncher.OutputConsumer {

    /**
     * The logger name.
     */
    public static final String LOGGER_NAME = "com.asakusafw.cli.workflow.executor";

    @Override
    public void accept(OutputChannel channel, String label, CharSequence line) {
        Lazy.LOG.info("({}:{}) {}", label, channel, line);
    }

    private static final class Lazy {

        static final Logger LOG = LoggerFactory.getLogger(LOGGER_NAME);

        private Lazy() {
            return;
        }
    }
}
