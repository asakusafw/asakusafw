/**
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
package com.asakusafw.testdriver.inprocess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.testdriver.TestDriverContext;

/**
 * Utilities for emulated executions.
 * @since 0.6.0
 */
public final class EmulatorUtils {

    private EmulatorUtils() {
        return;
    }

    /**
     * Returns the batch library paths for the current context.
     * @param context the current context
     * @return the batch library paths
     */
    public static Collection<File> getBatchLibraryPaths(TestDriverContext context) {
        Collection<File> results = new ArrayList<File>();
        File librariesPath = context.getLibrariesPackageLocation(context.getCurrentBatchId());
        if (librariesPath.isDirectory()) {
            for (File file : librariesPath.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    /**
     * Returns the jobflow library path for the current context.
     * @param context the current context
     * @return the jobflow library path (may not exist)
     */
    public static File getJobflowLibraryPath(TestDriverContext context) {
        File packagePath = context.getJobflowPackageLocation(context.getCurrentBatchId());
        File packageFile = new File(packagePath, Naming.getJobflowClassPackageName(context.getCurrentFlowId()));
        return packageFile;
    }
}
