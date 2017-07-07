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

import java.lang.reflect.Modifier

import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.ReflectionCache
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.util.ConfigureUtil
import org.gradle.util.GradleVersion

import com.asakusafw.gradle.plugins.PluginParticipant

/**
 * Basic utilities for Gradle plug-ins.
 * @since 0.7.4
 * @version 0.9.2
 */
final class PluginUtils {

    /**
     * The property name of module versions.
     * @since 0.9.0
     */
    public static final String PROPERTY_VERSION = 'version'

    /**
     * Executes a closure after the project was evaluated only if evaluation was not failed.
     * @param project the target project
     * @param closure the closure
     */
    static void afterEvaluate(Project project, Closure<?> closure) {
        project.afterEvaluate { Project p, ProjectState state ->
            if (state.failure != null) {
                return
            }
            closure.call(project)
        }
    }

    /**
     * Compares the target Gradle version with the current Gradle version.
     * @param version the target Gradle version
     * @return {@code =0} - same, {@code >0} - the current version is newer, {@code <0} - the current version is older
     */
    static int compareGradleVersion(String version) {
        GradleVersion current = GradleVersion.current()
        GradleVersion target = GradleVersion.version(version)
        return current.compareTo(target)
    }

    /**
     * Calls a closure after the target plug-in is enabled.
     * @param project the current project
     * @param pluginType the target plug-in class
     * @param closure the closure
     */
    static void afterPluginEnabled(Project project, Class<?> pluginType, Closure<?> closure) {
        project.plugins.withType(pluginType) {
            closure.call()
        }
    }

    /**
     * Calls a closure after the target plug-in is enabled.
     * @param project the current project
     * @param pluginId the target plug-in ID
     * @param closure the closure
     */
    static void afterPluginEnabled(Project project, String pluginId, Closure<?> closure) {
        if (compareGradleVersion('2.0') >= 0) {
            project.plugins.withId(pluginId) {
                closure.call()
            }
        } else {
            project.plugins.matching({ it == project.plugins.findPlugin(pluginId) }).all {
                closure.call()
            }
        }
    }

    /**
     * Calls a closure after the target task is enabled.
     * @param project the current project
     * @param taskName the target task name
     * @param closure the closure
     * @since 0.8.0
     */
    static void afterTaskEnabled(Project project, String taskName, Closure<?> closure) {
        project.tasks.matching { Task t ->
            t.project == project && t.name == taskName
        }.all { Task t ->
            closure.call(t)
        }
    }

    /**
     * Make modifying <code>asakusafwVersion</code> deprecated.
     * @param project the current project
     * @param prefix the instance prefix name
     * @param instance the target instance
     * @return the original instance
     * @since 0.8.1
     */
    static <T> T deprecateAsakusafwVersion(Project project, String prefix, T instance) {
        // must declare getter explicitly for older Gradle versions (e.g. 1.12)
        def getter = instance.&getAsakusafwVersion
        instance.metaClass.getAsakusafwVersion = { ->
            return getter()
        }
        instance.metaClass.setAsakusafwVersion = { String arg ->
            project.logger.warn "DEPRECATED: changing ${prefix}.asakusafwVersion is ignored."
        }
        // asakusafwVersion(String) does not via Groovy MOP when calls setAsakusafwVersion()
        instance.metaClass.asakusafwVersion = { String arg ->
            project.logger.warn "DEPRECATED: changing ${prefix}.asakusafwVersion is ignored."
        }
        return instance
    }

    /**
     * Finds for services.
     * @param <T> the service interface type
     * @param project the current project
     * @param serviceInterface the service interface
     * @param loader the service class loader
     * @return the loaded services
     * @since 0.9.0
     */
    static void applyParticipants(Project project, Class<? extends PluginParticipant> type) {
        // We always load implementations from the interface class loader.
        // A class loader relying on the project might not load the target interface.
        ClassLoader loader = type.classLoader
        ServiceLoader<? extends PluginParticipant> services = ServiceLoader.load(type)
        for (Iterator<? extends PluginParticipant> iter = services.iterator(); iter.hasNext();) {
            try {
                PluginParticipant participant = iter.next()
                project.logger.info "applying participant: ${participant.name} (${participant.descriptor})"
                project.apply plugin: participant.descriptor
            } catch (ServiceConfigurationError e) {
                project.logger.warn "error occurred while loading service: ${type.name}", e
            }
        }
    }

