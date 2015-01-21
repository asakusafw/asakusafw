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
package com.asakusafw.runtime.util.lock;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of {@link RetryStrategy} with constant time.
 * @since 0.7.0
 */
public class ConstantRetryStrategy implements RetryStrategy {

    static final Log LOG = LogFactory.getLog(ConstantRetryStrategy.class);

    private final int maxAttemptNumber;

    private final long minInterval;

    private final long maxInterval;

    /**
     * Creates a new instance for do not retry tasks.
     */
    public ConstantRetryStrategy() {
        this(0, 0, 0);
    }

    /**
     * Creates a new instance for do not retry tasks.
     * @param maxRetryCount maximum retry count ({@code >= 0})
     */
    public ConstantRetryStrategy(int maxRetryCount) {
        this(maxRetryCount, 0, 0);
    }

    /**
     * Creates a new instance.
     * @param maxRetryCount maximum retry count ({@code >= 0})
     * @param interval retry interval (ms)
     */
    public ConstantRetryStrategy(int maxRetryCount, long interval) {
        this(maxRetryCount, interval, interval);
    }

    /**
     * Creates a new instance.
     * @param maxRetryCount maximum retry count ({@code >= 0})
     * @param minInterval minimum retry interval (ms)
     * @param maxInterval maximum retry interval (ms)
     */
    public ConstantRetryStrategy(int maxRetryCount, long minInterval, long maxInterval) {
        this.maxAttemptNumber = Math.max(0, maxRetryCount) + 1;
        this.minInterval = Math.max(0, minInterval);
        this.maxInterval = Math.max(this.minInterval, maxInterval);
    }

    @Override
    public RetryObject newInstance(String taskName) {
        return new Retry(taskName, maxAttemptNumber, minInterval, maxInterval);
    }

    private static final class Retry implements RetryObject {

        private final String taskName;

        private final int maxAttemptNumber;

        private final long minInterval;

        private final long maxInterval;

        private int attemptNumber = 1;

        public Retry(String taskName, int maxAttemptNumber, long minInterval, long maxInterval) {
            this.taskName = taskName;
            this.maxAttemptNumber = maxAttemptNumber;
            this.minInterval = minInterval;
            this.maxInterval = maxInterval;
        }

        @Override
        public boolean waitForNextAttempt() throws InterruptedException {
            if (attemptNumber >= maxAttemptNumber) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "exceeded max attempt number ({2}): {0}", //$NON-NLS-1$
                            taskName,
                            maxAttemptNumber));
                }
                return false;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "waiting for next attempt ({1}/{2}): {0}", //$NON-NLS-1$
                        taskName,
                        attemptNumber,
                        maxAttemptNumber));
            }
            long interval;
            if (minInterval == maxInterval) {
                interval = minInterval;
            } else {
                interval = minInterval + (long) ((maxInterval - minInterval) * Math.random());
            }
            if (interval > 0) {
                Thread.sleep(interval);
            }
            attemptNumber++;
            return true;
        }
    }
}
