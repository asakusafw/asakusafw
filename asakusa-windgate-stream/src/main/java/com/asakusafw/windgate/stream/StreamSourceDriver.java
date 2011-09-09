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
package com.asakusafw.windgate.stream;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelReader;

/**
 * An implementation of {@link SourceDriver} using binary {@link InputStream}.
 * @param <T> type of data model objects
 * @since 0.2.2
 */
public class StreamSourceDriver<T> implements SourceDriver<T> {

    static final WindGateLogger WGLOG = new WindGateStreamLogger(StreamSourceDriver.class);

    private final String resourceName;

    private final String processName;

    private final StreamProvider<? extends InputStream> streamProvider;

    private final DataModelStreamSupport<? super T> streamSupport;

    private final T buffer;

    private MeasuringInputStream stream;

    private DataModelReader<? super T> reader;

    private boolean sawNext;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param resourceName original resource name
     * @param processName current process name
     * @param streamProvider provides source {@link InputStream}
     * @param streamSupport converts {@link InputStream} as data model objects
     * @param buffer data model object for buffer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public StreamSourceDriver(
            String resourceName,
            String processName,
            StreamProvider<? extends InputStream> streamProvider,
            DataModelStreamSupport<? super T> streamSupport,
            T buffer) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (processName == null) {
            throw new IllegalArgumentException("processName must not be null"); //$NON-NLS-1$
        }
        if (streamProvider == null) {
            throw new IllegalArgumentException("streamProvider must not be null"); //$NON-NLS-1$
        }
        if (streamSupport == null) {
            throw new IllegalArgumentException("streamSupport must not be null"); //$NON-NLS-1$
        }
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must not be null"); //$NON-NLS-1$
        }
        this.resourceName = resourceName;
        this.processName = processName;
        this.streamProvider = streamProvider;
        this.streamSupport = streamSupport;
        this.buffer = buffer;
    }

    @Override
    public void prepare() throws IOException {
        if (stream != null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Stream is already prepared (resource={0}, process={1})",
                    resourceName,
                    processName));
        }

        InputStream bare = null;
        boolean succeeded = false;
        try {
            WGLOG.info("I03001",
                    resourceName,
                    processName,
                    streamProvider.getDescription());
            bare = streamProvider.open();
            stream = new MeasuringInputStream(bare);
            reader = streamSupport.createReader(stream);
            sawNext = false;
            succeeded = true;
        } catch (IOException e) {
            WGLOG.error(e, "E03001",
                    resourceName,
                    processName,
                    streamProvider.getDescription());
            throw e;
        } finally {
            if (succeeded == false) {
                close(bare);
            }
        }
    }

    @Override
    public boolean next() throws IOException {
        if (reader.readTo(buffer)) {
            sawNext = true;
        } else {
            sawNext = false;
        }
        return sawNext;
    }

    @Override
    public T get() throws IOException {
        if (sawNext == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "next object is not yet prepared (resource={0}, process={1})",
                    resourceName,
                    processName));
        }
        return buffer;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        sawNext = false;
        if (stream != null) {
            WGLOG.info("I03002",
                    resourceName,
                    processName,
                    streamProvider.getDescription(),
                    stream.getCount());
            close(stream);
        }
        closed = true;
    }

    private void close(InputStream target) {
        if (target == null) {
            return;
        }
        try {
            target.close();
        } catch (IOException e) {
            WGLOG.warn(e, "W03001",
                    resourceName,
                    processName,
                    streamProvider.getDescription());
        }
    }
}
