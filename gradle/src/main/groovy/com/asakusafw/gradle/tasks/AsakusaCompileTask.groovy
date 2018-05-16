/*
 * Copyright 2011-2018 Asakusa Framework Team.
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

import groovy.transform.PackageScope

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

import com.asakusafw.gradle.tasks.internal.ResolutionUtils
import com.asakusafw.gradle.tasks.internal.ToolLauncherUtils

/**
 * Gradle Task for Asakusa DSL compiler framework.
 * @since 0.8.0
 * @version 0.8.1
 */
class AsakusaCompileTask extends DefaultTask {

    /**
     * The compiler name.
     * @since 0.8.0
     */
    String compilerName = 'Asakusa DSL compiler'

    /**
     * The maximum heap size.
     */
    String maxHeapSize

    /**
     * The tool launcher class libraries (can empty).
     */
    List<Object> launcherClasspath = []

    /**
     * Returns each file of {@link #launcherClasspath}.
     * @return each file
     */
    @InputFiles
    FileCollection getLauncherClasspathFiles() {
        return collectFiles(getLauncherClasspath())
    }

    /**
     * The compiler class libraries.
     */
    List<Object> toolClasspath = []

    /**
     * Returns each file of {@link #toolClasspath}.
     * @return each file
     */
    @InputFiles
    FileCollection getToolClasspathFiles() {
        return collectFiles(getToolClasspath())
    }

    /**
     * The Java system properties.
     */
    Map<Object, Object> systemProperties = [:]

    /**
     * Returns the actual values of {@link #systemProperties system properties}.
     * @return system properties for compiler process
     */
    @Input
    Map<String, String> getResolvedSystemProperties() {
        return ResolutionUtils.resolveToStringMap(getSystemProperties())
    }

    /**
     * The Java VM arguments.
     */
    List<Object> jvmArgs = []

    /**
     * Returns the actual values of {@link #jvmArgs}.
     * @return the Java VM arguments
     */
    @Input
    List<String> getResolvedJvmArgs() {
        return ResolutionUtils.resolveToStringList(getJvmArgs())
    }

    /**
     * The library paths with batch classes.
     */
    List<Object> explore = []

    /**
     * Returns each file of {@link #explore}.
     * @return each file
     */
    @SkipWhenEmpty
    @InputFiles
    FileCollection getExploreFiles() {
        return collectFiles(getExplore())
    }

    /**
     * The library paths to be attached to each batch package.
     */
    List<Object> attach = []

    /**
     * Returns each file of {@link #attach}.
     * @return each file
     */
    @InputFiles
    FileCollection getAttachFiles() {
        return collectFiles(getAttach())
    }

    /**
     * The library paths to be embedded to each jobflow package.
     */
    List<Object> embed = []

    /**
     * Returns each file of {@link #embed}.
     * @return each file
     */
    @InputFiles
    FileCollection getEmbedFiles() {
        return collectFiles(getEmbed())
    }

    /**
     * The external library paths.
     */
    List<Object> external = []

    /**
     * Returns each file of {@link #external}.
     * @return each file
     */
    @InputFiles
    FileCollection getExternalFiles() {
        return collectFiles(getExternal())
    }

    /**
     * The accepting batch class name patterns ({@code "*"} as a wildcard character).
     */
    List<Object> include = []

    /**
     * Returns the actual values of {@link #include}.
     * @return accepting batch class name patterns
     */
    @Input
    List<String> getResolvedInclude() {
        return ResolutionUtils.resolveToStringList(getInclude())
    }

    /**
     * The ignoring batch class name patterns ({@code "*"} as a wildcard character).
     */
    List<Object> exclude = []

    /**
     * Returns the actual values of {@link #exclude}.
     * @return ignoring batch class name patterns
     */
    @Input
    List<String> getResolvedExclude() {
        return ResolutionUtils.resolveToStringList(getExclude())
    }

    /**
     * The custom data model processor classes.
     */
    List<Object> customDataModelProcessors = []

    /**
     * Returns the actual values of {@link #customDataModelProcessors}.
     * @return the target class names
     */
    @Input
    List<String> getResolvedCustomDataModelProcessors() {
        return ResolutionUtils.resolveToStringList(getCustomDataModelProcessors())
    }

    /**
     * The custom external port processor classes.
     */
    List<Object> customExternalPortProcessors = []

