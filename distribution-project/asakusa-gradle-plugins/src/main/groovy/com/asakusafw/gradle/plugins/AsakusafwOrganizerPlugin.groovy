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
import org.gradle.api.tasks.bundling.*

import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ThunderGateConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HiveConfiguration

/**
 * Gradle plugin for assembling and Installing Asakusa Framework.
 */
class AsakusafwOrganizerPlugin  implements Plugin<Project> {

    public static final String ASAKUSAFW_ORGANIZER_GROUP = 'Asakusa Framework Organizer'

    private Project project

    void apply(Project project) {
        this.project = project

        project.plugins.apply(AsakusafwBasePlugin.class)

        configureProject()
        defineOrganizerTasks()
    }

    private void configureProject() {
        configureExtentionProperties()
        configureConfigurations()
        configureDependencies()
    }

    private void configureExtentionProperties() {
        AsakusafwOrganizerPluginConvention convention = project.extensions.create('asakusafwOrganizer', AsakusafwOrganizerPluginConvention)
        convention.thundergate = convention.extensions.create('thundergate', ThunderGateConfiguration)
        convention.hive = convention.extensions.create('hive', HiveConfiguration)
        convention.conventionMapping.with {
            asakusafwVersion = { throw new InvalidUserDataException('"asakusafw.asakusafwVersion" must be set') }
            assembleDir = { (String) "${project.buildDir}/asakusafw-assembly" }
        }
        convention.thundergate.conventionMapping.with {
            enabled = { false }
            target = { null }
        }
        convention.hive.libraries.add(project.asakusafwInternal.dep.hiveArtifact + '@jar')
        convention.hive.libraries.add(project.asakusafwInternal.dep.snappyArtifact + '@jar')
    }

    private void configureConfigurations() {
        project.configurations {
            asakusafwCoreDist {
                description = "Distribution contents of Asakusa Framework core modules."
            }
            asakusafwCoreLib {
                description = "Distribution libraries of Asakusa Framework core modules."
            }

            asakusafwDirectIoDist {
                description = "Distribution contents of Asakusa Framework Direct I/O modules."
            }
            asakusafwDirectIoLib {
                description = "Distribution libraries of Asakusa Framework Direct I/O modules."
            }

            asakusafwYaessDist {
                description = "Distribution contents of Asakusa Framework YAESS modules."
            }
            asakusafwYaessLib {
                description = "Distribution libraries of Asakusa Framework YAESS modules."
            }
            asakusafwYaessPlugin {
                description = "Distribution default plugin sets of YAESS."
            }
            asakusafwYaessTool {
                description = "Distribution libraries of Asakusa Framework YAESS tools."
            }

            asakusafwWindGateDist {
                description = "Distribution contents of Asakusa Framework WindGate tools."
            }
            asakusafwWindGateLib {
                description = "Distribution libraries of Asakusa Framework WindGate modules."
            }
            asakusafwWindGatePlugin {
                description = "Distribution default plugin sets of WindGate."
            }
            asakusafwWindGateSshLib {
                description = "Distribution libraries of Asakusa Framework WindGate-SSH modules."
            }

            asakusafwDevelopmentDist {
                description = "Distribution contents of Asakusa Framework development tools."
            }

            asakusafwThunderGateDist {
                description = "Distribution contents of Asakusa Framework ThunderGate tools."
            }
            asakusafwThunderGateCoreLib {
                description = "Distribution libraries of Asakusa Framework ThunderGate modules for core runtime."
                transitive = false
            }
            asakusafwThunderGateLib {
                description = "Distribution libraries of Asakusa Framework ThunderGate modules."
            }

            asakusafwOperationDist {
                description = "Distribution contents of Asakusa Framework operation tools."
            }
            asakusafwOperationLib {
                description = "Distribution libraries of Asakusa Framework operation tools."
            }

            asakusafwYaessJobQueuePluginLib {
                description = "Plugin distribution libraries of YAESS JobQueue."
            }
            asakusafwWindGateRetryablePluginLib {
                description = "Plugin distribution libraries of WindGate retryable."
            }
            asakusafwDirectIoHivePluginLib {
                description = "Plugin distribution libraries of Direct I/O Hive."
            }
        }
    }

