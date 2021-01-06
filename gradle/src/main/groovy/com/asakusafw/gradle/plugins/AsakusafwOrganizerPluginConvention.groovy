/*
 * Copyright 2011-2021 Asakusa Framework Team.
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

import org.gradle.api.NamedDomainObjectContainer

import com.asakusafw.gradle.assembly.AsakusafwAssembly

/**
 * Convention class for {@link AsakusafwOrganizerPlugin}.
 * @since 0.5.2
 * @version 0.10.0
 */
class AsakusafwOrganizerPluginConvention {

    /**
     * Asakusa Framework version (read only).
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> Asakusa Framework Core libraries version </dd>
     * </dl>
     * @deprecated use {@code asakusafwOrganizer.core.version} instead
     */
    @Deprecated
    String asakusafwVersion

    /**
     * Working directory path prefix of framework organizer.
     * <dl>
     *   <dt> Default value: </dt>
     *     <dd> <code>"${project.buildDir}/asakusafw-assembly"</code> </dd>
     * </dl>
     */
    String assembleDir

    /**
     * Core settings.
     * @since 0.9.0
     */
    CoreConfiguration core

    /**
     * Hadoop settings.
     * @since 0.10.0
     */
    HadoopConfiguration hadoop

    /**
     * Direct I/O settings.
     * @since 0.7.0
     */
    DirectIoConfiguration directio

    /**
     * WindGate settings.
     * @since 0.7.0
     */
    WindGateConfiguration windgate

    /**
     * Hive settings.
     * @since 0.7.0
     */
    HiveConfiguration hive

    /**
     * YAESS settings.
     * @since 0.7.0
     */
    YaessConfiguration yaess

    /**
     * Batch applications settings.
     * @since 0.7.0
     */
    BatchappsConfiguration batchapps

    /**
     * Test driver settings.
     * @since 0.7.0
     */
    TestingConfiguration testing

    /**
     * Asakusa Framework extension settings.
     * @since 0.7.1
     */
    ExtensionConfiguration extension

    /**
     * Profiles for organizing framework package.
     * @since 0.7.0
     */
    NamedDomainObjectContainer<AsakusafwOrganizerProfile> profiles

    /**
     * Custom framework files.
     */
    final AsakusafwAssembly assembly = new AsakusafwAssembly("assembly")

    /**
     * Asakusa Framework organizer core settings.
     * @since 0.9.0
     */
    static class CoreConfiguration {
        // no special members
    }

    /**
     * Hadoop settings for the Asakusa Framework organizer.
     * @since 0.10.0
     */
    static class HadoopConfiguration {

        /**
         * Configuration whether embedded Hadoop libraries enabled or not.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean embed

        /**
         * The Hadoop version.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> (Asakusa Framework default version) </dd>
         * </dl>
         */
        String version
    }

    /**
     * Direct I/O settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class DirectIoConfiguration {

        /**
         * Configuration whether Direct I/O features are enabled or not.
         * Direct I/O facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean enabled
    }

    /**
     * WindGate settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class WindGateConfiguration {

        /**
         * Configuration whether WindGate features are enabled or not.
         * WindGate facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean enabled

        /**
         * Configuration whether WindGate SSH feature is enabled or not.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean sshEnabled

        /**
         * Configuration whether WindGate retryable feature is enabled or not.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean retryableEnabled
    }

    /**
     * Direct I/O Hive settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class HiveConfiguration {

        /**
         * Configuration whether Direct I/O Hive features are enabled or not.
         * Direct I/O Hive facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean enabled

        /**
         * Default libraries for Direct I/O Hive runtime.
         * Clients should not modify this property, and use {@link #libraries} instead.
         */
        List<Object> defaultLibraries = []

        /**
         * Libraries for Direct I/O Hive runtime.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> Framework default hive version </dd>
         * </dl>
         */
        List<Object> libraries

        /**
         * Returns libraries for Direct I/O Hive runtime.
         * @return the libraries
         */
        List<Object> getLibraries() {
            if (this.@libraries == null) {
                return Collections.unmodifiableList(getDefaultLibraries())
            }
            return Collections.unmodifiableList(this.@libraries)
        }

        /**
         * Sets libraries for Direct I/O Hive runtime.
         * @param libraries the libraries
         */
        void setLibraries(Object... libraries) {
            // copy on write
            List<Object> list = new ArrayList<>()
            list.addAll(libraries.flatten())
            this.@libraries = list
        }
    }

    /**
     * YAESS settings for the Asakusa Framework organizer.
     * @since 0.7.0
     * @version 0.8.0
     */
    static class YaessConfiguration {

        /**
         * Configuration whether YAESS features are enabled or not.
         * YAESS facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean enabled

        /**
         * Configuration whether YAESS Hadoop bridge is enabled or not.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean hadoopEnabled

        /**
         * Configuration whether YAESS extra tools is enabled or not.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean toolsEnabled

        /**
         * Configuration whether YAESS JobQueue client is enabled or not.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean jobqueueEnabled

        /**
         * Configuration whether YAESS iterative extension is enabled or not.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         * @since 0.8.0
         */
        boolean iterativeEnabled
    }

    /**
     * Batch application settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class BatchappsConfiguration {

        /**
         * Configuration whether batch applications are included or not.
         * If it is not enabled, the deployment archive will not contain any batch applications.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} - except 'dev' profile </dd>
         *     <dd> {@code false} - for 'dev profile </dd>
         * </dl>
         */
        boolean enabled
    }

    /**
     * Test driver settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class TestingConfiguration {

        /**
         * Configuration whether test driver features are enabled or not.
         * Testing facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} - except 'dev' profile </dd>
         *     <dd> {@code true} - for 'dev profile </dd>
         * </dl>
         */
        boolean enabled
    }

    /**
     * Asakusa Framework extension settings for the Asakusa Framework organizer.
     * @since 0.7.1
     */
    static class ExtensionConfiguration {

        /**
         * Default libraries for Asakusa Framework extensions.
         * Clients should not modify this property, and use {@link #libraries} instead.
         */
        List<Object> defaultLibraries = []

        /**
         * Libraries for Asakusa Framework extensions.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> {@code []} </dd>
         * </dl>
         */
        List<Object> libraries

        /**
         * Returns libraries for Asakusa Framework extensions.
         * @return the libraries
         */
        List<Object> getLibraries() {
            if (this.@libraries == null) {
                return Collections.unmodifiableList(getDefaultLibraries())
            }
            return Collections.unmodifiableList(this.@libraries)
        }

        /**
         * Sets libraries for Asakusa Framework extensions.
         * @param libraries the libraries
         */
        void setLibraries(Object... libraries) {
            // copy on write
            List<Object> list = new ArrayList<>()
            list.addAll(libraries.flatten())
            this.@libraries = list
        }
    }

    @Override
    String toString() {
        // explicitly invoke meta-method
        def delegate = this.metaClass.getMetaMethod('toStringDelegate')
        if (delegate) {
            return delegate.invoke(this)
        }
        return toStringDelegate()
    }

    String toStringDelegate() {
        return super.toString()
    }
}
