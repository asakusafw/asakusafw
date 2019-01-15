/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.util.Collections;
import java.util.Map;

/**
 * The context information of tests.
 * @since 0.2.2
 */
public interface TestContext {

    /**
     * Returns the class loader to load testing peripherals.
     * @return the class loader
     */
    ClassLoader getClassLoader();

    /**
     * Returns the environment variables.
     * @return the environment variables
     * @since 0.2.4
     */
    Map<String, String> getEnvironmentVariables();

    /**
     * Returns the user arguments.
     * @return the user arguments
     */
    Map<String, String> getArguments();

    /**
     * Empty {@link TestContext}.
     * @since 0.2.2
     */
    class Empty implements TestContext {

        @Override
        public Map<String, String> getArguments() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, String> getEnvironmentVariables() {
            return System.getenv();
        }

        @Override
        public ClassLoader getClassLoader() {
            return ClassLoader.getSystemClassLoader();
        }
    }
}
