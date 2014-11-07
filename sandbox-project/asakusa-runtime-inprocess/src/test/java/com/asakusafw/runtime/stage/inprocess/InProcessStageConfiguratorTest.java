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

import static com.asakusafw.runtime.stage.inprocess.InProcessStageConfigurator.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ServiceLoader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.stage.StageConfigurator;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;

/**
 * Test for {@link InProcessStageConfigurator}.
 */
public class InProcessStageConfiguratorTest {

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Job job = newJob();
        Configuration conf = job.getConfiguration();

        conf.setLong(KEY_LIMIT, 100);
        new Mock(100).configure(job);
        assertThat(conf.get(StageConstants.PROP_JOB_RUNNER), is(notNullValue()));
    }

    /**
     * disabled.
     * @throws Exception if failed
     */
    @Test
    public void disabled() throws Exception {
        Job job = newJob();
        Configuration conf = job.getConfiguration();
        Assume.assumeThat(conf.get(KEY_LIMIT), is(nullValue()));

        new Mock(100).configure(job);
        assertThat(conf.get(StageConstants.PROP_JOB_RUNNER), is(nullValue()));
    }

    /**
     * input size is exceeded.
     * @throws Exception if failed
     */
    @Test
    public void large() throws Exception {
        Job job = newJob();
        Configuration conf = job.getConfiguration();

        conf.setLong(KEY_LIMIT, 100);
        new Mock(101).configure(job);
        assertThat(conf.get(StageConstants.PROP_JOB_RUNNER), is(nullValue()));
    }

    /**
     * activate alternative properties.
     * @throws Exception if failed
     */
    @Test
    public void activate_properties() throws Exception {
        Job job = newJob();
        Configuration conf = job.getConfiguration();

        conf.setLong(KEY_LIMIT, 100);
        conf.set(KEY_PREFIX_REPLACE + "com.example.testing", "YES!");
        new Mock(100).configure(job);
        assertThat(conf.get("com.example.testing"), is("YES!"));
    }

    /**
     * activate alternative properties.
     * @throws Exception if failed
     */
    @Test
    public void activate_properties_skip() throws Exception {
        Job job = newJob();
        Configuration conf = job.getConfiguration();

        conf.setLong(KEY_LIMIT, 100);
        conf.set(KEY_PREFIX_REPLACE + "com.example.testing", "YES!");
        new Mock(1000).configure(job);
        assertThat(conf.get("com.example.testing"), is(not("YES!")));
    }

    /**
     * SPI test.
     */
    @Test
    public void spi() {
        boolean found = false;
        for (StageConfigurator c : ServiceLoader.load(StageConfigurator.class)) {
            if (c instanceof InProcessStageConfigurator) {
                found = true;
                break;
            }
        }
        assertThat(found, is(true));
    }

    private Job newJob() {
        try {
            Job job = JobCompatibility.newJob(new ConfigurationProvider().newInstance());
            Assume.assumeThat(job.getConfiguration().get(StageConstants.PROP_JOB_RUNNER), is(nullValue()));
            job.setJobName("testing");
            return job;
        } catch (IOException e) {
            Assume.assumeNoException(e);
            throw new AssertionError(e);
        }
    }

    private static final class Mock extends InProcessStageConfigurator {

        private final long size;

        public Mock(long size) {
            this.size = size;
        }

        @Override
        long getEstimatedJobSize(Job job) throws InterruptedException {
            return size;
        }
    }
}