    /**
     * Injects the {@code version} property into the given container.
     * @param container the target container
     * @param version the version value
     * @since 0.9.0
     */
    static void injectVersionProperty(ExtensionAware container, Object version) {
        if (!(container instanceof ExtensionAware)) {
            throw new IllegalStateException()
        }
        ExtensionContainer extensions = container.extensions
        FeatureVersionExtension value = new FeatureVersionExtension(version)
        if (extensions instanceof Convention) {
            extensions.plugins['asakusafw-version'] = value
        } else {
            extensions.add(PROPERTY_VERSION, value.version)
        }
    }

    /**
     * Enhances {@code NamedDomainObjectContainer} for enabling property accesses.
     * @param <T> the element type
     * @param container the target container
     * @return the enhanced container
     * @since 0.9.1
     */
    static <T> NamedDomainObjectContainer<T> enhanceNamedDomainObjectContainer(NamedDomainObjectContainer<T> container) {
        if (compareGradleVersion('3.4') >= 0
                && container.hasProperty('extensions')
                && container.extensions.hasProperty('plugins')
                && container.extensions.plugins instanceof Map<?, ?>
                && container.extensions.plugins.containsKey('asakusafw-enhanced') == false) {
            container.extensions.plugins.put('asakusafw-enhanced', new NamedDomainObjectContainerPlugin(container))
        } else {
            container.metaClass.with {
                propertyMissing = { String name ->
                    return container.maybeCreate(name)
                }
                methodMissing = { String name, args ->
                    if (args.size() == 1 && args[0] instanceof Closure<?>) {
                        return ConfigureUtil.configure(args[0], container.maybeCreate(name))
                    }
                    throw new MissingMethodException(name, NamedDomainObjectContainer, args)
                }
            }
        }
        return container
    }

    private static final class NamedDomainObjectContainerPlugin {

        private final NamedDomainObjectContainer<?> container

        NamedDomainObjectContainerPlugin(NamedDomainObjectContainer<?> container) {
            this.metaClass = new NamedDomainObjectContainerMeta(metaClass, container)
        }
    }

    private static final class NamedDomainObjectContainerMeta extends DelegatingMetaClass {

        private static final CachedClass DECL_CLASS = ReflectionCache.getCachedClass(NamedDomainObjectContainerPlugin)

        private final NamedDomainObjectContainer<?> container

        NamedDomainObjectContainerMeta(MetaClass forward, NamedDomainObjectContainer<?> container) {
            super(forward)
            this.container = container
        }

        @Override
        MetaProperty getMetaProperty(String name) {
            MetaProperty existing = super.getMetaProperty(name)
            if (existing) {
                return existing
            }
            return new MetaProperty(name, container.getType()) {
                Object getProperty(Object object) {
                    return container.maybeCreate(name)
                }
                void setProperty(Object object, Object value) {
                    throw new UnsupportedOperationException()
                }
            }
        }

        @Override
        MetaMethod pickMethod(String methodName, Class[] args) {
            MetaProperty existing = super.pickMethod(methodName, args)
            if (existing) {
                return existing
            }
            if (args.size() == 1 && Closure.class.isAssignableFrom(args[0])) {
                return newMetaCallable(methodName)
            }
            return null
        }

        @Override
        MetaMethod getMetaMethod(String methodName, Object[] args) {
            MetaProperty existing = super.getMetaMethod(methodName, args)
            if (existing) {
                return existing
            }
            if (args.size() == 1
                    && (args[0] instanceof Closure<?> || Closure.class.isAssignableFrom(args[0]))) {
                return newMetaCallable(methodName)
            }
            return null
        }

        private MetaMethod newMetaCallable(String methodName) {
            return new MetaMethod([Closure]) {
                CachedClass getDeclaringClass() {
                    return DECL_CLASS
                }
                int getModifiers() {
                    return Modifier.PUBLIC
                }
                String getName() {
                    return methodName
                }
                Class<?> getReturnType() {
                    return container.getType()
                }
                Object invoke(Object object, Object[] arguments) {
                    return ConfigureUtil.configure(arguments[0], container.maybeCreate(methodName))
                }
            }
        }
    }

    /**
     * Returns a set of class files output directories.
     * @param project the current project
     * @param output the target output
     * @return the set of class files output directories
     * @since 0.9.2
     */
    public static FileCollection getClassesDirs(Project project, SourceSetOutput output) {
        if (output.hasProperty('classesDirs')) {
            return output.classesDirs
        } else {
            return project.files({ output.classesDir })
        }
    }

    private PluginUtils() {
    }
}
