/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver.compiler.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.testdriver.compiler.CompilerConfiguration;

/**
 * A basic implementation of {@link CompilerConfiguration}.
 * @since 0.8.0
 */
public class BasicCompilerConfiguration implements CompilerConfiguration {

    private ClassLoader classLoader = getClass().getClassLoader();

    private File workingDirectory;

    private final Map<String, String> options = new LinkedHashMap<>();

    private OptimizeLevel optimizeLevel = OptimizeLevel.NORMAL;

    private DebugLevel debugLevel = DebugLevel.DISABLED;

    private final List<File> classpathEntries = new ArrayList<>();

    private final Map<Class<?>, Object> extensions = new LinkedHashMap<>();

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public CompilerConfiguration withClassLoader(ClassLoader newValue) {
        this.classLoader = newValue;
        return this;
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public CompilerConfiguration withWorkingDirectory(File newValue) {
        this.workingDirectory = newValue;
        return this;
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public CompilerConfiguration withOption(String key, String value) {
        if (value == null) {
            options.remove(key);
        } else {
            options.put(key, value);
        }
        return this;
    }

    @Override
    public CompilerConfiguration withOptions(Map<String, String> newValue) {
        for (Map.Entry<String, String> entry : newValue.entrySet()) {
            withOption(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public OptimizeLevel getOptimizeLevel() {
        return optimizeLevel;
    }

    @Override
    public CompilerConfiguration withOptimizeLevel(OptimizeLevel newValue) {
        this.optimizeLevel = newValue;
        return this;
    }

    @Override
    public DebugLevel getDebugLevel() {
        return debugLevel;
    }

    @Override
    public CompilerConfiguration withDebugLevel(DebugLevel newValue) {
        this.debugLevel = newValue;
        return this;
    }

    @Override
    public List<File> getClasspathEntries() {
        return classpathEntries;
    }

    @Override
    public CompilerConfiguration withClasspathEntry(File classpathEntry) {
        this.classpathEntries.add(classpathEntry);
        return this;
    }

    /**
     * Returns the extensions.
     * @return the extensions
     */
    public Map<Class<?>, Object> getExtensions() {
        return extensions;
    }

    @Override
    public <T> T getExtension(Class<T> type) {
        return type.cast(extensions.get(type));
    }

    @Override
    public <T> CompilerConfiguration withExtension(Class<T> type, T value) {
        if (value == null) {
            this.extensions.remove(type);
        } else {
            this.extensions.put(type, value);
        }
        return this;
    }
}
