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
package com.asakusafw.runtime.io.csv.directio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.hadoop.ConfigurableBinaryStreamFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.csv.CsvConfiguration;
import com.asakusafw.runtime.io.csv.CsvEmitter;
import com.asakusafw.runtime.io.csv.CsvParser;
import com.asakusafw.runtime.io.util.InputSplitter;
import com.asakusafw.runtime.value.StringOption;

/**
 * An abstract implementation of Direct I/O data format for CSV files.
 * @param <T> the data type
 * @since 0.10.3
 */
public abstract class AbstractCsvStreamFormat<T> extends ConfigurableBinaryStreamFormat<T> {

    /**
     * Returns the CSV format configuration.
     * @param head {@code true} to returns a configuration for the head of file, otherwise {@code false}
     * @return the CSV format configuration
     */
    protected abstract CsvConfiguration getConfiguration(boolean head);

    /**
     * Parses next record body and fill the contents into the target model.
     * @param input the current input
     * @param model the target data object
     * @param path the current input path
     * @throws IOException if I/O error was occurred
     */
    protected abstract void parse(CsvParser input, T model, StringOption path) throws IOException;

    /**
     * Emits the next record body from the data object into the target output.
     * @param output the current output
     * @param model the data object
     * @param path the current output path
     * @throws IOException if I/O error was occurred
     */
    protected abstract void emit(CsvEmitter output, T model, StringOption path) throws IOException;

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
        CsvConfiguration conf = getConfiguration(offset == 0);
        InputStream source = decorate(stream, offset, fragmentSize);
        return new DecoratedInput(new CsvParser(source, path, conf), new StringOption(path));
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            String path,
            OutputStream stream) throws IOException, InterruptedException {
        CsvConfiguration conf = getConfiguration(true);
        OutputStream destination = decorate(stream);
        return new DecoratedOutput(new CsvEmitter(destination, path, conf), new StringOption(path));
    }

    private InputStream decorate(InputStream stream, long offset, long splitSize) throws IOException {
        InputSplitter splitter = getInputSplitter();
        if (splitter != null) {
            assert getCompressionCodecClass() == null;
            return splitter.trim(stream, offset, splitSize);
        }
        if (offset != 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "splitting is not supported: {0}",
                    getClass().getName()));
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

    private final class DecoratedInput implements ModelInput<T> {

        private final CsvParser input;

        private final StringOption path;

        DecoratedInput(CsvParser input, StringOption path) {
            this.input = input;
            this.path = path;
        }

        @Override
        public boolean readTo(T model) throws IOException {
            if (!input.next()) {
                return false;
            }
            parse(input, model, path);
            input.endRecord();
            return true;
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }

    private class DecoratedOutput implements ModelOutput<T> {

        private final CsvEmitter output;

        private final StringOption path;

        DecoratedOutput(CsvEmitter output, StringOption path) {
            this.output = output;
            this.path = path;
        }

        @Override
        public void write(T model) throws IOException {
            emit(output, model, path);
            output.endRecord();
        }

        @Override
        public void close() throws IOException {
            output.close();
        }
    }
}
