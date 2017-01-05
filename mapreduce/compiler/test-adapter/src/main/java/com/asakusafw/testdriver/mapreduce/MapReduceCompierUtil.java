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
package com.asakusafw.testdriver.mapreduce;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.testdriver.compiler.CommandToken;
import com.asakusafw.testdriver.compiler.CompilerConfiguration.DebugLevel;
import com.asakusafw.testdriver.compiler.CompilerConfiguration.OptimizeLevel;

/**
 * Utilities for MapReduce compiler.
 * @since 0.8.0
 */
final class MapReduceCompierUtil {

    private static final String PH_HOME = "{{__PH__:HOME}}"; //$NON-NLS-1$

    private static final String PH_EXECUTION_ID = "{{__PH__:EXECUTION_ID}}"; //$NON-NLS-1$

    private static final String PH_BATCH_ARGUMENTS = "{{__PH__:BATCH_ARGUMENTS}}"; //$NON-NLS-1$

    private MapReduceCompierUtil() {
        return;
    }

    public static List<File> computeEmbeddedLibraries(
            MapReduceCompilerConfiguration configuration, Class<?> userClass) {
        List<File> results = new ArrayList<>();
        results.add(DirectFlowCompiler.toLibraryPath(userClass));
        results.addAll(configuration.getClasspathEntries());
        return results;
    }

    public static File createTemporaryDirectory(File baseDirectory) throws IOException {
        File f = new File(baseDirectory, UUID.randomUUID().toString());
        if (f.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "error occurred while creating a temporary directory: {0}",
                    f));
        }
        return f;
    }

    public static Location createInputLocation(String basePath, String name) {
        Location location = Location.fromPath(basePath, '/')
                .append(StageConstants.EXPR_EXECUTION_ID)
                .append("input") //$NON-NLS-1$
                .append(normalize(name));
        return location;
    }

    public static Location createOutputLocation(String basePath, String name) {
        Location location = Location.fromPath(basePath, '/')
                .append(StageConstants.EXPR_EXECUTION_ID)
                .append("output") //$NON-NLS-1$
                .append(normalize(name))
                .asPrefix();
        return location;
    }

    public static Location createWorkingLocation(String basePath) {
        Location location = Location.fromPath(basePath, '/')
                .append(StageConstants.EXPR_EXECUTION_ID)
                .append("temp"); //$NON-NLS-1$
        return location;
    }

    private static String normalize(String name) {
        // Hadoop MultipleInputs/Outputs only can accept alphameric characters
        StringBuilder buf = new StringBuilder();
        for (char c : name.toCharArray()) {
            // we use '0' as escape symbol
            if ('1' <= c && c <= '9' || 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z') {
                buf.append(c);
            } else if (c <= 0xff) {
                buf.append('0');
                buf.append(String.format("%02x", (int) c)); //$NON-NLS-1$
            } else {
                buf.append("0u"); //$NON-NLS-1$
                buf.append(String.format("%04x", (int) c)); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }

    public static CommandContext createMockCommandContext() {
        return new CommandContext(PH_HOME, PH_EXECUTION_ID, PH_BATCH_ARGUMENTS);
    }

    public static String resolveCommand(String file) {
        if (file.startsWith(PH_HOME) == false) {
            throw new IllegalStateException(file);
        }
        return file.substring(PH_HOME.length());
    }

    public static List<CommandToken> resolveArguments(List<String> tokens) {
        List<CommandToken> results = new ArrayList<>();
        for (String token : tokens) {
            if (isPlaceholder(token, PH_EXECUTION_ID)) {
                results.add(CommandToken.EXECUTION_ID);
            } else if (isPlaceholder(token, PH_BATCH_ARGUMENTS)) {
                results.add(CommandToken.BATCH_ARGUMENTS);
            } else if (isPlaceholder(token, PH_HOME)) {
                throw new IllegalStateException(token);
            } else {
                results.add(CommandToken.of(token));
            }
        }
        return results;
    }

    public static FlowCompilerOptions toFlowCompilerOptions(
            OptimizeLevel optimizeLevel,
            DebugLevel debugLevel,
            Map<String, String> extraOptions) {
        FlowCompilerOptions options = new FlowCompilerOptions();
        switch (optimizeLevel) {
        case DISABLED:
            options.setCompressConcurrentStage(false);
            options.setCompressFlowPart(false);
            options.setHashJoinForSmall(false);
            options.setHashJoinForTiny(false);
            options.setEnableCombiner(false);
            break;
        case NORMAL:
            options.setCompressConcurrentStage(FlowCompilerOptions.Item.compressConcurrentStage.defaultValue);
            options.setCompressFlowPart(FlowCompilerOptions.Item.compressFlowPart.defaultValue);
            options.setHashJoinForSmall(FlowCompilerOptions.Item.hashJoinForSmall.defaultValue);
            options.setHashJoinForTiny(FlowCompilerOptions.Item.hashJoinForTiny.defaultValue);
            options.setEnableCombiner(FlowCompilerOptions.Item.enableCombiner.defaultValue);
            break;
        case AGGRESSIVE:
            options.setCompressConcurrentStage(true);
            options.setCompressFlowPart(true);
            options.setHashJoinForSmall(true);
            options.setHashJoinForTiny(true);
            options.setEnableCombiner(true);
            break;
        default:
            throw new AssertionError(optimizeLevel);
        }
        switch (debugLevel) {
        case DISABLED:
            options.setEnableDebugLogging(false);
            break;
        case NORMAL:
        case VERBOSE:
            options.setEnableDebugLogging(true);
            break;
        default:
            throw new AssertionError(debugLevel);
        }
        for (Map.Entry<String, String> entry : extraOptions.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            options.putExtraAttribute(name, value);
        }
        return options;
    }

    private static boolean isPlaceholder(String token, String placeholder) {
        if (token.equals(placeholder)) {
            return true;
        }
        if (token.indexOf(placeholder) >= 0) {
            throw new IllegalStateException(token);
        }
        return false;
    }
}
