/**
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
package com.asakusafw.testdriver.compiler;

import java.io.File;

/**
 * Constants for Asakusa DSL TestKit Compiler.
 * @since 0.8.0
 */
public final class CompilerConstants {

    /**
     * The system property key of runtime working directory.
     * This working directory must be a relative path from the default working directory.
     */
    public static final String KEY_RUNTIME_WORKING_DIRECTORY = "asakusa.testdriver.hadoopwork.dir"; //$NON-NLS-1$

    private static final String DEFAULT_RUNTIME_WORKING_DIRECTORY = "target/testdriver/hadoopwork"; //$NON-NLS-1$

    /**
     * Returns the path to the runtime working directory.
     * Clients can configure this property using system property {@value #KEY_RUNTIME_WORKING_DIRECTORY}.
     * @return the runtime working directory
     */
    public static String getRuntimeWorkingDirectory() {
        String dir = System.getProperty(KEY_RUNTIME_WORKING_DIRECTORY);
        if (dir == null) {
            return DEFAULT_RUNTIME_WORKING_DIRECTORY;
        }
        return dir;
    }

    /**
     * Returns the path of the jobflow library path (from the batch directory).
     * @param batchDirectory the batch directory
     * @param flowId the target flow ID
     * @return the jobflow library path
     */
    public static File getJobflowLibraryPath(File batchDirectory, String flowId) {
        File libs = new File(batchDirectory, "lib"); //$NON-NLS-1$
        File result = new File(libs, getJobflowLibraryName(flowId));
        return result;
    }

    /**
     * Returns the file name of the jobflow library.
     * @param flowId the target flow ID
     * @return the related jobflow package name
     */
    public static String getJobflowLibraryName(String flowId) {
        return String.format("jobflow-%s.jar", flowId); //$NON-NLS-1$
    }

    private CompilerConstants() {
        return;
    }
}