    private void configureDependencies() {

        project.afterEvaluate {
            project.dependencies {
                asakusafwCoreDist "com.asakusafw:asakusa-runtime-configuration:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwCoreLib "com.asakusafw:asakusa-runtime-all:${project.asakusafwOrganizer.asakusafwVersion}:lib@jar"

                asakusafwDirectIoDist "com.asakusafw:asakusa-directio-tools:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwDirectIoLib "com.asakusafw:asakusa-directio-tools:${project.asakusafwOrganizer.asakusafwVersion}@jar"

                asakusafwYaessDist "com.asakusafw:asakusa-yaess-core:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwYaessDist "com.asakusafw:asakusa-yaess-bootstrap:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwYaessDist "com.asakusafw:asakusa-yaess-tools:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwYaessLib "com.asakusafw:asakusa-yaess-bootstrap:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessLib "com.asakusafw:asakusa-yaess-core:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessLib "commons-cli:commons-cli:${project.asakusafwInternal.dep.commonsCliVersion}@jar"
                asakusafwYaessLib "ch.qos.logback:logback-classic:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwYaessLib "ch.qos.logback:logback-core:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwYaessLib "org.slf4j:slf4j-api:${project.asakusafwInternal.dep.slf4jVersion}@jar"
                asakusafwYaessLib "org.slf4j:jul-to-slf4j:${project.asakusafwInternal.dep.slf4jVersion}@jar"
                asakusafwYaessPlugin "com.asakusafw:asakusa-yaess-flowlog:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessPlugin "com.asakusafw:asakusa-yaess-jsch:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessPlugin "com.asakusafw:asakusa-yaess-multidispatch:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessPlugin "com.asakusafw:asakusa-yaess-paralleljob:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessPlugin "com.jcraft:jsch:${project.asakusafwInternal.dep.jschVersion}@jar"
                asakusafwYaessTool "com.asakusafw:asakusa-yaess-tools:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessTool "com.google.code.gson:gson:${project.asakusafwInternal.dep.gsonVersion}@jar"

                asakusafwWindGateDist "com.asakusafw:asakusa-windgate-plugin:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwWindGateDist "com.asakusafw:asakusa-windgate-hadoopfs:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwWindGateLib "com.asakusafw:asakusa-windgate-bootstrap:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwWindGateLib "com.asakusafw:asakusa-windgate-core:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwWindGateLib "ch.qos.logback:logback-classic:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwWindGateLib "ch.qos.logback:logback-core:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwWindGateLib "org.slf4j:slf4j-api:${project.asakusafwInternal.dep.slf4jVersion}@jar"
                asakusafwWindGateLib "org.slf4j:jul-to-slf4j:${project.asakusafwInternal.dep.slf4jVersion}@jar"
                asakusafwWindGateLib "com.jcraft:jsch:${project.asakusafwInternal.dep.jschVersion}@jar"
                asakusafwWindGatePlugin "com.asakusafw:asakusa-windgate-hadoopfs:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwWindGatePlugin "com.asakusafw:asakusa-windgate-jdbc:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwWindGatePlugin "com.asakusafw:asakusa-windgate-stream:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwWindGateSshLib "com.asakusafw:asakusa-windgate-core:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwWindGateSshLib "com.asakusafw:asakusa-windgate-hadoopfs:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwWindGateSshLib "ch.qos.logback:logback-classic:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwWindGateSshLib "ch.qos.logback:logback-core:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwWindGateSshLib "org.slf4j:slf4j-api:${project.asakusafwInternal.dep.slf4jVersion}@jar"
                asakusafwWindGateSshLib "org.slf4j:jul-to-slf4j:${project.asakusafwInternal.dep.slf4jVersion}@jar"

                asakusafwThunderGateDist "com.asakusafw:asakusa-thundergate:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwThunderGateLib "com.asakusafw:asakusa-thundergate:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwThunderGateLib "commons-configuration:commons-configuration:${project.asakusafwInternal.dep.commonsConfigurationVersion}@jar"
                asakusafwThunderGateLib "commons-io:commons-io:${project.asakusafwInternal.dep.commonsIoVersion}@jar"
                asakusafwThunderGateLib "commons-lang:commons-lang:${project.asakusafwInternal.dep.commonsLangVersion}@jar"
                asakusafwThunderGateLib "commons-logging:commons-logging:${project.asakusafwInternal.dep.commonsLoggingVersion}@jar"
                asakusafwThunderGateLib "log4j:log4j:${project.asakusafwInternal.dep.log4jVersion}@jar"
                asakusafwThunderGateLib "mysql:mysql-connector-java:${project.asakusafwInternal.dep.mysqlConnectorJavaVersion}@jar"
                asakusafwThunderGateCoreLib "com.asakusafw:asakusa-thundergate-runtime:${project.asakusafwOrganizer.asakusafwVersion}@jar"

                asakusafwDevelopmentDist "com.asakusafw:asakusa-test-driver:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwDevelopmentDist "com.asakusafw:asakusa-development-tools:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"

                asakusafwOperationDist "com.asakusafw:asakusa-operation-tools:${project.asakusafwOrganizer.asakusafwVersion}:dist@jar"
                asakusafwOperationLib  "com.asakusafw:asakusa-operation-tools:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwOperationLib "commons-cli:commons-cli:${project.asakusafwInternal.dep.commonsCliVersion}@jar"
                asakusafwOperationLib "ch.qos.logback:logback-classic:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwOperationLib "ch.qos.logback:logback-core:${project.asakusafwInternal.dep.logbackVersion}@jar"
                asakusafwOperationLib "org.slf4j:slf4j-api:${project.asakusafwInternal.dep.slf4jVersion}@jar"

                asakusafwYaessJobQueuePluginLib "com.asakusafw:asakusa-yaess-jobqueue:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                asakusafwYaessJobQueuePluginLib "org.apache.httpcomponents:httpcore:${project.asakusafwInternal.dep.httpClientVersion}@jar"
                asakusafwYaessJobQueuePluginLib "org.apache.httpcomponents:httpclient:${project.asakusafwInternal.dep.httpClientVersion}@jar"
                asakusafwYaessJobQueuePluginLib "com.google.code.gson:gson:${project.asakusafwInternal.dep.gsonVersion}@jar"
                asakusafwYaessJobQueuePluginLib "commons-codec:commons-codec:${project.asakusafwInternal.dep.commonsCodecVersion}@jar"
                asakusafwYaessJobQueuePluginLib "commons-logging:commons-logging:${project.asakusafwInternal.dep.commonsLoggingVersion}@jar"

                asakusafwWindGateRetryablePluginLib "com.asakusafw:asakusa-windgate-retryable:${project.asakusafwOrganizer.asakusafwVersion}@jar"

                asakusafwDirectIoHivePluginLib "com.asakusafw:asakusa-hive-core:${project.asakusafwOrganizer.asakusafwVersion}@jar"
                for (Object library : project.asakusafwOrganizer.hive.libraries) {
                    asakusafwDirectIoHivePluginLib library
                }
            }
        }
    }

