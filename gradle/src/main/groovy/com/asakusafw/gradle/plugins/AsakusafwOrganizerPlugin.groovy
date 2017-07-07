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

import org.gradle.api.*
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.bundling.*

import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.BatchappsConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.CoreConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.DirectIoConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ExtensionConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HiveConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.TestingConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.WindGateConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.YaessConfiguration
import com.asakusafw.gradle.plugins.internal.AsakusaSdk
import com.asakusafw.gradle.plugins.internal.AsakusafwOrganizer
import com.asakusafw.gradle.plugins.internal.PluginUtils
import com.asakusafw.gradle.tasks.GatherAssemblyTask

/**
 * Gradle plugin for assembling and Installing Asakusa Framework.
 * @since 0.5.3
 * @version 0.7.1
 */
class AsakusafwOrganizerPlugin  implements Plugin<Project> {

    /**
     * The task group name.
     */
    static final String ASAKUSAFW_ORGANIZER_GROUP = 'Asakusa Framework Organizer'

    /**
     * The development profile name.
     */
    static final String PROFILE_NAME_DEVELOPMENT = 'dev'

    /**
     * The production profile name.
     */
    static final String PROFILE_NAME_PRODUCTION = 'prod'

    private Project project

    private NamedDomainObjectCollection<AsakusafwOrganizer> organizers

    void apply(Project project) {
        this.project = project
        this.organizers = project.container(AsakusafwOrganizer)
        project.apply plugin: AsakusafwBasePlugin

        configureProject()
        configureProfiles()
        configureTasks()
    }

    private void configureProject() {
        configureExtentionProperties()
    }

    private void configureExtentionProperties() {
        AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
        AsakusafwOrganizerPluginConvention convention = project.extensions.create('asakusafwOrganizer', AsakusafwOrganizerPluginConvention)
        convention.core = convention.extensions.create('core', CoreConfiguration)
        convention.directio = convention.extensions.create('directio', DirectIoConfiguration)
        convention.windgate = convention.extensions.create('windgate', WindGateConfiguration)
        convention.hive = convention.extensions.create('hive', HiveConfiguration)
        convention.yaess = convention.extensions.create('yaess', YaessConfiguration)
        convention.batchapps = convention.extensions.create('batchapps', BatchappsConfiguration)
        convention.testing = convention.extensions.create('testing', TestingConfiguration)
        convention.extension = convention.extensions.create('extension', ExtensionConfiguration)

        convention.conventionMapping.with {
            asakusafwVersion = {
                if (base.frameworkVersion == null) {
                    throw new InvalidUserDataException('Asakusa Framework core libraries version is not defined')
                }
                return base.frameworkVersion
            }
            assembleDir = { (String) "${project.buildDir}/asakusafw-assembly" }
        }
        convention.directio.conventionMapping.with {
            enabled = { true }
        }
        convention.windgate.conventionMapping.with {
            enabled = { true }
            sshEnabled = { true }
            retryableEnabled = { false }
        }
        convention.hive.conventionMapping.with {
            enabled = { false }
        }
        convention.hive.defaultLibraries.add((String) "org.apache.hive:hive-exec:${base.hiveVersion}@jar")
        convention.yaess.conventionMapping.with {
            enabled = { true }
            toolsEnabled = { true }
            hadoopEnabled = { true }
            jobqueueEnabled = { false }
            iterativeEnabled = { false }
        }
        convention.batchapps.conventionMapping.with {
            enabled = { true }
        }
        convention.testing.conventionMapping.with {
            enabled = { false }
        }

        // profiles
        convention.profiles = createProfileContainer(convention)
        convention.extensions.profiles = convention.profiles
        convention.profiles.create(PROFILE_NAME_DEVELOPMENT) { AsakusafwOrganizerProfile profile ->
            profile.batchapps.enabled = false
            profile.testing.enabled = true
        }
        convention.profiles.create(PROFILE_NAME_PRODUCTION) { AsakusafwOrganizerProfile profile ->
            profile.conventionMapping.with {
                archiveName = { (String) "asakusafw-${project.name}.tar.gz" }
            }
        }
        PluginUtils.deprecateAsakusafwVersion project, 'asakusafwOrganizer', convention
        PluginUtils.injectVersionProperty(convention.core, { base.frameworkVersion })
        convention.metaClass.toStringDelegate = { -> "asakusafwOrganizer { ... }" }
    }

