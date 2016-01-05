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
package com.asakusafw.testdriver.inprocess.optimize;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.asakusafw.runtime.stage.inprocess.InProcessStageConfigurator;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;
import com.asakusafw.testdriver.inprocess.InProcessJobExecutor;

/**
 * Test for {@link InProcessEnvironmentOptimizer}.
 */
public class InProcessEnvironmentOptimizerTest {

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
            InProcessJobExecutor.getGlobalSettings().reset();
            escape.remove(InProcessEnvironmentOptimizer.KEY_FEATURE_ENABLE);
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
            if (configurator instanceof InProcessEnvironmentOptimizer) {
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
        new InProcessEnvironmentOptimizer().configure();
        assertThat(isInstalled(), is(true));
    }

    /**
     * Configuration test (explicitly enabled).
     */
    @Test
    public void configure_enabled() {
        System.setProperty(InProcessEnvironmentOptimizer.KEY_FEATURE_ENABLE, "true");
        new InProcessEnvironmentOptimizer().configure();
        assertThat(isInstalled(), is(true));
    }

    /**
     * Configuration test (explicitly disabled).
     */
    @Test
    public void configure_disabled() {
        System.setProperty(InProcessEnvironmentOptimizer.KEY_FEATURE_ENABLE, "false");
        new InProcessEnvironmentOptimizer().configure();
        assertThat(isInstalled(), is(false));
    }

    /**
     * Conflict job runner.
     */
    @Test
    public void configure_conflict() {
        getGlobalProperties().put(InProcessStageConfigurator.KEY_FORCE, "false");
        new InProcessEnvironmentOptimizer().configure();
        assertThat(isInstalled(), is(false));
    }

    private boolean isInstalled() {
        String value = getGlobalProperties().get(InProcessStageConfigurator.KEY_FORCE);
        return value != null && value.equals(String.valueOf(true));
    }

    private Map<String, String> getGlobalProperties() {
        return InProcessJobExecutor.getGlobalSettings().getProperties();
    }
}