    private defineOrganizerTasks() {
        def installAsakusafw = project.task('installAsakusafw', dependsOn: 'attachAssembleDev') << {
            if (!System.env['ASAKUSA_HOME']) {
                throw new RuntimeException('ASAKUSA_HOME is not defined')
            }
            def timestamp = new Date().format('yyyyMMddHHmmss')
            project.copy {
                from "${System.env.ASAKUSA_HOME}"
                into "${System.env.ASAKUSA_HOME}_${timestamp}"
            }
            project.delete "${System.env.ASAKUSA_HOME}"
            project.mkdir "${System.env.ASAKUSA_HOME}"
            project.copy {
                from "${project.asakusafwOrganizer.assembleDir}"
                into "${System.env.ASAKUSA_HOME}"
            }
            println "Asakusa Framework has been installed on ASAKUSA_HOME: ${System.env.ASAKUSA_HOME}"
        }
        installAsakusafw.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        installAsakusafw.setDescription('Installs framework files to \$ASAKUSA_HOME.')

        def cleanAssembleAsakusafw = project.task('cleanAssembleAsakusafw') << {
            project.delete project.asakusafwOrganizer.assembleDir
        }
        cleanAssembleAsakusafw.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        cleanAssembleAsakusafw.setDescription('Deletes the assemble working directory.')

        def attachBatchapps = project.task('attachBatchapps') << {
            project.copy {
                from project.asakusafw.compiler.compiledSourceDirectory
                into "${project.asakusafwOrganizer.assembleDir}/batchapps"
            }
        }
        attachBatchapps.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachBatchapps.setDescription('Attaches batch application files to assembly.')

        def attachComponentCore = project.task('attachComponentCore') << {
            unpackDists project.configurations.asakusafwCoreDist
            project.copy {
                from project.configurations.asakusafwCoreLib
                into "${project.asakusafwOrganizer.assembleDir}/core/lib"
                rename (/asakusa-runtime-all(.*).jar/, 'asakusa-runtime-all.jar')
            }
        }
        attachComponentCore.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachComponentCore.setDescription('Attaches framework core component files to assembly.')

        def attachComponentDirectIo = project.task('attachComponentDirectIo') << {
            unpackDists project.configurations.asakusafwDirectIoDist
            project.copy {
                from project.configurations.asakusafwDirectIoLib
                into "${project.asakusafwOrganizer.assembleDir}/directio/lib"
            }
        }
        attachComponentDirectIo.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachComponentDirectIo.setDescription('Attaches Direct I/O component files to assembly.')

        def attachComponentYaess = project.task('attachComponentYaess') << {
            unpackDists project.configurations.asakusafwYaessDist
            project.copy {
                from project.configurations.asakusafwYaessLib
                into "${project.asakusafwOrganizer.assembleDir}/yaess/lib"
            }
            project.copy {
                from project.configurations.asakusafwYaessPlugin
                into "${project.asakusafwOrganizer.assembleDir}/yaess/plugin"
            }
            project.copy {
                from project.configurations.asakusafwYaessTool
                into "${project.asakusafwOrganizer.assembleDir}/yaess/tools"
            }
        }
        attachComponentYaess.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachComponentYaess.setDescription('Attaches YAESS component files to assembly.')

        def attachComponentWindGate = project.task('attachComponentWindGate') << {
            unpackDists project.configurations.asakusafwWindGateDist
            project.copy {
                from project.configurations.asakusafwWindGateLib
                into "${project.asakusafwOrganizer.assembleDir}/windgate/lib"
            }
            project.copy {
                from project.configurations.asakusafwWindGatePlugin
                into "${project.asakusafwOrganizer.assembleDir}/windgate/plugin"
            }
            project.copy {
                from project.configurations.asakusafwWindGateSshLib
                into "${project.asakusafwOrganizer.assembleDir}/windgate-ssh/lib"
            }
        }
        attachComponentWindGate.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachComponentWindGate.setDescription('Attaches WindGate component files to assembly.')

        def attachComponentThunderGate = project.task('attachComponentThunderGate') << {
            unpackThunderGateDists project.configurations.asakusafwThunderGateDist, project.asakusafwOrganizer.thundergate.target
            project.copy {
                from project.configurations.asakusafwThunderGateLib
                into "${project.asakusafwOrganizer.assembleDir}/bulkloader/lib"
            }
            project.copy {
                from project.configurations.asakusafwThunderGateCoreLib
                into "${project.asakusafwOrganizer.assembleDir}/core/lib"
            }
        }
        attachComponentThunderGate.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachComponentThunderGate.setDescription('Attaches ThunderGate component files to assembly.')

        def attachComponentDevelopment = project.task('attachComponentDevelopment') << {
            unpackDists project.configurations.asakusafwDevelopmentDist
        }
        attachComponentDevelopment.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachComponentDevelopment.setDescription('Attaches developent tool files to assembly.')

        def attachComponentOperation = project.task('attachComponentOperation') << {
            unpackDists project.configurations.asakusafwOperationDist
            project.copy {
                from project.configurations.asakusafwOperationLib
                into "${project.asakusafwOrganizer.assembleDir}/tools/lib"
            }
        }
        attachComponentOperation.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachComponentOperation.setDescription('Attaches operation tool files to assembly.')

        def attachExtensionYaessJobQueue = project.task('attachExtensionYaessJobQueue') << {
            project.copy {
                from project.configurations.asakusafwYaessJobQueuePluginLib
                into "${project.asakusafwOrganizer.assembleDir}/yaess/plugin"
            }
        }
        attachExtensionYaessJobQueue.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachExtensionYaessJobQueue.setDescription('Attaches YaessJobQueue files to assembly.')

        def attachExtensionWindGateRetryable = project.task('attachExtensionWindGateRetryable') << {
            project.copy {
                from project.configurations.asakusafwWindGateRetryablePluginLib
                into "${project.asakusafwOrganizer.assembleDir}/windgate/plugin"
            }
        }
        attachExtensionWindGateRetryable.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachExtensionWindGateRetryable.setDescription('Attaches WindGateRetryable files to assembly.')

        def attachExtensionDirectIoHive = project.task('attachExtensionDirectIoHive') << {
            project.copy {
                from project.configurations.asakusafwDirectIoHivePluginLib
                into "${project.asakusafwOrganizer.assembleDir}/ext/lib"
            }
        }
        attachExtensionDirectIoHive.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachExtensionDirectIoHive.setDescription('Attaches DirectIoHive files to assembly.')

        def attachAssembleDev = project.task('attachAssembleDev', dependsOn: [
                attachComponentCore,
                attachComponentDirectIo,
                attachComponentYaess,
                attachComponentWindGate,
                attachComponentDevelopment,
                attachComponentOperation
        ])
        attachAssembleDev.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachAssembleDev.setDescription('Attaches application development environment files to assembly.')

        def attachAssemble = project.task('attachAssemble', dependsOn: [
                attachComponentCore,
                attachComponentDirectIo,
                attachComponentYaess,
                attachComponentWindGate,
                attachComponentOperation
        ])
        attachAssemble.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
        attachAssemble.setDescription('Attaches framework files to assembly with default configuration.')
        project.afterEvaluate {
            if (project.asakusafwOrganizer.thundergate.isEnabled()) {
                project.logger.info 'Enabling ThunderGate'
                attachAssemble.dependsOn attachComponentThunderGate
                attachAssembleDev.dependsOn attachComponentThunderGate
            }
            if (project.plugins.hasPlugin('asakusafw')) {
                project.logger.info 'Enabling batchapps'
                attachAssembleDev.dependsOn attachBatchapps
            }
        }

        project.afterEvaluate {
            def assembleAsakusafw = project.task('assembleAsakusafw', dependsOn: 'attachAssemble', type: Tar) {
                from project.asakusafwOrganizer.assembleDir
                destinationDir project.buildDir
                compression Compression.GZIP
                archiveName "asakusafw-${project.asakusafwOrganizer.asakusafwVersion}.tar.gz"
            }
            assembleAsakusafw.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            assembleAsakusafw.setDescription('Assembles a tarball containing framework files for deployment.')

            def assembleDevAsakusafw = project.task('assembleDevAsakusafw', dependsOn: 'attachAssembleDev', type: Tar) {
                from project.asakusafwOrganizer.assembleDir
                destinationDir project.buildDir
                compression Compression.GZIP
                archiveName "asakusafw-${project.asakusafwOrganizer.asakusafwVersion}-dev.tar.gz"
            }
            assembleDevAsakusafw.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            assembleDevAsakusafw.setDescription('Assembles a tarball containing framework files for development.')

            def asakusaDistCustom = project.task('assembleCustomAsakusafw', type: Tar) {
                from project.asakusafwOrganizer.assembleDir
                destinationDir project.buildDir
                compression Compression.GZIP
                archiveName "asakusafw-${project.asakusafwOrganizer.asakusafwVersion}-custom.tar.gz"
            }
            asakusaDistCustom.mustRunAfter(project.tasks.findAll { t ->
                t.name.startsWith('attach')
            })
            asakusaDistCustom.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            asakusaDistCustom.setDescription('Assembles a tarball containing custom framework configuration files.')

            project.tasks.addRule('Pattern: attachConf<Target>: Attaches asakusafw custom distribution files to assembly.') { String taskName ->
                if (taskName.startsWith('attachConf')) {
                    def task = project.task(taskName) {
                        ext.distTarget = (taskName - 'attachConf').toLowerCase()
                        doLast {
                            project.copy {
                                from "src/dist/${distTarget}"
                                into project.asakusafwOrganizer.assembleDir
                                exclude '**/.*'
                            }
                        }
                    }
                    task.mustRunAfter(project.tasks.findAll { t ->
                        t.name.startsWith('attachBatchapp') ||
                        t.name.startsWith('attachComponent') ||
                        t.name.startsWith('attachExtension')
                    })
                }
            }

            project.tasks.findAll { task -> task.name.startsWith('attach')}*.dependsOn('cleanAssembleAsakusafw')
        }
    }

    def unpackDists(distConf) {
        distConf.files.each { dist ->
            project.copy {
                from project.zipTree(dist)
                into project.asakusafwOrganizer.assembleDir
                exclude 'META-INF/'
                filesMatching('**/*.sh') { f ->
                    f.setMode(0755)
                }
            }
        }
    }

    def unpackThunderGateDists(distConf, targetName) {
        distConf.files.each { dist ->
            project.copy {
                from project.zipTree(dist)
                into project.asakusafwOrganizer.assembleDir
                exclude 'META-INF/'
                filesMatching('**/*.sh') { f ->
                    f.setMode(0755)
                }
                if (targetName) {
                    rename(/\[\w+\]-jdbc\.properties/, "${targetName}-jdbc.properties")
                }
            }
        }
    }
}
