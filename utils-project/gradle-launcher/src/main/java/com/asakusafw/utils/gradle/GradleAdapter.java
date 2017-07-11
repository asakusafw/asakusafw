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

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.internal.impldep.com.google.common.base.Objects;
import org.gradle.tooling.ConfigurableLauncher;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities about Gradle project.
 * @since 0.9.2
 */
public class GradleAdapter {

    static final Logger LOG = LoggerFactory.getLogger(GradleAdapter.class);

    private final ProjectContext context;

    private final ProjectConnection connection;

    private final List<String> arguments = new ArrayList<>();

    /**
     * Creates a new instance.
     * @param context the project context
     * @param connection the target connection
     */
    public GradleAdapter(ProjectContext context, ProjectConnection connection) {
        this.context = context;
        this.connection = connection;
    }

    /**
     * Sets build arguments.
     * It is available during this session.
     * @param args the arguments
     * @return this
     */
    public GradleAdapter withArguments(String... args) {
        arguments.clear();
        Collections.addAll(arguments, args);
        return this;
    }

    /**
     * Returns a Gradle model of the project.
     * @param <T> the model type
     * @param modelType the model type
     * @return the model
     */
    public <T> T get(Class<T> modelType) {
        return configure(connection.model(modelType)).get();
    }

    /**
     * Returns a Gradle model of the project.
     * @param <T> the model type
     * @param modelType the model type
     * @param configurator the model consumer
     * @return this
     */
    public <T> GradleAdapter get(Class<T> modelType, Consumer<? super T> configurator) {
        configurator.accept(get(modelType));
        return this;
    }

    /**
     * Runs tasks in the project.
     * @param tasks the tasks
     * @return this
     */
    public GradleAdapter launch(String... tasks) {
        configure(connection.newBuild().forTasks(tasks)).run();
        return this;
    }

    private <T extends ConfigurableLauncher<T>> T configure(T launcher) {
        List<String> properties = getSystemPropertyArgs();
        List<String> args = new ArrayList<>();
        args.addAll(properties);
        args.addAll(arguments);
        if (LOG.isDebugEnabled()) {
            args.forEach(arg -> LOG.debug("Gradle argument: {}", arg));
        }
        return launcher
                .setEnvironmentVariables(context.environment())
                .setJvmArguments(properties)
                .withArguments(args)
                .addProgressListener(
                        e -> LOG.debug("(Gradle:event) {}", e.getDisplayName()),
                        EnumSet.allOf(OperationType.class))
                .setStandardOutput(redirectOutputStream("Gradle:stdout"))
                .setStandardError(redirectOutputStream("Gradle:stderr"));
    }

    private List<String> getSystemPropertyArgs() {
        Map<String, String> props = context.properties();
        return Stream.concat(
                props.entrySet().stream()
                    .filter(e -> Objects.equal(System.getProperty(e.getKey()), e.getValue()) == false)
                    .map(e -> String.format("-D%s=%s",
                            e.getKey(),
                            Optional.ofNullable(e.getValue()).orElse(""))),
                System.getProperties().keySet().stream()
                    .filter(it -> props.containsKey(it) == false)
                    .map(it -> String.format("-D%s", it)))
                .collect(Collectors.toList());
    }

    private static OutputStream redirectOutputStream(String label) {
        return new OutputStreamRedirector(
                line -> LOG.info("({}) {}", label, line),
                Charset.defaultCharset());
    }

    private static class OutputStreamRedirector extends OutputStream {

        private final Consumer<CharSequence> destination;

        private final CharsetDecoder decoder;

        private final ByteBuffer byteBuffer = ByteBuffer.allocate(4096);

        private final CharBuffer charBuffer = CharBuffer.allocate(4096);

        private final StringBuilder lineBuffer = new StringBuilder();

        private boolean sawCr = false;

        OutputStreamRedirector(Consumer<CharSequence> destination, Charset charset) {
            this.destination = destination;
            this.decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }

        @Override
        public void write(int b) {
            assert byteBuffer.hasRemaining();
            byteBuffer.put((byte) b);
            written();
        }

        @Override
        public void write(byte[] b, int off, int len) {
            final int limit = off + len;
            int position = off;
            while (position < limit) {
                assert byteBuffer.hasRemaining();
                int count = Math.min(byteBuffer.remaining(), limit - position);
                byteBuffer.put(b, position, count);
                position += count;
                written();
            }
        }

        @Override
        public void close() {
            byteBuffer.flip();
            charBuffer.clear();
            decoder.decode(byteBuffer, charBuffer, false);
            charBuffer.flip();
            flushBuffer();
            byteBuffer.clear();
        }

        private void written() {
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                charBuffer.clear();
                decoder.decode(byteBuffer, charBuffer, false);
                charBuffer.flip();
                if (charBuffer.hasRemaining() == false) {
                    // circuit breaker: avoid infinite loop
                    break;
                }
                flushBuffer();
            }
            byteBuffer.clear();
        }

        private void flushBuffer() {
            while (charBuffer.hasRemaining()) {
                char c = charBuffer.get();
                if (c == '\r') {
                    if (sawCr) {
                        dumpLine();
                    } else {
                        sawCr = true;
                    }
                } else if (c == '\n') {
                    dumpLine();
                    sawCr = false;
                } else {
                    if (sawCr) {
                        dumpLine();
                    }
                    lineBuffer.append(c);
                    sawCr = false;
                }
            }
        }

        private void dumpLine() {
            destination.accept(lineBuffer);
            lineBuffer.setLength(0);
        }
    }
}
