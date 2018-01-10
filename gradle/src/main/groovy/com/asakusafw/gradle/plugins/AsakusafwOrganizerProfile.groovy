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

import com.asakusafw.gradle.assembly.AsakusafwAssembly
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.BatchappsConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.CoreConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.DirectIoConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ExtensionConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HadoopConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HiveConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.TestingConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.WindGateConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.YaessConfiguration

/**
 * Represents an Asakusa Framework organization profile.
 * @since 0.7.0
 * @version 0.10.0
 */
class AsakusafwOrganizerProfile {

    /**
     * The profile name.
     */
    final String name

    /**
     * Asakusa Framework Version (read only).
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> {@code project.asakusafwOrganizer.asakusafwVersion} </dd>
     * </dl>
     * @deprecated use {@code asakusafwOrganizer.core.version} instead
     */
    @Deprecated
    String asakusafwVersion

    /**
     * Working directory of Framework Organizer.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> <code>"${project.asakusafwOrganizer.assembleDir}-${name}"</code> </dd>
     * </dl>
     */
    String assembleDir

    /**
     * The final archive name (should be end with {@code .tar.gz}).
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> <code>"asakusafw-${project.name}-${name}.tar.gz"</code> - except 'prod' profile </dd>
     *     <dd> <code>"asakusafw-${project.name}.tar.gz"</code> - for 'prod' profile </dd>
     * </dl>
     */
    String archiveName

    /**
     * Hadoop settings.
     * @since 0.10.0
     */
    HadoopConfiguration hadoop

    /**
     * Core settings.
     * @since 0.9.0
     */
    CoreConfiguration core

    /**
     * Direct I/O settings.
     */
    DirectIoConfiguration directio

    /**
     * WindGate settings.
     */
    WindGateConfiguration windgate

    /**
     * Hive settings.
     */
    HiveConfiguration hive

    /**
     * YAESS settings.
     */
    YaessConfiguration yaess

    /**
     * Batchapps settings.
     */
    BatchappsConfiguration batchapps

    /**
     * Testing settings.
     */
    TestingConfiguration testing

    /**
     * Asakusa Framework extension settings.
     * @since 0.7.1
     */
    ExtensionConfiguration extension

    /**
     * Core framework files.
     * Clients should not edit this.
     */
    final AsakusafwAssembly components

    /**
     * Custom framework files.
     */
    final AsakusafwAssembly assembly

    /**
     * Creates a new instance.
     * @param name the profile name
     */
    AsakusafwOrganizerProfile(String name) {
        this.name = name
        this.components = new AsakusafwAssembly("${name}.components")
        this.assembly = new AsakusafwAssembly("${name}.assembly")
    }

    @Override
    String toString() {
        return "Profile(${name})"
    }
}
