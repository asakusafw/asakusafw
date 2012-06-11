/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.file;

import org.junit.Assume;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;

/**
 * Skips tests if Hadoop is not installed.
 */
class HadoopEnvironmentChecker extends TestWatchman {

    static Logger LOG = LoggerFactory.getLogger(HadoopEnvironmentChecker.class);

    @Override
    public void starting(FrameworkMethod method) {
        if (ConfigurationProvider.findHadoopCommand() == null) {
            LOG.warn("hadoop command is not found. skip {}.{}",
                    method.getClass().getName(),
                    method.getName());
            Assume.assumeTrue(false);
        }
    }
}
