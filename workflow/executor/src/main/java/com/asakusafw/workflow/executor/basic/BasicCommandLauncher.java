/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.workflow.executor.basic;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.CommandLauncher;

/**
 * A basic implementation of {@link CommandLauncher}.
 * @since 0.10.0
 */
public class BasicCommandLauncher implements CommandLauncher {

    static final Logger LOG = LoggerFactory.getLogger(BasicCommandLauncher.class);

    private final OutputConsumer output;

    private final Path workingDir;

    private final Map<String, String> environment;

    /**
     * Creates a new instance.
     * @param workingDir the command working directory
     * @param environment environment variables
     */
    public BasicCommandLauncher(Path workingDir, Map<String, String> environment) {
        this(Output.LOGGING, workingDir, environment);
    }

    /**
     * Creates a new instance.
     * @param output the output consumer
     * @param workingDir the command working directory
     * @param environment environment variables
     */
    public BasicCommandLauncher(OutputConsumer output, Path workingDir, Map<String, String> environment) {
        this.output = output;
        this.workingDir = workingDir;
        this.environment = new HashMap<>(environment);
    }

    @Override
    public int launch(Path command, List<String> arguments) throws IOException, InterruptedException {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(command.toAbsolutePath().toString());
        commandLine.addAll(arguments);
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.directory(workingDir.toFile());
        builder.environment().clear();
        builder.environment().putAll(environment);

        if (LOG.isInfoEnabled()) {
            LOG.info("exec: {}", builder.command().stream()
                    .map(it -> '"' + it + '"')
                    .collect(Collectors.joining(", ")));
        }
        Process process = builder.start();
        try {
            return handle(process, Optional.ofNullable(command.getFileName())
                    .map(Path::toString)
                    .orElse("N/A"));
        } finally {
            process.destroy();
        }
    }

    private int handle(Process process, String label) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r, String.format("%s-%d", label, counter.incrementAndGet())); //$NON-NLS-1$
            thread.setDaemon(true);
            return thread;
        });
        try (ReaderRedirector stdOut = redirect(process.getInputStream(), label,
                    OutputChannel.STANDARD_OUTPUT);
                ReaderRedirector stdErr = redirect(process.getErrorStream(), label,
                        OutputChannel.STANDARD_ERROR_OUTPUT)) {
            Future<?> stdOutFuture = executor.submit(stdOut);
            Future<?> stdErrFuture = executor.submit(stdErr);
            stdOutFuture.get();
            stdErrFuture.get();
        } catch (IOException | ExecutionException e) {
            LOG.warn("error occurred while reading output", e);
        } finally {
            executor.shutdownNow();
        }
        return process.waitFor();
    }

    private ReaderRedirector redirect(InputStream stream, String label, OutputChannel channel) {
        return new ReaderRedirector(
                stream,
                line -> output.accept(channel, label, line));
    }

    /**
     * Represents a kind of output.
     * @since 0.10.0
     */
    public enum OutputChannel {

        /**
         * Standard output.
         */
        STANDARD_OUTPUT("stdout"),

        /**
         * Standard error output.
         */
        STANDARD_ERROR_OUTPUT("stderr"),
        ;

        private final String symbol;

        OutputChannel(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Returns the symbol.
         * @return the symbol
         */
        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return getSymbol();
        }
    }

    /**
     * Consumes command output by lines.
     * @since 0.10.0
     */
    @FunctionalInterface
    public interface OutputConsumer {

        /**
         * Handles the output line.
         * @param channel the output channel
         * @param line the line
         * @param label the output label
         */
        void accept(OutputChannel channel, String label, CharSequence line);
    }

    /**
     * Represents a kind of output target.
     * @since 0.10.0
     */
    public enum Output implements OutputConsumer {

        /**
         * Print to standard outputs.
         */
        STANDARD {
            @Override
            public void accept(OutputChannel channel, String label, CharSequence line) {
                switch (channel) {
                case STANDARD_OUTPUT:
                    System.out.println(line);
                    break;
                case STANDARD_ERROR_OUTPUT:
                    System.err.println(line);
                    break;
                default:
                    throw new AssertionError(channel);
                }
            }
        },

        /**
         * Print via logger.
         */
        LOGGING {
            @Override
            public void accept(OutputChannel channel, String label, CharSequence line) {
                LOG.info("({}:{}) {}", label, channel, line);
            }
        },

        /**
         * Print nothing.
         */
        NOTHING {
            @Override
            public void accept(OutputChannel channel, String label, CharSequence line) {
                LOG.debug("({}:{}) {}", label, channel, line);
            }
        },
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
