/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * @since 0.2.3
 */
public class TeeDataModelSource implements DataModelSource {

    static final Logger LOG = LoggerFactory.getLogger(TeeDataModelSource.class);

    private final DataModelSource source;

    private final DataModelSink sink;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param source original source
     * @param sink tee to sink
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TeeDataModelSource(DataModelSource source, DataModelSink sink) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (sink == null) {
            throw new IllegalArgumentException("sink must not be null"); //$NON-NLS-1$
        }
        this.source = source;
        this.sink = sink;
    }

    @Override
    public DataModelReflection next() throws IOException {
        DataModelReflection next = source.next();
        if (next != null) {
            sink.put(next);
        }
        return next;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        IOException exception = null;
        try {
            source.close();
        } catch (IOException e) {
            exception = e;
        } finally {
            try {
                sink.close();
            } catch (IOException e) {
                if (exception != null) {
                    LOG.warn(MessageFormat.format(
                            "Failed to close sink: {0}",
                            sink), e);
                } else {
                    exception = e;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
