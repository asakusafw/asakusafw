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
package com.asakusafw.gradle.plugins.internal

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import com.asakusafw.gradle.plugins.AsakusafwBaseExtension
import com.asakusafw.gradle.plugins.AsakusafwBasePlugin
import com.asakusafw.gradle.plugins.AsakusafwOrganizerProfile
import com.asakusafw.gradle.tasks.GatherAssemblyTask

/**
 * Processes an {@link AsakusafwOrganizerProfile}.
 * @since 0.7.0
 * @version 0.10.0
 */
class AsakusafwOrganizer extends AbstractOrganizer {

    /**
     * Creates a new instance.
     * @param project the current project
     * @param profile the target profile
     */
    AsakusafwOrganizer(Project project, AsakusafwOrganizerProfile profile) {
        super(project, profile)
    }

    /**
     * Configures the target profile.
     */
    @Override
    void configureProfile() {
        configureConfigurations()
        configureDependencies()
        configureTasks()
        enableTasks()
    }

    private void configureConfigurations() {
        createConfigurations('asakusafw', [
                         CoreDist : "Contents of Asakusa Framework core modules (${profile.name}).",
                          CoreLib : "Libraries of Asakusa Framework core modules (${profile.name}).",
                       HadoopDist : "Contents of embedded Hadoop modules (${profile.name}).",
                        HadoopLib : "Libraries of embedded Hadoop modules (${profile.name}).",
                 HadoopLoggingLib : "Logging Libraries of embedded Hadoop modules (${profile.name}).",
                     DirectIoDist : "Contents of Asakusa Framework Direct I/O modules (${profile.name}).",
                      DirectIoLib : "Libraries of Asakusa Framework Direct I/O modules (${profile.name}).",
                 DirectIoHiveDist : "Contents of Direct I/O Hive modules (${profile.name}).",
                  DirectIoHiveLib : "Libraries of Direct I/O Hive modules (${profile.name}).",
                        YaessDist : "Contents of Asakusa Framework YAESS modules (${profile.name}).",
                         YaessLib : "Libraries of Asakusa Framework YAESS modules (${profile.name}).",
                      YaessPlugin : "Default plugin library sets of YAESS (${profile.name}).",
                  YaessHadoopDist : "Contents of Asakusa Framework YAESS Hadoop bridge (${profile.name}).",
                   YaessHadoopLib : "Libraries of Asakusa Framework YAESS Hadoop bridge (${profile.name}).",
                   YaessToolsDist : "Contents of Asakusa Framework YAESS tools (${profile.name}).",
                    YaessToolsLib : "Libraries of Asakusa Framework YAESS tools (${profile.name}).",
                YaessJobQueueDist : "Contents of YAESS JobQueue client modules (${profile.name}).",
                 YaessJobQueueLib : "Libraries of YAESS JobQueue client modules (${profile.name}).",
                YaessIterativeLib : "Libraries of YAESS iterative extension modules (${profile.name})",
                     WindGateDist : "Contents of Asakusa Framework WindGate tools (${profile.name}).",
                      WindGateLib : "Libraries of Asakusa Framework WindGate modules (${profile.name}).",
                   WindGatePlugin : "Default plugin library sets of WindGate (${profile.name}).",
                  WindGateSshDist : "Contents of Asakusa Framework WindGate-SSH modules (${profile.name}).",
                   WindGateSshLib : "Libraries of Asakusa Framework WindGate-SSH modules (${profile.name}).",
            WindGateRetryableDist : "Contents of WindGate retryable modules (${profile.name}).",
             WindGateRetryableLib : "Libraries of WindGate retryable modules (${profile.name}).",
                      TestingDist : "Contents of Asakusa Framework testing tools (${profile.name}).",
                    OperationDist : "Contents of Asakusa Framework operation tools (${profile.name}).",
                     OperationLib : "Libraries of Asakusa Framework operation tools (${profile.name}).",
                     ExtensionLib : "Asakusa Framework extension libraries (${profile.name}).",
        ])
        configuration('asakusafwExtensionLib').transitive = false
        configuration('asakusafwHadoopLib').with { Configuration conf ->
            conf.transitive = true
            // use snappy-java which is provided in asakusa-runtime-all
            conf.exclude group: 'org.xerial.snappy', module: 'snappy-java'
            conf.exclude group: 'org.slf4j', module: 'slf4j-log4j12'
            conf.exclude group: 'ch.qos.logback', module: 'logback-classic'
        }
    }

