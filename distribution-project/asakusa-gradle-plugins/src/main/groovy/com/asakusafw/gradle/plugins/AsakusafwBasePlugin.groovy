package com.asakusafw.gradle.plugins

import com.asakusafw.gradle.plugins.internal.AsakusafwInternalPluginConvention
import org.gradle.api.*

class AsakusafwBasePlugin implements Plugin<Project> {

    private Project project
    private AntBuilder ant

    void apply(Project project) {
        this.project = project
        this.ant = project.ant

        configureProject()
    }

    private void configureProject() {
        configureExtentionProperties()
        configureRepositories()
    }

    private void configureExtentionProperties() {
        project.extensions.create('asakusafwInternal', AsakusafwInternalPluginConvention)
    }

    private void configureRepositories() {
        project.repositories {
            mavenCentral()
            maven { url "http://asakusafw.s3.amazonaws.com/maven/releases" }
            maven { url "http://asakusafw.s3.amazonaws.com/maven/snapshots" }
        }
    }
}

