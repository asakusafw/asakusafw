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
package com.asakusafw.runtime.stage.inprocess;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.asakusafw.runtime.mapreduce.simple.SimpleJobRunner;
import com.asakusafw.runtime.stage.StageConfigurator;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.input.ExtremeSplitCombiner;
import com.asakusafw.runtime.stage.input.StageInputDriver;
import com.asakusafw.runtime.stage.input.StageInputFormat;
import com.asakusafw.runtime.stage.resource.StageResourceDriver;

/**
 * Configures {@link SimpleJobRunner}.
 * @since 0.7.1
 */
public class InProcessStageConfigurator extends StageConfigurator {

    static final Log LOG = LogFactory.getLog(InProcessStageConfigurator.class);

    private static final String KEY_PREFIX = "com.asakusafw.inprocess."; //$NON-NLS-1$

    /**
     * Hadoop property key of the max input data size for in-process job execution.
     */
    public static final String KEY_LIMIT = KEY_PREFIX + "limit"; //$NON-NLS-1$

    /**
     * Always enables in-process execution even.
     */
    public static final String KEY_FORCE = KEY_PREFIX + "force"; //$NON-NLS-1$

    /**
     * Activates trailing Hadoop property only if in-process job execution is enabled.
     */
    static final String KEY_PREFIX_REPLACE = KEY_PREFIX + "activate."; //$NON-NLS-1$

    private static final Pattern PATTERN_KEY_REPLACE = Pattern.compile('^' + Pattern.quote(KEY_PREFIX_REPLACE));

    @Override
    public void configure(Job job) throws IOException, InterruptedException {
        if (job.getConfiguration().getBoolean(KEY_FORCE, false)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "force enabled in-process execution: {0}", //$NON-NLS-1$
                        job.getJobName()));
            }
            install(job);
            return;
        }
        long limit = job.getConfiguration().getLong(KEY_LIMIT, -1L);
        if (limit < 0L) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "in-process execution is disabled: {0}", //$NON-NLS-1$
                        job.getJobName()));
            }
            return;
        }
        if (hasCustomJobRunner(job)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "custom job runner is already activated: {0}", //$NON-NLS-1$
                        job.getJobName()));
            }
            return;
        }
        long estimated = getEstimatedJobSize(job);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "estimated input data size for in-process execution: " //$NON-NLS-1$
                    + "job={0}, limit={1}, estimated={2}", //$NON-NLS-1$
                    job.getJobName(),
                    limit,
                    estimated));
        }
        if (estimated < 0L || estimated > limit) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(MessageFormat.format(
                    "enable in-process execution: job={0}, limit={1}, estimated={2}",
                    job.getJobName(),
                    limit,
                    estimated));
        }
        install(job);
    }

    private boolean hasCustomJobRunner(Job job) {
        return job.getConfiguration().get(StageConstants.PROP_JOB_RUNNER) != null;
    }

    long getEstimatedJobSize(Job job) throws InterruptedException {
        long total = 0L;
        total += StageInputDriver.estimateInputSize(job);
        total += StageResourceDriver.estimateResourceSize(job);
        return total;
    }

    private void install(Job job) {
        Configuration conf = job.getConfiguration();
        int prefixLength = KEY_PREFIX_REPLACE.length();
        for (Map.Entry<String, String> entry : conf.getValByRegex(PATTERN_KEY_REPLACE.pattern()).entrySet()) {
            assert entry.getKey().length() >= prefixLength;
            String key = entry.getKey().substring(prefixLength);
            if (key.isEmpty()) {
                continue;
            }
            String value = entry.getValue();
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "activate in-process configuration: {0}=\"{1}\"->\"{2}\"", //$NON-NLS-1$
                        key,
                        conf.get(key, ""), //$NON-NLS-1$
                        value));
            }
            conf.set(key, value);
        }
        conf.set(StageConstants.PROP_JOB_RUNNER, SimpleJobRunner.class.getName());
        StageResourceDriver.setAccessMode(job, StageResourceDriver.AccessMode.DIRECT);
        StageInputFormat.setSplitCombinerClass(job, ExtremeSplitCombiner.class);
    }
}