    private void configureDependencies() {
        PluginUtils.afterEvaluate(project) {
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            createDependencies('asakusafw', [
                CoreDist : "com.asakusafw:asakusa-runtime-configuration:${base.frameworkVersion}:dist@jar",
                CoreLib : [
                    "com.asakusafw:asakusa-runtime-all:${base.frameworkVersion}:lib@jar"
                ],
                HadoopDist : [],
                HadoopLib : [
                    "org.apache.hadoop:hadoop-common:${profile.hadoop.version}",
                    "org.apache.hadoop:hadoop-mapreduce-client-jobclient:${profile.hadoop.version}",
                ],
                HadoopLoggingLib : [
                    "org.slf4j:slf4j-simple:${base.slf4jVersion}@jar",
                 ],
                DirectIoDist : "com.asakusafw:asakusa-directio-tools:${base.frameworkVersion}:dist@jar",
                DirectIoLib : [
                    "com.asakusafw:asakusa-directio-tools:${base.frameworkVersion}:lib@jar",
                    "org.slf4j:slf4j-simple:${base.slf4jVersion}@jar",
                ],
                YaessDist : "com.asakusafw:asakusa-yaess-bootstrap:${base.frameworkVersion}:dist@jar",
                YaessLib : [
                    "com.asakusafw:asakusa-yaess-bootstrap:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-core:${base.frameworkVersion}@jar",
                    "commons-cli:commons-cli:${base.commonsCliVersion}@jar",
                    "ch.qos.logback:logback-classic:${base.logbackVersion}@jar",
                    "ch.qos.logback:logback-core:${base.logbackVersion}@jar",
                    "org.slf4j:slf4j-api:${base.slf4jVersion}@jar",
                    "org.slf4j:jul-to-slf4j:${base.slf4jVersion}@jar",
                ],
                YaessPlugin : [
                    "com.asakusafw:asakusa-yaess-flowlog:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-jsch:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-multidispatch:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-paralleljob:${base.frameworkVersion}@jar",
                    "com.jcraft:jsch:${base.jschVersion}@jar",
                ],
                YaessHadoopDist : "com.asakusafw:asakusa-yaess-core:${base.frameworkVersion}:dist@jar",
                YaessHadoopLib : [],
                YaessToolsDist : "com.asakusafw:asakusa-yaess-tools:${base.frameworkVersion}:dist@jar",
                YaessToolsLib : [
                    "com.asakusafw:asakusa-yaess-tools:${base.frameworkVersion}@jar",
                    "com.google.code.gson:gson:${base.gsonVersion}@jar",
                ],
                YaessJobQueueLib : [
                    "com.asakusafw:asakusa-yaess-jobqueue:${base.frameworkVersion}@jar",
                    "org.apache.httpcomponents:httpcore:${base.httpClientVersion}@jar",
                    "org.apache.httpcomponents:httpclient:${base.httpClientVersion}@jar",
                    "com.google.code.gson:gson:${base.gsonVersion}@jar",
                    "commons-codec:commons-codec:${base.commonsCodecVersion}@jar",
                    "commons-logging:commons-logging:${base.commonsLoggingVersion}@jar",
                ],
                YaessIterativeLib : [
                    "com.asakusafw:asakusa-iterative-yaess:${base.frameworkVersion}:lib@jar",
                ],
                WindGateDist : "com.asakusafw:asakusa-windgate-bootstrap:${base.frameworkVersion}:dist@jar",
                WindGateLib : [
                    "com.asakusafw:asakusa-windgate-bootstrap:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-core:${base.frameworkVersion}@jar",
                    "ch.qos.logback:logback-classic:${base.logbackVersion}@jar",
                    "ch.qos.logback:logback-core:${base.logbackVersion}@jar",
                    "org.slf4j:slf4j-api:${base.slf4jVersion}@jar",
                    "org.slf4j:jul-to-slf4j:${base.slf4jVersion}@jar",
                    "com.jcraft:jsch:${base.jschVersion}@jar",
                ],
                WindGatePlugin : [
                    "com.asakusafw:asakusa-windgate-hadoopfs:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-jdbc:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-stream:${base.frameworkVersion}@jar",
                ],
                WindGateSshDist : "com.asakusafw:asakusa-windgate-hadoopfs:${base.frameworkVersion}:dist@jar",
                WindGateSshLib : [
                    "com.asakusafw:asakusa-windgate-core:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-hadoopfs:${base.frameworkVersion}@jar",
                    "ch.qos.logback:logback-classic:${base.logbackVersion}@jar",
                    "ch.qos.logback:logback-core:${base.logbackVersion}@jar",
                    "org.slf4j:slf4j-api:${base.slf4jVersion}@jar",
                    "org.slf4j:jul-to-slf4j:${base.slf4jVersion}@jar",
                ],
                WindGateRetryableDist : [],
                WindGateRetryableLib : [
                    "com.asakusafw:asakusa-windgate-retryable:${base.frameworkVersion}@jar",
                ],
                TestingDist : "com.asakusafw:asakusa-test-driver:${base.frameworkVersion}:dist@jar",
                OperationDist : [
                    "com.asakusafw:asakusa-operation-tools:${base.frameworkVersion}:dist@jar",
                    "com.asakusafw:asakusa-command-portal:${base.frameworkVersion}:dist@jar",
                ],
                OperationLib : [
                    "com.asakusafw:asakusa-operation-tools:${base.frameworkVersion}:lib@jar",
                    "com.asakusafw:asakusa-command-portal:${base.frameworkVersion}:exec@jar",
                    "org.slf4j:slf4j-simple:${base.slf4jVersion}@jar",
                ],
                DirectIoHiveDist : [],
                DirectIoHiveLib : [
                    "com.asakusafw:asakusa-hive-info:${base.frameworkVersion}@jar",
                    "com.asakusafw:asakusa-hive-core:${base.frameworkVersion}@jar",
                ] + profile.hive.libraries,
                ExtensionLib : profile.extension.libraries,
            ])
            if (PluginUtils.compareGradleVersion('2.5') >= 0) {
                configuration('asakusafwHadoopLib').resolutionStrategy.dependencySubstitution {
                    substitute module('log4j:log4j') with module("org.slf4j:log4j-over-slf4j:${base.slf4jVersion}")
                    substitute module('commons-logging:commons-logging') with module("org.slf4j:jcl-over-slf4j:${base.slf4jVersion}")
                }
            }
        }
    }

