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
package com.asakusafw.runtime.stage;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.MRConfig;

/**
 * Utilities for MapReduce stages.
 * @since 0.9.0
 */
public final class StageUtil {

    static final Log LOG = LogFactory.getLog(StageUtil.class);

    private StageUtil() {
        return;
    }

    /**
     * Returns whether the current job is on the local mode or not.
     * @param context the current context
     * @return {@code true} if the target job is running on the local mode, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.6.2
     */
    public static boolean isLocalMode(JobContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "{0}={1}", //$NON-NLS-1$
                    MRConfig.FRAMEWORK_NAME,
                    context.getConfiguration().get(MRConfig.FRAMEWORK_NAME)));
        }
        String name = context.getConfiguration().get(MRConfig.FRAMEWORK_NAME, MRConfig.LOCAL_FRAMEWORK_NAME);
        return name.equals(MRConfig.LOCAL_FRAMEWORK_NAME);
    }
}
