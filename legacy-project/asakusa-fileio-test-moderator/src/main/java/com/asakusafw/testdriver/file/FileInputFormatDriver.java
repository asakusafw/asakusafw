/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver.file;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.LinkedList;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;

/**
 * Retrieves model objects using {@link FileInputFormat}.
 * @param <V> the type of target model to retrieve
 */
class FileInputFormatDriver<V> implements DataModelSource {

    static final Logger LOG = LoggerFactory.getLogger(FileInputFormatDriver.class);

    private final DataModelDefinition<V> definition;

    private final TaskAttemptContext context;

    private final FileInputFormat<?, V> format;

    private final LinkedList<InputSplit> splits;

    private RecordReader<?, V> current;

    /**
     * Creates a new instance.
     * @param context target context with source information
     * @param definition the data model definition
     * @param format the input format
     * @throws IOException if failed to initialize
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileInputFormatDriver(
            DataModelDefinition<V> definition,
            TaskAttemptContext context,
            FileInputFormat<?, V> format) throws IOException {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Emulating InputFormat: {}", format.getClass().getName());
        this.definition = definition;
        this.context = context;
        this.format = format;

        LOG.debug("Computing input splits: {}", format.getClass().getName());
        this.splits = new LinkedList<InputSplit>(format.getSplits(context));
    }

    @Override
    public DataModelReflection next() throws IOException {
        if (prepare() == false) {
            return null;
        }
        assert current != null;
        while (true) {
            V model = getNext();
            if (model != null) {
                return definition.toReflection(model);
            }
            disposeCurrent();
            if (prepareNext() == false) {
                break;
            }
        }
        return null;
    }

    private V getNext() throws IOException {
        assert current != null;
        try {
            if (current.nextKeyValue() == false) {
                return null;
            }
            return current.getCurrentValue();
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }

    private void disposeCurrent() throws IOException {
        assert current != null;
        current.close();
        current = null;
    }

    private boolean prepare() throws IOException {
        if (current != null) {
            return true;
        }
        return prepareNext();
    }

    private boolean prepareNext() throws IOException {
        if (splits.isEmpty()) {
            return false;
        }
        InputSplit next = splits.removeFirst();
        try {
            current = format.createRecordReader(next, context);
            current.initialize(next, context);
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        if (current != null) {
            current.close();
        }
        splits.clear();
    }
}