    private void configureTasks() {
        createAttachComponentTasks 'attachComponent', [
            Core : {
                into('.') {
                    extract configuration('asakusafwCoreDist')
                }
                into('core/lib') {
                    put configuration('asakusafwCoreLib')
                    process {
                        rename(/asakusa-runtime-all-(.*).jar/, 'asakusa-runtime-all.jar')
                    }
                }
            },
            Hadoop : {
                into('.') {
                    extract configuration('asakusafwHadoopDist')
                }
                into('hadoop/lib') {
                    put configuration('asakusafwHadoopLib')
                }
                into('hadoop/lib/logging') {
                    put configuration('asakusafwHadoopLoggingLib')
                }
            },
            DirectIo : {
                into('.') {
                    extract configuration('asakusafwDirectIoDist')
                }
                into('directio/lib') {
                    put configuration('asakusafwDirectIoLib')
                    process {
                        rename(/asakusa-directio-tools-.*\.jar/, 'asakusa-directio-tools.jar')
                    }
                }
            },
            Yaess : {
                into('.') {
                    extract configuration('asakusafwYaessDist')
                }
                into('yaess/lib') {
                    put configuration('asakusafwYaessLib')
                }
                into('yaess/plugin') {
                    put configuration('asakusafwYaessPlugin')
                }
            },
            YaessHadoop : {
                into('.') {
                    extract configuration('asakusafwYaessHadoopDist')
                }
                into('yaess-hadoop/lib') {
                    put configuration('asakusafwYaessHadoopLib')
                }
            },
            WindGate : {
                into('.') {
                    extract configuration('asakusafwWindGateDist')
                }
                into('windgate/lib') {
                    put configuration('asakusafwWindGateLib')
                }
                into('windgate/plugin') {
                    put configuration('asakusafwWindGatePlugin')
                }
            },
            WindGateSsh : {
                into('.') {
                    extract configuration('asakusafwWindGateSshDist')
                }
                into('windgate-ssh/lib') {
                    put configuration('asakusafwWindGateSshLib')
                }
            },
            Testing : {
                into('.') {
                    extract configuration('asakusafwTestingDist')
                }
            },
            Operation : {
                into('.') {
                    extract configuration('asakusafwOperationDist')
                }
                into('tools/lib') {
                    put configuration('asakusafwOperationLib')
                    process {
                        rename(/(asakusa-operation-tools)-.*-lib\.jar/, '$1.jar')
                        rename(/(asakusa-command-portal)-.*-exec\.jar/, '$1.jar')
                        rename(/([0-9A-Za-z\-]+)-cli-.*-exec.jar/, '$1.jar')
                        rename(/slf4j-simple-.*\.jar/, 'slf4j-simple.jar')
                    }
               }
            },
            Extension : {
                into('ext/lib') {
                    put configuration('asakusafwExtensionLib')
                }
            },
        ]
        createAttachComponentTasks 'attachExtension', [
            DirectIoHive : {
                into('.') {
                    put configuration('asakusafwDirectIoHiveDist')
                }
                into('ext/lib') {
                    put configuration('asakusafwDirectIoHiveLib')
                }
            },
            YaessTools : {
                into('.') {
                    extract configuration('asakusafwYaessToolsDist')
                }
                into('yaess/lib') {
                    put configuration('asakusafwYaessToolsLib')
                }
            },
            YaessJobQueue : {
                into('.') {
                    extract configuration('asakusafwYaessJobQueueDist')
                }
                into('yaess/plugin') {
                    put configuration('asakusafwYaessJobQueueLib')
                }
            },
            YaessIterative : {
                into('yaess/plugin') {
                    put configuration('asakusafwYaessIterativeLib')
                }
            },
            WindGateRetryable : {
                into('.') {
                    put configuration('asakusafwWindGateRetryableDist')
                }
                into('windgate/plugin') {
                    put configuration('asakusafwWindGateRetryableLib')
                }
            },
        ]
        createTask('cleanAssembleAsakusafw') {
            doLast {
                project.delete task('gatherAsakusafw').destination
                project.delete task('assembleAsakusafw').archivePath
            }
        }
        createTask('attachAssemble')
        createTask('gatherAsakusafw', GatherAssemblyTask) { Task t ->
            dependsOn task('attachAssemble')
            shouldRunAfter task('cleanAssembleAsakusafw')
            assemblies << profile.components
            assemblies << project.asakusafwOrganizer.assembly
            assemblies << profile.assembly
            dependsOn assemblies
            conventionMapping.with {
                destination = { project.file(profile.assembleDir) }
            }
        }

        PluginUtils.afterEvaluate(project) {
            // Gather task must be run after 'attach*' were finished
            project.tasks.matching { it.name.startsWith('attach') && isProfileTask(it) }.all { Task target ->
                task('gatherAsakusafw').mustRunAfter target
            }
            createTask('assembleAsakusafw', Tar) {
                dependsOn task('gatherAsakusafw')
                from task('gatherAsakusafw').destination
                destinationDir project.buildDir
                compression Compression.GZIP
                archiveName profile.archiveName
            }
        }
    }

