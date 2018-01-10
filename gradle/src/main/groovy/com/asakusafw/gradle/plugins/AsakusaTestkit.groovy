/*
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.gradle.plugins

import org.gradle.api.Project

/**
 * An abstract implementation of Asakusa Testkit.
 * @since 0.9.0
 */
interface AsakusaTestkit {

    /**
     * Returns the testkit name.
     * @return the testkit name
     */
    String getName()

    /**
     * Returns the testkit priority.
     * @return the testkit priority, or {@code -1} if this testkit must not be selected in default
     */
    int getPriority()

    /**
     * Applies this testkit into the given project.
     * @param project the target project
     */
    void apply(Project project)
}
