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
package com.asakusafw.gradle.plugins.internal

/**
 * Convention class for plugin internal.
 */
class AsakusafwInternalPluginConvention {
    DependencyConfiguration dep

    AsakusafwInternalPluginConvention() {
        dep = new DependencyConfiguration()
    }
    /**
     * Internal dependency settings.
     */
    class DependencyConfiguration {
        String slf4jVersion
        String logbackVersion
        String log4jVersion
        String jschVersion
        String gsonVersion
        String httpClientVersion
        String commonsCliVersion
        String commonsCodecVersion
        String commonsConfigurationVersion
        String commonsIoVersion
        String commonsLangVersion
        String commonsLoggingVersion
        String mysqlConnectorJavaVersion
        String hiveArtifact
        String snappyArtifact

        String embeddedLibsDirectory

        DependencyConfiguration() {
            slf4jVersion = '1.7.5'
            logbackVersion = '1.0.12'
            log4jVersion = '1.2.15'
            jschVersion = '0.1.49'
            gsonVersion = '2.2.3'
            httpClientVersion = '4.2.5'
            commonsCliVersion = '1.2'
            commonsIoVersion = '2.4'
            commonsLangVersion = '2.6'
            commonsCodecVersion = '1.8'
            commonsConfigurationVersion = '1.8'
            commonsLoggingVersion = '1.1.1'
            mysqlConnectorJavaVersion = '5.1.25'
            hiveArtifact = 'org.apache.hive:hive-exec:0.13.1'
            snappyArtifact = 'org.xerial.snappy:snappy-java:1.0.5'

            embeddedLibsDirectory = "src/main/libs"
        }
    }
}