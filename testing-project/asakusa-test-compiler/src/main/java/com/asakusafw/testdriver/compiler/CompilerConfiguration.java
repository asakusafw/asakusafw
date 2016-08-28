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
package com.asakusafw.testdriver.compiler;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Represents a configuration for {@link CompilerSession}.
 * @since 0.8.0
 */
public interface CompilerConfiguration {

    /**
     * Returns the class loader.
     * @return the class loader
     */
    ClassLoader getClassLoader();

    /**
     * Returns the working directory.
     * @return the working directory
     */
    File getWorkingDirectory();

    /**
     * Returns the options.
     * @return the options
     */
    Map<String, String> getOptions();

    /**
     * Returns the optimize level.
     * @return the optimize level
     */
    OptimizeLevel getOptimizeLevel();

    /**
     * Returns the debug level.
     * @return the debug level
     */
    DebugLevel getDebugLevel();

    /**
     * Returns the additional classpath entries.
     * This may not include application classes.
     * @return the additional classpath entries
     */
    List<File> getClasspathEntries();

    /**
     * Returns an extension.
     * @param <T> the extension type
     * @param type the extension type
     * @return the related extension object, or {@code null} if it is not defined
     */
    <T> T getExtension(Class<T> type);

    /**
     * Sets the base class loader.
     * @param newValue the class loader
     * @return this
     */
    CompilerConfiguration withClassLoader(ClassLoader newValue);

    /**
     * Sets a compiler specific option.
     * @param key the option key
     * @param value the option value
     * @return this
     */
    CompilerConfiguration withOption(String key, String value);

    /**
     * Sets compiler specific options.
     * @param newValue the option map
     * @return this
     */
    CompilerConfiguration withOptions(Map<String, String> newValue);

    /**
     * Sets the optimization level.
     * @param newValue the optimization level
     * @return this
     */
    CompilerConfiguration withOptimizeLevel(OptimizeLevel newValue);

    /**
     * Sets the debug level.
     * @param newValue the debug level
     * @return this
     */
    CompilerConfiguration withDebugLevel(DebugLevel newValue);

    /**
     * Sets the compiler working directory.
     * @param newValue the working directory
     * @return this
     */
    CompilerConfiguration withWorkingDirectory(File newValue);

    /**
     * Adds an additional classpath entry.
     * @param classpathEntry the classpath entry
     * @return this
     */
    CompilerConfiguration withClasspathEntry(File classpathEntry);

    /**
     * Adds a compiler extension.
     * @param <T> the extension type
     * @param type the extension type
     * @param value the extension object
     * @return this
     */
    <T> CompilerConfiguration withExtension(Class<T> type, T value);

    /**
     * Represents an optimization level.
     * @since 0.8.0
     */
    enum OptimizeLevel {

        /**
         * Disables optimization.
         */
        DISABLED,

        /**
         * Enables standard optimizations.
         */
        NORMAL,

        /**
         * Enables aggressive optimizations.
         */
        AGGRESSIVE,
    }

    /**
     * Represents a debug level.
     * @since 0.8.0
     */
    enum DebugLevel {

        /**
         * No debugging information.
         */
        DISABLED,

        /**
         * Normal debugging information.
         */
        NORMAL,

        /**
         * Verbose debugging information.
         */
        VERBOSE,
    }
}