    /**
     * Returns the actual values of {@link #customExternalPortProcessors}.
     * @return the target class names
     */
    @Input
    List<String> getResolvedCustomExternalPortProcessors() {
        return ResolutionUtils.resolveToStringList(getCustomExternalPortProcessors())
    }

    /**
     * The custom jobflow processor classes.
     */
    List<Object> customJobflowProcessors = []

    /**
     * Returns the actual values of {@link #customJobflowProcessors}.
     * @return the target class names
     */
    @Input
    List<String> getResolvedCustomJobflowProcessors() {
        return ResolutionUtils.resolveToStringList(getCustomJobflowProcessors())
    }

    /**
     * The custom batch processor classes.
     */
    List<Object> customBatchProcessors = []

    /**
     * Returns the actual values of {@link #customBatchProcessors}.
     * @return the target class names
     */
    @Input
    List<String> getResolvedCustomBatchProcessors() {
        return ResolutionUtils.resolveToStringList(getCustomBatchProcessors())
    }

    /**
     * The custom compiler participant classes.
     */
    List<Object> customParticipants = []

    /**
     * Returns the actual values of {@link #customParticipants}.
     * @return the target class names
     */
    @Input
    List<String> getResolvedCustomParticipants() {
        return ResolutionUtils.resolveToStringList(getCustomParticipants())
    }

    /**
     * The custom runtime working directory URI.
     */
    @Optional
    @Input
    String runtimeWorkingDirectory

    /**
     * The compiler properties.
     */
    Map<Object, Object> compilerProperties = [:]

    /**
     * Returns the actual values of {@link #compilerProperties}.
     * @return the compiler properties
     */
    @Input
    Map<String, String> getResolvedCompilerProperties() {
        return ResolutionUtils.resolveToStringMap(getCompilerProperties())
    }

    /**
     * The batch ID prefix for applications.
     */
    @Optional
    @Input
    String batchIdPrefix

    /**
     * The batch application output base path.
     */
    @OutputDirectory
    File outputDirectory

    /**
     * Whether fails on compilation errors or not.
     */
    boolean failOnError

    /**
     * Whether clean-up output directory before compile applications or not.
     */
    @Input
    boolean clean

    /**
     * Adds extra {@link #compilerProperties compiler properties}.
     * @param encoded the encoded {@code key=value} entries separated by comma
     */
    @Option(option = 'compiler-properties', description = 'extra compiler properties separated by comma')
    void setExtraCompilerPropertiesOption(String encoded) {
        getCompilerProperties().putAll(decodeMap(encoded))
    }

    /**
     * Adds extra {@link #compilerProperties compiler properties}.
     * This is an alias of {@link #setExtraCompilerPropertiesOption(String)}.
     * @param encoded the encoded {@code key=value} entries separated by comma
     * @since 0.8.1
     */
    @Option(option = 'options', description = 'extra compiler properties separated by comma')
    void setExtraCompilerOptionsOption(String encoded) {
        setExtraCompilerPropertiesOption(encoded)
    }

    /**
     * Set the batch ID prefix for applications.
     * @param value the batch ID prefix
     */
    @Option(option = 'batch-id-prefix', description = 'the batch ID prefix')
    void setBatchIdPrefixOption(String value) {
        setBatchIdPrefix(value)
    }

    /**
     * Set whether fails on compilation errors or not.
     * @param value {@code "true"} to fail on compilation error, otherwise ignore errors
     */
    @Option(option = 'fail-on-error', description = 'whether fails on compilation errors or not')
    void setFailOnErrorOption(String value) {
        setFailOnError(Boolean.valueOf(value))
    }

    /**
     * Set the update target batch class name.
     * With this, the compiler only builds the target batch class,
     * and the {@link #include} {@link #exclude} will be ignored.
     * @param className the target class name pattern
     */
    @Option(option = 'update', description = 'compiles the specified batch class only')
    void setUpdateOption(String className) {
        setClean(false)
        setInclude([className])
        setExclude([])
    }

    private Map<String, String> decodeMap(String encoded) {
        Map<String, String> map = [:]
        StringBuilder buf = new StringBuilder()
        boolean sawEscape = false
        for (char c in encoded.toCharArray()) {
            if (sawEscape) {
                buf.append c
                sawEscape = false
            } else if (c == '\\') {
                sawEscape = true
            } else if (c == ',') {
                addKeyValue buf.toString(), map
                buf.setLength 0
            } else {
                buf.append c
            }
        }
        addKeyValue buf.toString(), map
        return map
    }

