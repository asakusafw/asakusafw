/*
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
package com.asakusafw.gradle.plugins

import org.gradle.api.*
import org.gradle.util.ConfigureUtil

/**
 * Convention class for {@link AsakusafwOrganizerPlugin}.
 * @since 0.5.2
 * @version 0.6.1
 */
class AsakusafwOrganizerPluginConvention {

    final Project project

    /**
     * Asakusa Framework Version.
     * This property must be specified in project configuration
     */
    String asakusafwVersion

    /**
     * Working directory of Framework Organizer.
     */
    String assembleDir

    /**
     * ThunderGate Settings.
     * @since 0.6.1
     */
    ThunderGateConfiguration thundergate

    AsakusafwOrganizerPluginConvention(final Project project) {
        this.project = project
        assembleDir = "${project.buildDir}/asakusafw-assembly"
        thundergate = new ThunderGateConfiguration(project)
    }

    def thundergate(Closure configureClousure) {
        ConfigureUtil.configure(configureClousure, thundergate)
    }

    /**
     * ThunderGate Settings.
     * @since 0.6.1
     */
    class ThunderGateConfiguration {

        /**
         * Configuration whether ThunderGate is enabled or not.
         * ThunderGate facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.database.enabled} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean enabled = false

        /**
         * The ThunderGate default name using in the development environment (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.database.target} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} </dd>
         * </dl>
         */
        String target = null

        ThunderGateConfiguration(Project project) {
            //NOP
        }

        /**
         * Returns whether ThunderGate is enabled or not.
         * @return {@code true} if ThunderGate is enabled, otherwise {@code false}
         */
        public boolean isEnabled() {
            return this.enabled || this.getTarget() != null
        }

        /**
         * Sets whether ThunderGate is enabled or not.
         * @param value {@code true} to enable ThunderGate
         * @return {@code this}
         */
        ThunderGateConfiguration enabled(boolean value = true) {
            this.enabled = value
            return this
        }

        /**
         * Sets the ThunderGate default name using in the development environment.
         * @param value the value to set
         * @return {@code this}
         */
        ThunderGateConfiguration target(String value) {
            this.target = value
            return this
        }
    }
}
