package com.asakusafw.gradle.plugins.internal

import org.gradle.api.*

class AsakusafwInternalPluginConvention {
    DependencyConfiguration dep;

    AsakusafwInternalPluginConvention() {
        dep = new DependencyConfiguration()
    }
    /**
     * Internal dependency settings
     */
    class DependencyConfiguration {
        String slf4jVersion
        String logbackVersion
        String jschVersion
        String gsonVersion
        String httpClientVersion
        String commonsCliVersion
        String commonsCodecVersion
        String commonsIoVersion
        String commonsLangVersion
        String commonsLoggingVersion

        String embeddedLibsDirectory

        DependencyConfiguration() {
            slf4jVersion = '1.7.5'
            logbackVersion = '1.0.12'
            jschVersion = '0.1.49'
            gsonVersion = '2.2.3'
            httpClientVersion = '4.2.5'
            commonsCliVersion = '1.2'
            commonsIoVersion = '2.4'
            commonsLangVersion = '2.6'
            commonsCodecVersion = '1.8'
            commonsLoggingVersion = '1.1.1'

            embeddedLibsDirectory = "src/main/libs"
        }
    }
}