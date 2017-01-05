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
package com.asakusafw.testdriver.inprocess;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.compiler.CompilerConstants;

/**
 * Utilities for emulated executions.
 * @since 0.6.0
 * @version 0.8.0
 */
public final class EmulatorUtils {

    static final String KEY_PREFIX = "com.asakusafw.testdriver.inprocess"; //$NON-NLS-1$

    private EmulatorUtils() {
        return;
    }

    /**
     * Returns the batch library paths for the current context.
     * @param context the current context
     * @return the batch library paths
     */
    public static Collection<File> getBatchLibraryPaths(TestDriverContext context) {
        Collection<File> results = new ArrayList<>();
        File librariesPath = context.getLibrariesPackageLocation(context.getCurrentBatchId());
        if (librariesPath.isDirectory()) {
            for (File file : list(librariesPath)) {
                if (file.isFile() && file.getName().endsWith(".jar")) { //$NON-NLS-1$
                    results.add(file);
                }
            }
        }
        return results;
    }

    private static List<File> list(File file) {
        return Optional.ofNullable(file.listFiles())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns the jobflow library path for the current context.
     * @param context the current context
     * @return the jobflow library path (may not exist)
     */
    public static File getJobflowLibraryPath(TestDriverContext context) {
        File packagePath = context.getJobflowPackageLocation(context.getCurrentBatchId());
        File packageFile = new File(packagePath, CompilerConstants.getJobflowLibraryName(context.getCurrentFlowId()));
        return packageFile;
    }

    /**
     * Returns whether the target command path has the specified suffix.
     * @param command the target command
     * @param suffix the suffix
     * @return {@code true} if the command path has the specified suffix, otherwise {@code false}
     * @since 0.8.0
     */
    public static boolean hasCommandSuffix(String command, String suffix) {
        List<String> commandList = toPathSegments(command);
        List<String> suffixList = toPathSegments(suffix);
        int commandSize = commandList.size();
        int suffixSize = suffixList.size();
        if (commandSize < suffixSize) {
            return false;
        }
        List<String> commandRegion = commandList.subList(commandSize - suffixSize, commandSize);
        return commandRegion.equals(suffixList);
    }

    private static List<String> toPathSegments(String path) {
        return Stream.of(new File(path).getPath().split(Pattern.quote(File.separator)))
                .filter(s -> s.isEmpty() == false)
                .collect(Collectors.toList());
    }
}
