/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.optimizer;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.asakusafw.runtime.stage.StageConfigurator;
import com.asakusafw.runtime.stage.input.StageInputDriver;

/**
 * Configures number of reduce tasks.
 * @since 0.6.2
 */
public class ReducerSimplifierConfigurator extends StageConfigurator {

    static final Log LOG = LogFactory.getLog(ReducerSimplifierConfigurator.class);

    private static final String KEY_TINY_LIMIT = "com.asakusafw.reducer.tiny.limit"; //$NON-NLS-1$

    private static final int TASKS_TINY = 1;

    @Override
    public void configure(Job job) throws IOException, InterruptedException {
        int count = job.getNumReduceTasks();
        if (count <= TASKS_TINY) {
            return;
        }
        Configuration conf = job.getConfiguration();
        long limit = conf.getLong(KEY_TINY_LIMIT, -1L);
        if (limit < 0L) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Reducer simplifier is disabled for tiny inputs: {0}", //$NON-NLS-1$
                        job.getJobName()));
            }
            return;
        }
        long estimated = StageInputDriver.estimateInputSize(job);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Reducer simplifier: job={0}, tiny-limit={1}, estimated={2}", //$NON-NLS-1$
                    job.getJobName(),
                    limit,
                    estimated));
        }
        if (estimated < 0L || estimated > limit) {
            return;
        }

        LOG.info(MessageFormat.format(
                "The number of reduce task ({0}) is configured: {1}->{2}",
                job.getJobName(),
                job.getNumReduceTasks(),
                TASKS_TINY));

        job.setNumReduceTasks(TASKS_TINY);
    }
}
