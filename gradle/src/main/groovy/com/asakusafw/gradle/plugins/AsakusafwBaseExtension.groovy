/*
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * An extension object for the Asakusa features.
 * This is only for internal use.
 * @since 0.8.0
 * @version 0.9.0
 */
class AsakusafwBaseExtension {

    /**
     * The plug-ins version.
     */
    String pluginVersion

    /**
     * The default framework version.
     */
    String frameworkVersion

    /**
     * The default Java version.
     */
    String javaVersion

    /**
     * The default Gradle version.
     */
    String gradleVersion

    /**
     * The default embedded libraries directory path (relative from the project root).
     */
    String embeddedLibsDirectory

    /**
     * The default SLF4J version.
     */
    String slf4jVersion

    /**
     * The default Logback version.
     */
    String logbackVersion

    /**
     * The default JSCH version.
     */
    String jschVersion

    /**
     * The default GSON version.
     */
    String gsonVersion

    /**
     * The default HTTP Client version.
     */
    String httpClientVersion

    /**
     * The default Commons CLI version.
     */
    String commonsCliVersion

    /**
     * The default Commons Codec version.
     */
    String commonsCodecVersion

    /**
     * The default Commons IO version.
     */
    String commonsIoVersion

    /**
     * The default Commons Lang version.
     */
    String commonsLangVersion

    /**
     * The default Commons Logging version.
     */
    String commonsLoggingVersion

    /**
     * The default Hive version.
     */
    String hiveVersion
}
