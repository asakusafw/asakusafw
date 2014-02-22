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
package com.asakusafw.gradle.tasks

import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import com.asakusafw.gradle.tasks.internal.AbstractAsakusaToolTask

/**
 * Gradle Task for generating ThunderGate data models from DDL scripts.
 */
class GenerateThunderGateDataModelTask extends AbstractAsakusaToolTask {

    /**
     * The DDL script source encoding.
     */
    @Optional
    @Input
    String ddlEncoding

    /**
     * The target database connectivity property file.
     */
    @InputFile
    File jdbcConfiguration

    /**
     * The DMDL script target directory.
     */
    @OutputDirectory
    File dmdlOutputDirectory

    /**
     * The DMDL script encoding.
     */
    @Optional
    @Input
    String dmdlOutputEncoding

    /**
     * The target table/view name inclusion pattern (in regex).
     */
    @Optional
    @Input
    String includePattern

    /**
     * The target table/view name exclusion pattern (in regex).
     */
    @Optional
    @Input
    String excludePattern

    /**
     * The system ID column name.
     */
    @Optional
    @Input
    String sidColumnName

    /**
     * The last updated date-time column name.
     */
    @Optional
    @Input
    String timestampColumnName

    /**
     * The logical delete flag column name.
     */
    @Optional
    @Input
    String deleteFlagColumnName

    /**
     * The logical delete flag column value what represents 'TRUE'.
     */
    @Optional
    @Input
    Object deleteFlagColumnValue

    /**
     * The record lock DDL script target file.
     */
    @Optional
    @OutputFile
    File recordLockDdlOutput

    /**
     * The system DDL files.
     * This task executes these DDL files after the data model was generated.
     * Each element is evaluated as {@code project.file(...)}.
     */
    @InputFiles
    List<Object> systemDdlFiles = []

    /**
     * Performs actions of this task.
     */
    @TaskAction
    def perform() {
        clearOutput()
        def jdbcConf = extractConfigurations()
        resetDatabase jdbcConf
        executeUserDdlFiles jdbcConf
        generateDmdls jdbcConf
        executeSystemDdlFiles jdbcConf
    }

    private def extractConfigurations() {
        project.logger.info('Loading JDBC properties: {}', getJdbcConfiguration())
        Properties props = new Properties()
        File file = getJdbcConfiguration()
        file.withInputStream { stream -> props.load(stream) }
        checkConfigurations file, props, [
            'jdbc.driver',
            'jdbc.url',
            'jdbc.user',
            'jdbc.password',
            'database.name',
        ]
        return new ConfigSlurper().parse(props)
    }

    private def checkConfigurations(File file, Properties properties, List<String> keys) {
        for (String key in keys) {
            if (properties[key] == null) {
                throw new InvalidUserDataException("${file} must contain \"${key}\"")
            }
        }
    }

    private def clearOutput() {
        project.delete getDmdlOutputDirectory()
        if (getRecordLockDdlOutput() != null) {
            project.delete getRecordLockDdlOutput()
        }
    }

    private def resetDatabase(jdbcConf) {
        project.logger.info('Initializing ThunderGate database: {}', jdbcConf.database.name)
        def conf = getAntSqlConf(jdbcConf)
        project.ant {
            sql(conf) {
                transaction "DROP DATABASE IF EXISTS ${jdbcConf.database.name};"
                transaction "CREATE DATABASE ${jdbcConf.database.name} DEFAULT CHARACTER SET utf8;"
            }
        }
    }

    private def executeUserDdlFiles(jdbcConf) {
        project.logger.info('Executing User DDLs: {}', jdbcConf.database.name)
        def files = []
        if (getRecordLockDdlOutput() != null && getRecordLockDdlOutput().exists()) {
            files << getRecordLockDdlOutput()
        }
        for (def file in getSourcepathCollection()) {
            if (file != null) {
                files << file.absoluteFile
            }
        }
        files.sort(true)
        executeDdlFiles jdbcConf, files
    }

    private def generateDmdls(jdbcConf) {
        project.logger.info('Generating Data Models: {}', jdbcConf.database.name)
        project.javaexec {
            main = 'com.asakusafw.dmdl.thundergate.Main'
            delegate.classpath = this.getToolClasspathCollection() + this.getPluginClasspathCollection()
            delegate.jvmArgs = this.getJvmArgs()
            if (this.getMaxHeapSize()) {
                delegate.maxHeapSize = this.getMaxHeapSize()
            }
            if (this.getLogbackConf()) {
                delegate.systemProperties += [ 'logback.configurationFile' : this.getLogbackConf().absolutePath ]
            }
            delegate.systemProperties += this.getSystemProperties()
            delegate.args = [
                '-jdbc',
                getJdbcConfiguration().absolutePath,
                '-output',
                getDmdlOutputDirectory().absolutePath,
            ]
            this.appendArgs(delegate, '-encoding', getDmdlOutputEncoding())
            this.appendArgs(delegate, '-includes', getIncludePattern())
            this.appendArgs(delegate, '-excludes', getExcludePattern())
            this.appendArgs(delegate, '-sid_column', getSidColumnName())
            this.appendArgs(delegate, '-timestamp_column', getTimestampColumnName())
            this.appendArgs(delegate, '-delete_flag_column', getDeleteFlagColumnName())
            this.appendArgs(delegate, '-delete_flag_value', getDeleteFlagColumnValue()?.toString())
            this.appendArgs(delegate, '-record_lock_ddl_output', getRecordLockDdlOutput()?.absolutePath)
        }
    }

    def appendArgs(delegate, key, value) {
        project.logger.info('Optional arg: {} {}', key, value)
        if (value != null) {
            delegate.args key, value
        }
    }

    private def executeSystemDdlFiles(jdbcConf) {
        project.logger.info('Executing System DDLs: {}', jdbcConf.database.name)
        def files = []
        for (def file in getSystemDdlFiles()) {
            if (file != null) {
                files << project.file(file)
            }
        }
        if (getRecordLockDdlOutput() != null && getRecordLockDdlOutput().exists()) {
            files << getRecordLockDdlOutput()
        }
        executeDdlFiles jdbcConf, files
    }

    private def executeDdlFiles(jdbcConf, files) {
        project.logger.info('Executing DDLs: {}', files)
        if (!files.empty) {
            def conf = getAntSqlConf(jdbcConf)
            project.ant {
                sql(conf) {
                    path {
                        for (def file in files) {
                            pathelement(location: file.absolutePath)
                        }
                    }
                }
            }
        }
    }

    private def getAntSqlConf(jdbcConf) {
        def conf = [
            driver: jdbcConf.jdbc.driver,
            url: jdbcConf.jdbc.url,
            userid: jdbcConf.jdbc.user,
            password: jdbcConf.jdbc.password,
            classpath: getToolClasspathCollection().asPath
        ]
        if (getDdlEncoding()) {
            conf += [ encoding : getDdlEncoding() ]
        }
        return conf
    }
}
