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
package com.asakusafw.utils.gradle;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a command path.
 * @since 0.9.2
 */
public class CommandPath {

    static final Logger LOG = LoggerFactory.getLogger(CommandPath.class);

    static final boolean WINDOWS = System.getProperty("os.name", "")
            .toLowerCase(Locale.ENGLISH)
            .startsWith("windows");

    static final String ENV_PATH = "PATH";

    static final String ENV_PATHEXT = "PATHEXT";

    static final List<String> WINDOWS_PATH_EXTENSIONS;
    static {
        List<String> extensions = new ArrayList<>();
        if (WINDOWS) {
            Optional.ofNullable(System.getenv(ENV_PATHEXT))
                    .map(it -> Arrays.stream(it.split(Pattern.quote(File.pathSeparator))))
                    .orElseGet(Stream::empty)
                    .sequential()
                    .map(String::trim)
                    .filter(it -> it.isEmpty() == false)
                    .map(it -> it.toLowerCase(Locale.ENGLISH))
                    .forEach(extensions::add);
        }
        WINDOWS_PATH_EXTENSIONS = extensions;
    }

    private final List<Path> directories;

    /**
     * Creates a new instance.
     * @param directories the path directories
     */
    public CommandPath(List<? extends Path> directories) {
        this.directories = new ArrayList<>(directories);
    }

    /**
     * Returns an appended path.
     * @param other to be appended in the tail of this path
     * @return the appended path
     */
    public CommandPath append(CommandPath other) {
        List<Path> results = new ArrayList<>();
        results.addAll(directories);
        results.addAll(other.directories);
        return new CommandPath(results);
    }

    /**
     * Creates a new instance.
     * @param env the environment variables
     * @return the created instance
     */
    public static CommandPath system(Map<String, String> env) {
        return CommandPath.of(env.get(ENV_PATH));
    }

    /**
     * Creates a new instance.
     * @param pathString the path string
     * @return the created instance
     */
    public static CommandPath of(String pathString) {
        if (pathString == null) {
            return new CommandPath(Collections.emptyList());
        }
        List<Path> directories = Stream.of(pathString.split(Pattern.quote(File.pathSeparator)))
            .map(String::trim)
            .filter(s -> s.isEmpty() == false)
            .map(Paths::get)
            .collect(Collectors.toList());
        return new CommandPath(directories);
    }

    /**
     * Returns a resolved file path of the specified command.
     * @param command the target command name
     * @return the command file path, or {@code empty} if there is no such a command
     */
    public Optional<Path> find(String command) {
        return Stream.concat(
                    Stream.of(Paths.get(command)).filter(Path::isAbsolute),
                    directories.stream().map(d -> d.resolve(command)))
                .flatMap(CommandPath::expand)
                .filter(Files::isRegularFile)
                .filter(Files::isExecutable)
                .findFirst();
    }

    private static Stream<Path> expand(Path path) {
        if (WINDOWS) {
            String name = Optional.ofNullable(path.getFileName())
                    .map(Path::toString)
                    .orElse(null);
            if (name == null || name.lastIndexOf('.') >= 0) {
                // if already has extension, we don't add any extensions.
                return Stream.of(path);
            }
            // otherwise, we append each %PathExt% value.
            return WINDOWS_PATH_EXTENSIONS.stream()
                    .map(name::concat)
                    .map(path::resolveSibling);
        } else {
            return Stream.of(path);
        }
    }

    /**
     * Returns the path string.
     * @return the path string
     */
    public String asPathString() {
        return directories.stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
    }

    /**
     * Launches the command.
     * @param command the command location
     * @param arguments the command arguments
     * @param workingDir the working directory
     * @param env the environment variables
     * @return the exit status
     */
    public static int launch(Path command, List<String> arguments, Path workingDir, Map<String, String> env) {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(command.toAbsolutePath().toString());
        commandLine.addAll(arguments);
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.directory(workingDir.toFile());
        builder.environment().clear();
        builder.environment().putAll(env);

        LOG.info("Command: {}", builder.command());
        try {
            Process process = builder.start();
            try {
                return handle(process, Optional.ofNullable(command.getFileName())
                        .map(Path::toString)
                        .orElse("N/A"));
            } finally {
                process.destroy();
            }
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "error occurred while executing command: {0}",
                    command), e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "interrupted while executing command: {0}",
                    command), e);
        }
    }

    private static int handle(Process process, String label) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r, String.format("%s-%d", label, counter.incrementAndGet())); //$NON-NLS-1$
            thread.setDaemon(true);
            return thread;
        });
        try (ReaderRedirector stdIn = redirect(process.getInputStream(), String.format("%s:stdout", label));
                ReaderRedirector stdErr = redirect(process.getErrorStream(), String.format("%s:stderr", label))) {
            Future<?> output = executor.submit(stdIn);
            Future<?> error = executor.submit(stdErr);
            output.get();
            error.get();
        } catch (IOException | ExecutionException e) {
            LOG.warn("error occurred while reading output", e);
        } finally {
            executor.shutdownNow();
        }
        return process.waitFor();
    }

    private static ReaderRedirector redirect(InputStream stream, String title) {
        return new ReaderRedirector(stream, line -> LOG.info("({}) {}", title, line));
    }

    @Override
    public String toString() {
        return directories.toString();
    }

    private static final class ReaderRedirector implements Runnable, Closeable {

        private BufferedReader reader;

        private final Consumer<CharSequence> consumer;

        ReaderRedirector(InputStream input, Consumer<CharSequence> consumer) {
            this(new InputStreamReader(input, Charset.defaultCharset()), consumer);
        }

        ReaderRedirector(Reader reader, Consumer<CharSequence> consumer) {
            this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String line;
                    synchronized (this) {
                        if (reader == null) {
                            return;
                        } else {
                            line = reader.readLine();
                        }
                    }
                    if (line == null) {
                        return;
                    }
                    consumer.accept(line);
                }
            } catch (IOException e) {
                LOG.warn("error occurred while reading output", e);
            }
        }

        @Override
        public void close() throws IOException {
            synchronized (this) {
                if (reader != null) {
                    Reader r = reader;
                    reader = null;
                    r.close();
                }
            }
        }
    }
}
