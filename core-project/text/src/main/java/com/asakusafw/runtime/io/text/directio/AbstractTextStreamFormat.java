/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.text.directio;

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
import com.asakusafw.runtime.io.text.TextFormat;
import com.asakusafw.runtime.io.text.TextInput;
import com.asakusafw.runtime.io.text.TextOutput;
import com.asakusafw.runtime.io.text.driver.InputOption;
import com.asakusafw.runtime.io.text.driver.OutputOption;
import com.asakusafw.runtime.io.text.driver.RecordDefinition;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.value.StringOption;

/**
 * An abstract implementation of Direct I/O data format for formatted text files.
 * @param <T> the data type
 * @since 0.9.1
 */
public abstract class AbstractTextStreamFormat<T> extends ConfigurableBinaryStreamFormat<T> {

    private static final Set<InputOption> INPUT_OPTS_HEAD =
            Collections.unmodifiableSet(EnumSet.of(InputOption.FROM_FILE_HEAD));

    private static final Set<InputOption> INPUT_OPTS_REST = Collections.emptySet();

    private static final Set<OutputOption> OUTPUT_OPTS = Collections.emptySet();

    private final AtomicReference<TextFormat> textFormatCache = new AtomicReference<>();

    private final AtomicReference<RecordDefinition<T>> recordDefinitionCache = new AtomicReference<>();

    /**
     * Returns the {@link TextFormat}.
     * @return the text format
     */
    protected abstract TextFormat createTextFormat();

    /**
     * Returns the {@link RecordDefinition}.
     * @return the record structure definition
     */
    protected abstract RecordDefinition<T> createRecordDefinition();

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
    protected void afterInput(T model, StringOption path, TextInput<T> input) {
        return;
    }

    /**
     * Process the data object.
     * @param model the data object
     * @param path the current output path
     * @param output the current output
     */
    protected void beforeOutput(T model, StringOption path, TextOutput<T> output) {
        return;
    }

    /**
     * Returns the {@link TextFormat}.
     * @return the text format
     */
    public final TextFormat getTextFormat() {
        return cached(this::createTextFormat, textFormatCache);
    }

    /**
     * Returns the {@link RecordDefinition}.
     * @return the record structure definition
     */
    public final RecordDefinition<T> getRecordDefinition() {
        return cached(this::createRecordDefinition, recordDefinitionCache);
    }

    private static <U> U cached(Supplier<U> factory, AtomicReference<U> cache) {
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
        TextFormat format = getTextFormat();
        RecordDefinition<T> record = getRecordDefinition();
        Set<InputOption> options = offset == 0 ? INPUT_OPTS_HEAD : INPUT_OPTS_REST;
        InputStream source = decorate(stream, offset, fragmentSize);
        return new DecoratedInput(record.newInput(format.open(source), path, options), new StringOption(path));
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            String path,
            OutputStream stream) throws IOException, InterruptedException {
        TextFormat format = getTextFormat();
        RecordDefinition<T> record = getRecordDefinition();
        Set<OutputOption> options = OUTPUT_OPTS;
        OutputStream destination = decorate(stream);
        return new DecoratedOutput(record.newOutput(format.open(destination), path, options), new StringOption(path));
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

        private final TextInput<T> input;

        private final StringOption path;

        DecoratedInput(TextInput<T> input, StringOption path) {
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

        private final TextOutput<T> output;

        private final StringOption path;

        DecoratedOutput(TextOutput<T> output, StringOption path) {
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
