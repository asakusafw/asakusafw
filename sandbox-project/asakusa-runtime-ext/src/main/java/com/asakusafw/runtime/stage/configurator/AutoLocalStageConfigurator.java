/**
 * Copyright 2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.configurator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.asakusafw.runtime.stage.StageConfigurator;
import com.asakusafw.runtime.stage.input.StageInputDriver;

/**
 * Configures auto stage localize.
 * @since 0.6.0
 */
public class AutoLocalStageConfigurator extends StageConfigurator {

    static final Log LOG = LogFactory.getLog(AutoLocalStageConfigurator.class);

    private static final String KEY_LIMIT = "com.asakusafw.autolocal.limit";

    private static final String KEY_TEMPORARY_DIRECTORY = "com.asakusafw.autolocal.dir.path";

    private static final String KEY_DIRECTORY_QUALIFIER = "com.asakusafw.autolocal.dir.qualifier";

    private static final String KEY_JOBTRACKER = "mapred.job.tracker";

    private static final String KEY_LOCAL_DIR = "mapred.local.dir";

    private static final String KEY_STAGING_DIR = "mapreduce.jobtracker.staging.root.dir";

    private static final String DEFAULT_JOBTRACKER = "local";

    private static final String[] KEYS_REWRITE_TARGET = {
        KEY_JOBTRACKER,
        KEY_LOCAL_DIR,
        KEY_STAGING_DIR,
    };

    @Override
    public void configure(Job job) throws IOException, InterruptedException {
        if (isLocal(job)) {
            return;
        }
        Configuration conf = job.getConfiguration();
        long limit = conf.getLong(KEY_LIMIT, -1L);
        if (limit < 0L) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Auto stage localize is disabled: {0}",
                        job.getJobName()));
            }
            return;
        }
        if (isProtected(job)) {
            return;
        }
        long estimated = StageInputDriver.estimateInputSize(job);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Auto stage localize: job={0}, limit={1}, estimated={2}",
                    job.getJobName(),
                    limit,
                    estimated));
        }
        if (estimated < 0L || estimated > limit) {
            return;
        }

        LOG.info(MessageFormat.format(
                "The job \"{0}\" will run in local mode",
                job.getJobName()));

        localize(job);
    }

    private boolean isLocal(Job job) {
        Configuration conf = job.getConfiguration();
        if (conf.get(KEY_JOBTRACKER, DEFAULT_JOBTRACKER).equals(DEFAULT_JOBTRACKER)) {
            return true;
        }
        return false;
    }

    private boolean isProtected(Job job) {
        Configuration conf = job.getConfiguration();
        Set<?> finalParameters = null;
        try {
            Field field = Configuration.class.getDeclaredField("finalParameters");
            field.setAccessible(true);
            Object value = field.get(conf);
            if (value instanceof Set<?>) {
                finalParameters = (Set<?>) value;
            }
        } catch (Exception e) {
            LOG.debug("Exception occurred while inspecting configuration", e);
        }
        if (finalParameters == null) {
            LOG.warn("Auto stage localize does not support the current Hadoop version");
            return true;
        }
        for (String key : KEYS_REWRITE_TARGET) {
            if (finalParameters.contains(key)) {
                LOG.warn(MessageFormat.format(
                        "Auto stage localize requires that configuration \"{0}\" is not final",
                        key));
                return true;
            }
        }
        return false;
    }

    private void localize(Job job) {
        Configuration conf = job.getConfiguration();

        // reset job-tracker
        conf.set(KEY_JOBTRACKER, DEFAULT_JOBTRACKER);

        // replace local directories
        String tmpDir = conf.get(KEY_TEMPORARY_DIRECTORY, "");
        if (tmpDir.isEmpty()) {
            String name = System.getProperty("user.name", "asakusa");
            tmpDir = String.format("/tmp/hadoop-%s/autolocal", name);
        } else if (tmpDir.length() > 1 && tmpDir.endsWith("/")) {
            tmpDir = tmpDir.substring(0, tmpDir.length() - 1);
        }
        if (conf.getBoolean(KEY_DIRECTORY_QUALIFIER, true)) {
            String qualifier = UUID.randomUUID().toString();
            tmpDir = String.format("%s/%s", tmpDir, qualifier);
        }
        LOG.info(MessageFormat.format(
                "Substituting temporary dir: job={0}, target={1}",
                job.getJobName(),
                tmpDir));
        conf.set(KEY_LOCAL_DIR, tmpDir + "/mapred/local");
        conf.set(KEY_STAGING_DIR, tmpDir + "/mapred/staging");
    }
}