    private NamedDomainObjectContainer<AsakusafwOrganizerProfile> createProfileContainer(AsakusafwOrganizerPluginConvention convention) {
        NamedDomainObjectContainer<AsakusafwOrganizerProfile> container
        container = project.container(AsakusafwOrganizerProfile) { String name ->
            def profile = container.extensions.create(name, AsakusafwOrganizerProfile, name)
            configureProfile convention, profile
            return profile
        }
        return PluginUtils.enhanceNamedDomainObjectContainer(container)
    }

    private void configureProfile(AsakusafwOrganizerPluginConvention convention,  AsakusafwOrganizerProfile profile) {
        AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
        profile.core = profile.extensions.create('core', CoreConfiguration)
        profile.directio = profile.extensions.create('directio', DirectIoConfiguration)
        profile.windgate = profile.extensions.create('windgate', WindGateConfiguration)
        profile.hive = profile.extensions.create('hive', HiveConfiguration)
        profile.yaess = profile.extensions.create('yaess', YaessConfiguration)
        profile.batchapps = profile.extensions.create('batchapps', BatchappsConfiguration)
        profile.testing = profile.extensions.create('testing', TestingConfiguration)
        profile.extension = profile.extensions.create('extension', ExtensionConfiguration)

        profile.conventionMapping.with {
            asakusafwVersion = { convention.asakusafwVersion } // ok, this just inherits parent version
            assembleDir = { (String) "${convention.assembleDir}-${profile.name}" }
            archiveName = { (String) "asakusafw-${project.name}-${profile.name}.tar.gz" }
        }
        profile.components.process {
            exclude 'META-INF/**'
            filesMatching('**/*.sh') { FileCopyDetails f ->
                f.setMode(0755)
            }
        }
        profile.directio.conventionMapping.with {
            enabled = { convention.directio.enabled }
        }
        profile.windgate.conventionMapping.with {
            enabled = { convention.windgate.enabled }
            sshEnabled = { convention.windgate.sshEnabled }
            retryableEnabled = { convention.windgate.retryableEnabled }
        }
        profile.hive.conventionMapping.with {
            enabled = { convention.hive.enabled }
            defaultLibraries = { convention.hive.libraries }
        }
        profile.yaess.conventionMapping.with {
            enabled = { convention.yaess.enabled }
            toolsEnabled = { convention.yaess.toolsEnabled }
            hadoopEnabled = { convention.yaess.hadoopEnabled }
            jobqueueEnabled = { convention.yaess.jobqueueEnabled }
            iterativeEnabled = { convention.yaess.iterativeEnabled }
        }
        profile.batchapps.conventionMapping.with {
            enabled = { convention.batchapps.enabled }
        }
        profile.testing.conventionMapping.with {
            enabled = { convention.testing.enabled }
        }
        profile.extension.conventionMapping.with {
            defaultLibraries = { convention.extension.libraries }
        }
        PluginUtils.deprecateAsakusafwVersion project, "asakusafwOrganizer.profiles.${profile.name}", profile
        PluginUtils.injectVersionProperty(profile.core, { base.frameworkVersion })
    }

    private void configureProfiles() {
        project.asakusafwOrganizer.profiles.all { AsakusafwOrganizerProfile profile ->
            AsakusafwOrganizer organizer = new AsakusafwOrganizer(project, profile)
            organizer.configureProfile()
            organizers << organizer
        }
    }

    private void configureTasks() {
        configureCommonTasks()
        configureDevelopmentProfileTasks()
        configureProductionProfileTasks()
    }