    private void addKeyValue(String string, Map<String, String> kvs) {
        int index = string.indexOf '='
        if (index < 0) {
            throw new InvalidUserDataException("Invalid key-value: \"${string}\"")
        }
        kvs.put string.substring(0, index), string.substring(index + 1)
    }

    private FileCollection collectFiles(Object files) {
        Object all = project.files(files).collect { File f ->
            if (f.isFile()) {
                return [f]
            } else if (f.isDirectory()) {
                return project.fileTree(f)
            } else {
                return []
            }
        }
        return project.files(all)
    }

    /**
     * Task Action of this task.
     */
    @TaskAction
    void perform() {
        String javaMain = 'com.asakusafw.lang.compiler.cli.BatchCompilerCli'
        FileCollection javaClasspath = project.files(getToolClasspath())
        List<String> javaArguments = createArguments()
        FileCollection launcher = project.files(getLauncherClasspath())
        if (!launcher.empty) {
            logger.info "Starting ${getCompilerName()} using launcher"
            File script = ToolLauncherUtils.createLaunchFile(this, javaClasspath, javaMain, javaArguments)
            javaMain = ToolLauncherUtils.MAIN_CLASS
            javaClasspath = launcher
            javaArguments = [script.absolutePath]
        }

        if (isClean()) {
            logger.info "Cleaning ${getCompilerName()} output directory"
            project.delete getOutputDirectory()
        }
        if (getOutputDirectory().exists() == false) {
            project.mkdir getOutputDirectory()
        }

        project.javaexec { JavaExecSpec spec ->
            spec.main = javaMain
            spec.classpath = javaClasspath
            spec.jvmArgs = getResolvedJvmArgs()
            if (getMaxHeapSize() != null) {
                spec.maxHeapSize = getMaxHeapSize()
            }
            spec.systemProperties getResolvedSystemProperties()
            spec.systemProperties getExtraSystemProperties()
            spec.enableAssertions = true
            spec.args = javaArguments
        }
    }

    @PackageScope
    Map<String, String> getExtraSystemProperties() {
        return [
            'com.asakusafw.batchapp.build.timestamp' : new Date().format('yyyy-MM-dd HH:mm:ss (z)'),
            'com.asakusafw.batchapp.build.java.version' : System.getProperty('java.version', '?')
        ]
    }

    private List<String> createArguments() {
        List<String> results = []

        // project repository
        configureFiles(results, '--explore', getExplore())
        configureFiles(results, '--attach', getAttach())
        configureFiles(results, '--embed', getEmbed())
        configureFiles(results, '--external', getExternal())

        // batch class detection
        configureClasses(results, '--include', getResolvedInclude())
        configureClasses(results, '--exclude', getResolvedExclude())

        // custom compiler plug-ins
        configureClasses(results, '--dataModelProcessors', getResolvedCustomDataModelProcessors())
        configureClasses(results, '--externalPortProcessors', getResolvedCustomExternalPortProcessors())
        configureClasses(results, '--jobflowProcessors', getResolvedCustomJobflowProcessors())
        configureClasses(results, '--batchProcessors', getResolvedCustomBatchProcessors())
        configureClasses(results, '--participants', getResolvedCustomParticipants())

        // other options
        configureString(results, '--output', getOutputDirectory().getAbsolutePath())
        configureString(results, '--runtimeWorkingDirectory', getRuntimeWorkingDirectory())
        configureString(results, '--batchIdPrefix', getBatchIdPrefix())
        configureBoolean(results, '--failOnError', getFailOnError())
        getResolvedCompilerProperties().each { k, v ->
            results << '--property' << "${k}=${v}"
        }

        return results
    }

    private void configureBoolean(List<String> arguments, String key, boolean value) {
        if (value == false) {
            return
        }
        logger.debug("Asakusa compiler option: ${key}")
        arguments << key
    }

    private void configureString(List<String> arguments, String key, Object value) {
        if (value == null) {
            return
        }
        String s = String.valueOf(value)
        if (s.isEmpty()) {
            return
        }
        logger.debug("Asakusa compiler option: ${key}=${s}")
        arguments << key << s
    }

    private void configureFiles(List<String> arguments, String key, Object files) {
        FileCollection f = project.files(files)
        if (f.isEmpty()) {
            return
        }
        configureString(arguments, key, f.asPath)
    }

    private void configureClasses(List<String> arguments, String key, List<String> classes) {
        if (classes.isEmpty()) {
            return
        }
        configureString(arguments, key, classes.join(','))
    }
}
