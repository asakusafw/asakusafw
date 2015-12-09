/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.Blob;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.VariableResolver;
import com.asakusafw.yaess.core.util.PropertiesUtil;
import com.asakusafw.yaess.core.util.StreamRedirectTask;
import com.asakusafw.yaess.core.util.TemporaryFiles;

/**
 * Utilities for Processes.
 * @since 0.2.3
 */
final class ProcessUtil {

    static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    /**
     * The (sub) key prefix of executable command line tokens.
     */
    public static final String PREFIX_COMMAND = "command.";

    /**
     * The (sub) key prefix of setup command line tokens.
     */
    public static final String PREFIX_SETUP = "setup.";

    /**
     * The (sub) key prefix of cleanup command line tokens.
     */
    public static final String PREFIX_CLEANUP = "cleanup.";

    private static final String BLOB_FILE_PREFIX = "yaess-blob-";

    private static final Pattern ARGUMENT = Pattern.compile("@\\[(0|[1-9][0-9]{0,3})\\]");

    private static final ExecutorService REDIRECT;
    static {
        REDIRECT = Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, MessageFormat.format(
                        "stream-redirect-{0}",
                        String.valueOf(counter.incrementAndGet())));
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    /**
     * Extract command line tokens from configuration.
     * This configuration must have keys {@code "command.<position>"} and its position must be positive integer.
     * The extracted tokens are ordered by its position in natural order.
     * @param prefix configuration prefix (may be ends with '.')
     * @param configuration source configuration
     * @param variables variable resolver (or {@code null} to suppress resolving variables)
     * @return extracted tokens
     * @throws IllegalArgumentException if failed to extract, or some parameters were {@code null}
     */
    public static List<String> extractCommandLineTokens(
            String prefix,
            Map<String, String> configuration,
            VariableResolver variables) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        NavigableMap<String, String> map = PropertiesUtil.createPrefixMap(configuration, prefix);
        SortedMap<Integer, String> ordered = new TreeMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Integer position;
            try {
                position = Integer.valueOf(entry.getKey());
                if (position < 0) {
                    position = null;
                }
            } catch (NumberFormatException e) {
                position = null;
            }
            if (position == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid command position in \"{0}\": {1}",
                        prefix + entry.getKey(),
                        entry.getValue()));
            } else {
                ordered.put(position, entry.getValue());
            }
        }
        List<String> results = new ArrayList<>();
        if (variables == null) {
            results.addAll(ordered.values());
        } else {
            for (String token : ordered.values()) {
                String resolved = variables.replace(token, true);
                results.add(resolved);
            }
        }
        LOG.debug("Extracted command prefix: {}", results);
        return results;
    }

    /**
     * Builds command line tokens.
     * The resulting tokens are concatenated as {@code head}, {@code original}, and {@code tail}.
     * Additionally, {@code head} and {@code tail} <code>&#64;[&lt;position&gt;]</code>
     * in {@code head} and {@code tail} are replaced into {@code original.get(<position>)}.
     * @param head head of command line (resolved)
     * @param original original command line tokens
     * @param tail tail of command line (resolved)
     * @return the built command line tokens
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<String> buildCommand(List<String> head, List<String> original, List<String> tail) {
        if (head == null) {
            throw new IllegalArgumentException("head must not be null"); //$NON-NLS-1$
        }
        if (original == null) {
            throw new IllegalArgumentException("original must not be null"); //$NON-NLS-1$
        }
        if (tail == null) {
            throw new IllegalArgumentException("tail must not be null"); //$NON-NLS-1$
        }
        List<String> results = new ArrayList<>();
        results.addAll(resolveCommand(head, original));
        results.addAll(original);
        results.addAll(resolveCommand(tail, original));
        LOG.debug("Built command: {}", results);
        return results;
    }

    private static List<String> resolveCommand(List<String> target, List<String> original) {
        assert target != null;
        assert original != null;
        List<String> results = new ArrayList<>();
        for (String token : target) {
            StringBuilder buf = new StringBuilder();
            int start = 0;
            Matcher matcher = ARGUMENT.matcher(token);
            while (matcher.find(start)) {
                buf.append(token.substring(start, matcher.start()));
                int position = Integer.parseInt(matcher.group(1));
                assert position >= 0;
                if (position >= original.size()) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Command reference is out of bounds: {0}",
                            matcher.group()));
                }
                buf.append(original.get(position));
                start = matcher.end();
            }
            buf.append(token.substring(start));
            results.add(buf.toString());
        }
        return results;
    }

    /**
     * Returns an implementation of {@link ProcessExecutor} runs in the local computer.
     * @return {@link ProcessExecutor} which runs in the local computer
     */
    public static ProcessExecutor getProcessExecutor() {
        return new ProcessExecutor() {
            @Override
            public int execute(
                    ExecutionContext context,
                    List<String> commandLineTokens,
                    Map<String, String> environmentVariables) throws InterruptedException, IOException {
                return execute(context,
                        commandLineTokens, environmentVariables,
                        Collections.<String, Blob>emptyMap(), System.out);
            }
            @Override
            public int execute(
                    ExecutionContext context,
                    List<String> commandLineTokens,
                    Map<String, String> environmentVariables,
                    OutputStream output) throws InterruptedException, IOException {
                return execute(context,
                        commandLineTokens, environmentVariables,
                        Collections.<String, Blob>emptyMap(), output);
            }
            @Override
            public int execute(
                    ExecutionContext context,
                    List<String> commandLineTokens,
                    Map<String, String> environmentVariables,
                    Map<String, Blob> extensions,
                    OutputStream output) throws InterruptedException, IOException {
                return ProcessUtil.execute(
                        context,
                        commandLineTokens, environmentVariables,
                        extensions, output);
            }
        };
    }

    static int execute(
            ExecutionContext context,
            List<String> command,
            Map<String, String> env,
            Map<String, Blob> extensions,
            OutputStream output) throws InterruptedException, IOException {
        Map<String, String> newEnv = new LinkedHashMap<>();
        newEnv.putAll(env);
        try (TemporaryFiles temporaries = new TemporaryFiles()) {
            for (Map.Entry<String, Blob> entry : extensions.entrySet()) {
                String name = entry.getKey();
                String token = resolve(entry.getKey(), entry.getValue(), temporaries);
                newEnv.put(ExecutionScriptHandler.ENV_EXTENSION_PREFIX + name, token);
            }
            return execute(context, command, newEnv, output);
        }
    }

    private static String resolve(String extension, Blob blob, TemporaryFiles temporaries) throws IOException {
        if (blob instanceof FileBlob) {
            return ((FileBlob) blob).getFile().getAbsolutePath();
        }
        String suffix = BlobUtil.getSuffix(extension, blob);
        try (InputStream in = blob.open()) {
            return temporaries.create(BLOB_FILE_PREFIX, suffix, in).getAbsolutePath();
        }
    }

    private static int execute(
            ExecutionContext context,
            List<String> command,
            Map<String, String> env,
            OutputStream output) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        builder.environment().putAll(env);

        String home = System.getProperty("user.home", ".");
        File homeDirectory = new File(home);
        if (homeDirectory.isDirectory()) {
            builder.directory(homeDirectory);
        }

        Process process = builder.start();
        try {
            ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
            redirect(empty, process.getOutputStream());
            Future<?> stdout = redirect(process.getInputStream(), output);
            int exit = process.waitFor();
            try {
                stdout.get();
            } catch (ExecutionException e) {
                // don't care
                LOG.debug("Error occurred while waiting stdout is closed", e);
            }
            return exit;
        } finally {
            process.destroy();
        }
    }

    private static Future<?> redirect(InputStream source, OutputStream sink) {
        assert source != null;
        assert sink != null;
        return REDIRECT.submit(new StreamRedirectTask(source, sink));
    }

    private ProcessUtil() {
        return;
    }
}