    private void configureCommonTasks() {
        defineFacadeTasks([
                      cleanAssembleAsakusafw : 'Deletes assembly files and directories.',
                         attachComponentCore : 'Attaches framework core components to assemblies.',
                     attachComponentDirectIo : 'Attaches Direct I/O components to assemblies.',
                        attachComponentYaess : 'Attaches YAESS components to assemblies.',
                  attachComponentYaessHadoop : 'Attaches Yaess Hadoop bridge components to assemblies.',
                     attachComponentWindGate : 'Attaches WindGate components to assemblies.',
                  attachComponentWindGateSsh : 'Attaches WindGate SSH components to assemblies.',
                      attachComponentTesting : 'Attaches testing tools to assemblies.',
                    attachComponentOperation : 'Attaches operation tools to assemblies.',
                    attachComponentExtension : 'Attaches framework extension components to assemblies.',
                   attachExtensionYaessTools : 'Attaches YAESS extra tools to assemblies.',
                attachExtensionYaessJobQueue : 'Attaches YAESS JobQueue client extensions to assemblies.',
               attachExtensionYaessIterative : 'Attaches YAESS iterative extensions to assemblies.',
            attachExtensionWindGateRetryable : 'Attaches WindGate retryable extensions to assemblies.',
                 attachExtensionDirectIoHive : 'Attaches Direct I/O Hive extensions to assemblies.',
        ])
        project.task('assembleAsakusafw') { Task task ->
            task.group ASAKUSAFW_ORGANIZER_GROUP
            task.description 'Assembles Asakusa deployment archives for each profile.'
            organizers.all { AsakusafwOrganizer organizer ->
                // task may not resolved yet
                task.dependsOn organizer.taskName(task.name)
            }
        }
        project.tasks.addRule('Pattern: attachConf<Target>: Attaches asakusafw custom distribution files to assembly.') {
            String taskName ->
            if (taskName.startsWith('attachConf')) {
                project.task(taskName) { Task task ->
                    task.ext.distTarget = (taskName - 'attachConf').toLowerCase()
                    project.tasks.withType(GatherAssemblyTask) { Task target ->
                        target.dependsOn task
                    }
                    task.doLast {
                        project.asakusafwOrganizer.assembly.into('.') {
                            put "src/dist/${distTarget}"
                            process {
                                exclude '**/.*'
                            }
                        }
                    }
                }
            }
        }

        PluginUtils.afterEvaluate(project) {
            if (project.plugins.hasPlugin('asakusafw-sdk')) {
                organizers.matching { it.name != PROFILE_NAME_DEVELOPMENT }.all { AsakusafwOrganizer organizer ->
                    project.tasks.assemble.dependsOn organizer.taskName('assembleAsakusafw')
                }
                project.tasks.matching { it.name == 'cleanAssemble' }.all { Task t ->
                    organizers.all { AsakusafwOrganizer organizer ->
                        t.dependsOn organizer.taskName('cleanAssembleAsakusafw')
                    }
                }
            }
        }
    }

    private void defineFacadeTasks(Map<String, String> taskMap) {
        taskMap.each { String taskName, String desc ->
            project.task(taskName) { Task task ->
                if (desc != null) {
                    task.group ASAKUSAFW_ORGANIZER_GROUP
                    task.description desc
                }
                organizers.all { AsakusafwOrganizer organizer ->
                    task.dependsOn organizer.task(task.name)
                }
            }
        }
    }

    private void configureDevelopmentProfileTasks() {
        AsakusafwOrganizer organizer = organizers[PROFILE_NAME_DEVELOPMENT]
        organizer.task('gatherAsakusafw').dependsOn organizer.task('cleanAssembleAsakusafw')
        project.tasks.create('backupAsakusafw') {
            shouldRunAfter organizer.task('gatherAsakusafw')
            onlyIf { AsakusaSdk.getFrameworkInstallationPath(project) != null }
            doLast {
                File home = AsakusaSdk.getFrameworkInstallationPath(project)
                String timestamp = new Date().format('yyyyMMddHHmmss')
                File backup = new File(home.parentFile, "${home.name}_${timestamp}")
                project.copy {
                    from home
                    into backup
                }
            }
        }
        project.tasks.create('updateAsakusafw') { Task t ->
            t.group ASAKUSAFW_ORGANIZER_GROUP
            t.description "Updates Asakusa Framework on \$ASAKUSA_HOME using '${organizer.profile.name}' profile."
            t.dependsOn organizer.task('gatherAsakusafw')
            t.shouldRunAfter 'backupAsakusafw'
            PluginUtils.afterEvaluate(project) {
                File home = AsakusaSdk.getFrameworkInstallationPath(project)
                if (home != null) {
                    t.inputs.dir organizer.task('gatherAsakusafw').destination
                    t.outputs.dir home
                }
            }
            t.doLast {
                File home = AsakusaSdk.getFrameworkInstallationPath(project, true)
                if (home.exists()) {
                    project.delete home
                }
                project.mkdir home
                project.copy {
                    from organizer.task('gatherAsakusafw').destination
                    into home
                }
            }
        }
        project.tasks.create('installAsakusafw') {
            group ASAKUSAFW_ORGANIZER_GROUP
            description "Installs Asakusa Framework to \$ASAKUSA_HOME using '${organizer.profile.name}' profile."
            dependsOn 'backupAsakusafw', 'updateAsakusafw'
            doLast {
                File home = AsakusaSdk.getFrameworkInstallationPath(project, true)
                if (project.tasks.updateAsakusafw.didWork) {
                    logger.lifecycle "Asakusa Framework is successfully installed: ${home}"
                } else {
                    logger.lifecycle "Asakusa Framework is already up-to-date: ${home}"
                }
            }
        }
    }

    private void configureProductionProfileTasks() {
        // no special tasks
    }
}
