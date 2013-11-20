package com.asakusafw.gradle.plugins

import org.gradle.api.*
import org.gradle.api.tasks.bundling.*

class AsakusafwOrganizerPlugin  implements Plugin<Project> {

    public static final String ASAKUSAFW_ORGANIZER_GROUP = 'Asakusa Framework Organizer'

    private Project project
    private AntBuilder ant

    void apply(Project project) {
        this.project = project
        this.ant = project.ant

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
        project.extensions.create('asakusafwOrganizer', AsakusafwOrganizerPluginConvention, project)
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
            }
        }
    }

    private defineOrganizerTasks() {
        project.afterEvaluate {
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
            attachBatchapps.setDescription('Attachs batch application files to assembly.')

            def attachComponentCore = project.task('attachComponentCore') << {
                unpackDists project.configurations.asakusafwCoreDist
                project.copy {
                    from project.configurations.asakusafwCoreLib
                    into "${project.asakusafwOrganizer.assembleDir}/core/lib"
                    rename (/asakusa-runtime-all(.*).jar/, 'asakusa-runtime-all.jar')
                }
            }
            attachComponentCore.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachComponentCore.setDescription('Attachs framework core component files to assembly.')

            def attachComponentDirectIo = project.task('attachComponentDirectIo') << {
                unpackDists project.configurations.asakusafwDirectIoDist
                project.copy {
                    from project.configurations.asakusafwDirectIoLib
                    into "${project.asakusafwOrganizer.assembleDir}/directio/lib"
                }
            }
            attachComponentDirectIo.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachComponentDirectIo.setDescription('Attachs Direct I/O component files to assembly.')

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
            attachComponentYaess.setDescription('Attachs YAESS component files to assembly.')

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
            attachComponentWindGate.setDescription('Attachs WindGate component files to assembly.')


            def attachComponentDevelopment = project.task('attachComponentDevelopment') << {
                unpackDists project.configurations.asakusafwDevelopmentDist
            }
            attachComponentDevelopment.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachComponentDevelopment.setDescription('Attachs developent tool files to assembly.')

            def attachComponentOperation = project.task('attachComponentOperation') << {
                unpackDists project.configurations.asakusafwOperationDist
                project.copy {
                    from project.configurations.asakusafwOperationLib
                    into "${project.asakusafwOrganizer.assembleDir}/tools/lib"
                }
            }
            attachComponentOperation.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachComponentOperation.setDescription('Attachs operation tool files to assembly.')

            def attachExtensionYaessJobQueue = project.task('attachExtensionYaessJobQueue') << {
                project.copy {
                    from project.configurations.asakusafwYaessJobQueuePluginLib
                    into "${project.asakusafwOrganizer.assembleDir}/yaess/plugin"
                }
            }
            attachExtensionYaessJobQueue.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachExtensionYaessJobQueue.setDescription('Attachs YaessJobQueue files to assembly.')

            def attachExtensionWindGateRetryable = project.task('attachExtensionWindGateRetryable') << {
                project.copy {
                    from project.configurations.asakusafwWindGateRetryablePluginLib
                    into "${project.asakusafwOrganizer.assembleDir}/windgate/plugin"
                }
            }
            attachExtensionWindGateRetryable.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachExtensionWindGateRetryable.setDescription('Attachs WindGateRetryable files to assembly.')

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
            asakusaDistCustom.setDescription('Assembles a tar archive containing custom framework configuration files.')

            def attachAssembleDev = project.task('attachAssembleDev', dependsOn: [
                    'attachBatchapps',
                    'attachComponentCore',
                    'attachComponentDirectIo',
                    'attachComponentYaess',
                    'attachComponentWindGate',
                    'attachComponentDevelopment',
                    'attachComponentOperation'
            ])
            attachAssembleDev.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachAssembleDev.setDescription('Attachs application development environment files to assembly.')


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

            def assembleDevAsakusafw = project.task('assembleDevAsakusafw', dependsOn: 'attachAssembleDev', type: Tar) {
                from project.asakusafwOrganizer.assembleDir
                destinationDir project.buildDir
                compression Compression.GZIP
                archiveName "asakusafw-${project.asakusafwOrganizer.asakusafwVersion}-dev.tar.gz"
            }
            assembleDevAsakusafw.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            assembleDevAsakusafw.setDescription('Assembles a tar archive containing framework files for development.')

            def attachAssemble = project.task('attachAssemble', dependsOn: [
                    'attachComponentCore',
                    'attachComponentDirectIo',
                    'attachComponentYaess',
                    'attachComponentWindGate',
                    'attachComponentOperation'
            ])
            attachAssemble.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            attachAssemble.setDescription('Attachs framework files to assembly with default configuration.')

            def assembleAsakusafw = project.task('assembleAsakusafw', dependsOn: 'attachAssemble', type: Tar) {
                from project.asakusafwOrganizer.assembleDir
                destinationDir project.buildDir
                compression Compression.GZIP
                archiveName "asakusafw-${project.asakusafwOrganizer.asakusafwVersion}.tar.gz"
            }
            assembleAsakusafw.setGroup(ASAKUSAFW_ORGANIZER_GROUP)
            assembleAsakusafw.setDescription('Assembles a tar archive containing framework files for deployment.')

            project.tasks.addRule('Pattern: attachConf<Target>: Attachs asakusafw custom distribution files to assembly.') { String taskName ->
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

}
