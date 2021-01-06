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
package com.asakusafw.runtime.io.json.directio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.hadoop.ConfigurableBinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.json.JsonFormat;
import com.asakusafw.runtime.io.json.JsonFormat.InputOption;
import com.asakusafw.runtime.io.json.JsonFormat.OutputOption;
import com.asakusafw.runtime.io.json.JsonInput;
import com.asakusafw.runtime.io.json.JsonOutput;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.value.StringOption;

/**
 * An abstract implementation of Direct I/O data format for JSON files.
 * @param <T> the data type
 * @since 0.10.3
 */
public abstract class AbstractJsonStreamFormat<T> extends ConfigurableBinaryStreamFormat<T> {

    private static final Set<InputOption> INPUT_OPTS_HEAD = Collections.unmodifiableSet(EnumSet.of(
            InputOption.ENABLE_SOURCE_POSITION,
            InputOption.ENABLE_RECORD_INDEX));

    private static final Set<InputOption> INPUT_OPTS_REST = Collections.emptySet();

    private static final Set<OutputOption> OUTPUT_OPTS = Collections.emptySet();

    private final AtomicReference<JsonFormat<T>> formatCache = new AtomicReference<>();

    /**
     * Configures {@link JsonFormat} builder.
     * @param builder the target builder
     */
    protected abstract void configureJsonFormat(JsonFormat.Builder<T> builder);

    /**
     * Returns the input splitter of this format.
     * @return the input splitter, or {@code null} is input split is disabled
     */
    protected InputSplitter getInputSplitter() {
        return null;
    }

    /**
     * Returns the compression codec class.
     * @return the compression codec class, or {@code null} if compression is disabled
     */
    protected Class<? extends CompressionCodec> getCompressionCodecClass() {
        return null;
    }

    /**
     * Processes the data object, which has filled the current record data.
     * @param model the data object
     * @param path the current input path
     * @param input the current input
     */
    protected void afterInput(T model, StringOption path, JsonInput<T> input) {
        return;
    }

    /**
     * Process the data object.
     * @param model the data object
     * @param path the current output path
     * @param output the current output
     */
    protected void beforeOutput(T model, StringOption path, JsonOutput<T> output) {
        return;
    }

    /**
     * Returns the {@link JsonFormat}.
     * @return the JSON format
     */
    public final JsonFormat<T> getJsonFormat() {
        return cached(formatCache, () -> {
            JsonFormat.Builder<T> builder = JsonFormat.builder(getSupportedType());
            configureJsonFormat(builder);
            return builder.build();
        });
    }

    private static <U> U cached(AtomicReference<U> cache, Supplier<U> factory) {
        U cached = cache.get();
        if (cached != null) {
            return cached;
        }
        cache.compareAndSet(null, factory.get());
        return cache.get();
    }

    @Override
    public final long getPreferredFragmentSize() {
        InputSplitter splitter = getInputSplitter();
        if (splitter != null) {
            return splitter.getPreferredSize();
        } else {
            return -1L;
        }
    }

    @Override
    public final long getMinimumFragmentSize() {
        InputSplitter splitter = getInputSplitter();
        if (splitter != null) {
            return splitter.getLowerLimitSize();
        } else {
            return -1L;
        }
    }

    @Override
    public ModelInput<T> createInput(
            Class<? extends T> dataType,
            String path,
            InputStream stream,
            long offset, long fragmentSize) throws IOException, InterruptedException {
        JsonFormat<T> format = getJsonFormat();
        Set<InputOption> options = offset == 0 ? INPUT_OPTS_HEAD : INPUT_OPTS_REST;
        InputStream source = decorate(stream, offset, fragmentSize);
        return new DecoratedInput(format.open(path, source, options), new StringOption(path));
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            String path,
            OutputStream stream) throws IOException, InterruptedException {
        JsonFormat<T> format = getJsonFormat();
        Set<OutputOption> options = OUTPUT_OPTS;
        OutputStream destination = decorate(stream);
        return new DecoratedOutput(format.open(path, destination, options), new StringOption(path));
    }

    private InputStream decorate(InputStream stream, long offset, long splitSize) throws IOException {
        InputSplitter splitter = getInputSplitter();
        if (splitter != null) {
            assert getCompressionCodecClass() == null;
            return splitter.trim(stream, offset, splitSize);
        }
        Class<? extends CompressionCodec> codecClass = getCompressionCodecClass();
        if (codecClass != null) {
            CompressionCodec codec = ReflectionUtils.newInstance(codecClass, getConf());
            return codec.createInputStream(stream);
        }
        return stream;
    }

    private OutputStream decorate(OutputStream stream) throws IOException {
        Class<? extends CompressionCodec> codecClass = getCompressionCodecClass();
        if (codecClass != null) {
            CompressionCodec codec = ReflectionUtils.newInstance(codecClass, getConf());
            return codec.createOutputStream(stream);
        }
        return stream;
    }

    private class DecoratedInput implements ModelInput<T> {

        private final JsonInput<T> input;

        private final StringOption path;

        DecoratedInput(JsonInput<T> input, StringOption path) {
            this.input = input;
            this.path = path;
        }

        @Override
        public boolean readTo(T model) throws IOException {
            if (input.readTo(model)) {
                afterInput(model, path, input);
                return true;
            }
            return false;
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }

    private class DecoratedOutput implements ModelOutput<T> {

        private final JsonOutput<T> output;

        private final StringOption path;

        DecoratedOutput(JsonOutput<T> output, StringOption path) {
            this.output = output;
            this.path = path;
        }

        @Override
        public void write(T model) throws IOException {
            beforeOutput(model, path, output);
            output.write(model);
        }

        @Override
        public void close() throws IOException {
            output.close();
        }
    }
}
