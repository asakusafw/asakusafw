package com.asakusafw.gradle.plugins

import org.gradle.api.*

class AsakusafwOrganizerPluginConvention {
    final Project project

    /**
     * Asakusa Framework Version.
     * This property must be specified in project configuration
     */
    String asakusafwVersion

    /**
     * Working dicrectory of Framework Organizer.
     */
    String assembleDir

    AsakusafwOrganizerPluginConvention(final Project project) {
        this.project = project

        assembleDir = "${project.buildDir}/asakusafw-assembly"
    }
}
