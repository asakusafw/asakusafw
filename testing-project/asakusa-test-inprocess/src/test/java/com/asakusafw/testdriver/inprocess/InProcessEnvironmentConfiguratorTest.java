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
package com.asakusafw.testdriver.inprocess;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Properties;
import java.util.ServiceLoader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;

/**
 * Test for {@link InProcessEnvironmentConfigurator}.
 */
public class InProcessEnvironmentConfiguratorTest {

    /**
     * Keeps system properties.
     */
    @Rule
    public final ExternalResource propertiesKeeper = new ExternalResource() {
        private Properties props;
        @Override
        public void before() {
            props = System.getProperties();
            Properties escape = new Properties();
            escape.putAll(props);

            // reset properties
            escape.remove(InProcessEnvironmentConfigurator.KEY_FEATURE_ENABLE);
            escape.remove(TestDriverContext.KEY_JOB_EXECUTOR_FACTORY);
            System.setProperties(escape);
        }
        @Override
        public void after() {
            if (props != null) {
                System.setProperties(props);
            }
        }
    };

    /**
     * Check registered.
     */
    @Test
    public void spi() {
        for (TestingEnvironmentConfigurator configurator : ServiceLoader.load(TestingEnvironmentConfigurator.class)) {
            if (configurator instanceof InProcessEnvironmentConfigurator) {
                return;
            }
        }
        throw new AssertionError();
    }

    /**
     * Configuration test.
     */
    @Test
    public void configure_default() {
        new InProcessEnvironmentConfigurator().configure();
        assertThat(getFactoryName(), is(InProcessJobExecutorFactory.class.getName()));
    }

    /**
     * Configuration test (explicitly enabled).
     */
    @Test
    public void configure_enabled() {
        System.setProperty(InProcessEnvironmentConfigurator.KEY_FEATURE_ENABLE, "true");
        new InProcessEnvironmentConfigurator().configure();
        assertThat(getFactoryName(), is(InProcessJobExecutorFactory.class.getName()));
    }

    /**
     * Configuration test (explicitly disabled).
     */
    @Test
    public void configure_disabled() {
        System.setProperty(InProcessEnvironmentConfigurator.KEY_FEATURE_ENABLE, "false");
        new InProcessEnvironmentConfigurator().configure();
        assertThat(getFactoryName(), is(nullValue()));
    }

    /**
     * Conflict factory name.
     */
    @Test
    public void configure_conflict() {
        System.setProperty(TestDriverContext.KEY_JOB_EXECUTOR_FACTORY, "testing");
        new InProcessEnvironmentConfigurator().configure();
        assertThat(getFactoryName(), is("testing"));
    }

    private String getFactoryName() {
        return System.getProperty(TestDriverContext.KEY_JOB_EXECUTOR_FACTORY);
    }
}