    private void enableTasks() {
        PluginUtils.afterEvaluate(project) {
            // default enabled
            task('attachAssemble').dependsOn task('attachComponentCore')
            task('attachAssemble').dependsOn task('attachComponentOperation')
            task('attachAssemble').dependsOn task('attachComponentExtension')

            if (profile.hadoop.isEmbed()) {
                project.logger.info "Enabling embedded Hadoop: ${profile.name}"
                task('attachAssemble').dependsOn task('attachComponentHadoop')
            }
            if (profile.directio.isEnabled()) {
                project.logger.info "Enabling Direct I/O: ${profile.name}"
                task('attachAssemble').dependsOn task('attachComponentDirectIo')
            }
            if (profile.windgate.isEnabled()) {
                project.logger.info "Enabling WindGate: ${profile.name}"
                task('attachAssemble').dependsOn task('attachComponentWindGate')
            }
            if (profile.windgate.isSshEnabled()) {
                project.logger.info "Enabling WindGate SSH: ${profile.name}"
                task('attachAssemble').dependsOn task('attachComponentWindGateSsh')
            }
            if (profile.windgate.isRetryableEnabled()) {
                project.logger.info "Enabling WindGate Retryable: ${profile.name}"
                task('attachComponentWindGate').dependsOn task('attachExtensionWindGateRetryable')
            }
            if (profile.hive.isEnabled()) {
                project.logger.info "Enabling Direct I/O Hive: ${profile.name}"
                task('attachAssemble').dependsOn task('attachExtensionDirectIoHive')
            }
            if (profile.yaess.isEnabled()) {
                project.logger.info "Enabling YAESS: ${profile.name}"
                task('attachAssemble').dependsOn task('attachComponentYaess')
            }
            if (profile.yaess.isHadoopEnabled()) {
                project.logger.info "Enabling YAESS Hadoop: ${profile.name}"
                task('attachAssemble').dependsOn task('attachComponentYaessHadoop')
            }
            if (profile.yaess.isToolsEnabled()) {
                project.logger.info "Enabling YAESS tools: ${profile.name}"
                task('attachComponentYaess').dependsOn task('attachExtensionYaessTools')
            }
            if (profile.yaess.isJobqueueEnabled()) {
                project.logger.info "Enabling YAESS JobQueue: ${profile.name}"
                task('attachComponentYaess').dependsOn task('attachExtensionYaessJobQueue')
            }
            if (profile.yaess.isIterativeEnabled()) {
                project.logger.info "Enabling YAESS iterative extension: ${profile.name}"
                task('attachComponentYaess').dependsOn task('attachExtensionYaessIterative')
            }
            if (profile.testing.isEnabled()) {
                project.logger.info "Enabling Testing: ${profile.name}"
                task('attachAssemble').dependsOn task('attachComponentTesting')
            }
        }
    }
}
