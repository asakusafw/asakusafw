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

/**
 * Convention class for {@link AsakusafwOrganizerPlugin}.
 * @since 0.5.2
 * @version 0.6.1
 */
class AsakusafwOrganizerPluginConvention {

    /**
     * Asakusa Framework Version.
     * This property must be specified in project configuration
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> pom.xml - {@code properties/asakusafw.version} </dd>
     *   <dt> Default value: </dt>
     *     <dd> N/A </dd>
     * </dl>
     */
    String asakusafwVersion

    /**
     * Working directory of Framework Organizer.
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> N/A </dd>
     *   <dt> Default value: </dt>
     *     <dd> <code>"${project.buildDir}/asakusafw-assembly"</code> </dd>
     * </dl>
     */
    String assembleDir

    /**
     * ThunderGate Settings.
     * @since 0.6.1
     */
    ThunderGateConfiguration thundergate

    /**
     * ThunderGate settings for the Asakusa Framework organizer.
     * @since 0.6.1
     */
    static class ThunderGateConfiguration {

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
        boolean enabled

        /**
         * The ThunderGate default name using in the development environment (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.database.target} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} </dd>
         * </dl>
         */
        String target

        /**
         * Sets the ThunderGate default target name using in the development environment.
         * @param value the target name
         */
        void setTarget(String value) {
            this.target = value
            if (value != null && !isEnabled()) {
                setEnabled(true)
            }
        }
    }
}
