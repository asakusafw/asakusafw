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
package com.asakusafw.runtime.configuration;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;

/**
 * Skips tests if Hadoop is not installed.
 */
public class HadoopEnvironmentChecker extends TestWatcher {

    static final Logger LOG = LoggerFactory.getLogger(HadoopEnvironmentChecker.class);

    final boolean failOnError;

    /**
     * Creates a new instance.
     * @param failOnError {@code false} to skip test if not installed, {@code true} to fail
     */
    public HadoopEnvironmentChecker(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    protected void starting(Description description) {
        boolean found = ConfigurationProvider.findHadoopCommand() != null;
        if (found) {
            return;
        }
        if (failOnError) {
            Assert.fail("Hadoop command is not found");
        } else {
            LOG.warn(MessageFormat.format(
                    "hadoop command is not found. skip {0}.{1}",
                    description.getTestClass().getName(),
                    description.getMethodName()));
            Assume.assumeTrue(false);
        }
    }
}
