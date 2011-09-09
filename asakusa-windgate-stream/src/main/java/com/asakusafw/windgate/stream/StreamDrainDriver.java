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
import java.io.OutputStream;
import java.text.MessageFormat;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelWriter;

/**
 * An implementation of {@link SourceDriver} using binary {@link InputStream}.
 * @param <T> type of data model objects
 * @since 0.2.2
 */
public class StreamDrainDriver<T> implements DrainDriver<T> {

    static final WindGateLogger WGLOG = new WindGateStreamLogger(StreamDrainDriver.class);

    private final String resourceName;

    private final String processName;

    private final StreamProvider<? extends OutputStream> streamProvider;

    private final DataModelStreamSupport<? super T> streamSupport;

    private MeasuringOutputStream stream;

    private DataModelWriter<? super T> writer;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param resourceName original resource name
     * @param processName current process name
     * @param streamProvider provides target {@link OutputStream}
     * @param streamSupport converts data model objects into binary data
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public StreamDrainDriver(
            String resourceName,
            String processName,
            StreamProvider<? extends OutputStream> streamProvider,
            DataModelStreamSupport<? super T> streamSupport) {
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
        this.resourceName = resourceName;
        this.processName = processName;
        this.streamProvider = streamProvider;
        this.streamSupport = streamSupport;
    }

    @Override
    public void prepare() throws IOException {
        if (stream != null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Stream is already prepared (resource={0}, process={1})",
                    resourceName,
                    processName));
        }

        OutputStream bare = null;
        boolean succeeded = false;
        try {
            WGLOG.info("I04001",
                    resourceName,
                    processName,
                    streamProvider.getDescription());
            bare = streamProvider.open();
            stream = new MeasuringOutputStream(bare);
            writer = streamSupport.createWriter(stream);
            succeeded = true;
        } catch (IOException e) {
            WGLOG.error(e, "E04001",
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
    public void put(T object) throws IOException {
        writer.write(object);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        IOException exception = null;
        if (writer != null) {
            try {
                writer.flush();
            } catch (IOException e) {
                exception = e;
            }
        }
        if (stream != null) {
            WGLOG.info("I04002",
                    resourceName,
                    processName,
                    streamProvider.getDescription(),
                    stream.getCount());
            close(stream);
        }
        closed = true;
        if (exception != null) {
            throw exception;
        }
    }

    private void close(OutputStream target) {
        if (target == null) {
            return;
        }
        try {
            target.close();
        } catch (IOException e) {
            WGLOG.warn(e, "W04001",
                    resourceName,
                    processName,
                    streamProvider.getDescription());
        }
    }
}
