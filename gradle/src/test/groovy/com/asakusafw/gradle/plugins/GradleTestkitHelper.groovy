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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildResultException
import org.gradle.util.GradleVersion

/**
 * Gradle Testkit helper for Asakusa Gradle plug-ins.
 * @since 0.8.1
 */
final class GradleTestkitHelper {

    private GradleTestkitHelper() {
        return
    }

    /**
     * Returns a simple Gradle build script for testing.
     * @param classpath the current plug-in classpath
     * @param plugins the required plug-ins
     * @return
     */
    static String getSimpleBuildScript(Set<File> classpath, String... plugins) {
        List<String> lines = new ArrayList<>()
        lines << 'buildscript {'
        lines << '  repositories {'
        lines << '    mavenCentral()'
        lines << '  }'
        lines << '  dependencies {'
        if (!classpath.empty) {
            lines << "    classpath files(${classpath.collect { "'''${it.absolutePath.replace('\\', '/')}'''" }.join(', ')})"
        }
        lines << "    classpath 'org.codehaus.groovy:groovy-backports-compat23:${GroovySystem.version}'"
        lines << '  }'
        lines << '}'
        for (String plugin : plugins) {
            lines << "apply plugin: '${plugin}'"
        }
        return lines.join('\n')
    }

    /**
     * Returns the set of classpath entries for the element resources.
     * @param resources a list of element classes or element resources
     * @return the classpath entries
     */
    static Set<File> toClasspath(Object... resources) {
        Set<File> results = new LinkedHashSet<>()
        for (Object resource : resources) {
            if (resource instanceof Class<?>) {
                results.add(toClasspathEntry((Class<?>) resource))
            } else if (resource instanceof String) {
                results.add(toClasspathEntry((String) resource))
            } else {
                throw new AssertionError(resource)
            }
        }
        return results
    }

    private static File toClasspathEntry(String resourcePath) {
        URL resource = AsakusaUpgradeTest.class.getClassLoader().getResource(resourcePath)
        if (resource == null) {
            throw new AssertionError(resourcePath)
        }
        return findLibraryFromUrl(resource, resourcePath)
    }

    private static File toClasspathEntry(Class<?> aClass) {
        URL resource = toUrl(aClass)
        if (resource == null) {
            throw new AssertionError(aClass)
        }
        String resourcePath = toResourcePath(aClass)
        return findLibraryFromUrl(resource, resourcePath)
    }

    private static String toResourcePath(Class<?> aClass) {
        return aClass.getName().replace('.', '/') + '.class'
    }

    private static URL toUrl(Class<?> aClass) {
        String className = aClass.getName()
        int start = className.lastIndexOf('.') + 1
        String name = className.substring(start)
        URL resource = aClass.getResource(name + '.class')
        return resource
    }

    private static Set<File> findLibrariesByResource(ClassLoader classLoader, String path) {
        Set<File> results = new LinkedHashSet<>()
        for (URL url : Collections.list(classLoader.getResources(path))) {
            File library = findLibraryFromUrl(url, path)
            if (library != null) {
                results.add(library)
            }
        }
        return results
    }

    private static File findLibraryFromUrl(URL resource, String resourcePath) {
        String protocol = resource.getProtocol()
        if (protocol.equals('file')) {
            File file = new File(resource.toURI())
            return toClassPathRoot(file, resourcePath)
        } else if (protocol.equals('jar')) {
            String path = resource.getPath()
            return toClassPathRoot(path, resourcePath)
        } else {
            throw new AssertionError(resource)
        }
    }

    private static File toClassPathRoot(File resourceFile, String resourcePath) {
        File current = resourceFile.getParentFile()
        for (int start = resourcePath.indexOf('/'); start >= 0; start = resourcePath.indexOf('/', start + 1)) {
            current = current.getParentFile()
            if (current == null || current.isDirectory() == false) {
                throw new AssertionError(resourceFile)
            }
        }
        return current
    }

    private static File toClassPathRoot(String uriQualifiedPath, String resourceName) {
        int entry = uriQualifiedPath.lastIndexOf('!')
        String qualifier
        if (entry >= 0) {
            qualifier = uriQualifiedPath.substring(0, entry)
        } else {
            qualifier = uriQualifiedPath
        }
        URI archive = new URI(qualifier)
        if (archive.getScheme().equals('file') == false) {
            throw new AssertionError(archive)
        }
        return new File(archive)
    }

    /**
     * Runs a Gradle build.
     * @param project the project directory
     * @param version the target Gradle version number
     * @param script the build script contents
     * @param tasks the task names to invoke
     */
    static void runGradle(File project, String version, String script, String... tasks) {
        File buildScript = new File(project, 'build.gradle')
        assert buildScript.createNewFile()
        buildScript.setText(script, 'UTF-8')
        BuildResult result
        try {
            result = GradleRunner.create()
                    .withGradleVersion(version)
                    .withProjectDir(project)
                    .withArguments(['-i', '-s', *tasks])
                    .build()
        } catch (UnexpectedBuildResultException t) {
            throw new AssertionError(script, t)
        }
        if (GradleVersion.version(version).compareTo(GradleVersion.version('2.5')) >= 0) {
            for (String task : tasks) {
                assert result.task(":${task}").outcome == TaskOutcome.SUCCESS
            }
        }
    }
}
