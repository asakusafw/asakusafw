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
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport.DataModelWriter;

/**
 * An implementation of {@link SourceDriver} using binary {@link InputStream}.
 * @param <T> type of data model objects
 * @since 0.2.4
 */
public class StreamDrainDriver<T> implements DrainDriver<T> {

    static final WindGateLogger WGLOG = new WindGateStreamLogger(StreamDrainDriver.class);

    private final String resourceName;

    private final String processName;

    private final OutputStreamProvider streamProvider;

    private final DataModelStreamSupport<? super T> streamSupport;

    private final long eachStreamSize;

    private String currentPath;

    private CountingOutputStream currentStream;

    private DataModelWriter<? super T> currentWriter;

    private long bytesCount;

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
            OutputStreamProvider streamProvider,
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
        if (streamProvider.getDesiredStreamSize() > 0) {
            eachStreamSize = streamProvider.getDesiredStreamSize();
        } else {
            eachStreamSize = Long.MAX_VALUE;
        }
    }

    @Override
    public void prepare() throws IOException {
        return;
    }

    @Override
    public void put(T object) throws IOException {
        if (currentWriter == null || currentStream.getCount() >= eachStreamSize) {
            if (currentWriter != null) {
                closeCurrentStream();
            }
            prepareNextStream();
        }
        currentWriter.write(object);
    }

    private void prepareNextStream() throws IOException {
        streamProvider.next();
        currentPath = streamProvider.getCurrentPath();
        WGLOG.info("I04001",
                resourceName,
                processName,
                currentPath);
        try {
            currentStream = streamProvider.openStream();
            currentWriter = streamSupport.createWriter(currentPath, currentStream);
        } catch (IOException e) {
            WGLOG.error(e, "E04001",
                    resourceName,
                    processName,
                    currentPath);
            throw e;
        }
    }

    private void closeCurrentStream() throws IOException {
        currentWriter.flush();
        WGLOG.info("I04002",
                resourceName,
                processName,
                currentPath,
                currentStream.getCount());
        bytesCount += currentStream.getCount();
        try {
            currentStream.close();
        } catch (IOException e) {
            WGLOG.error(e, "E04002",
                    resourceName,
                    processName,
                    currentPath);
            throw e;
        }
        currentPath = null;
        currentStream = null;
        currentWriter = null;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        if (currentWriter != null) {
            closeCurrentStream();
        }
        closed = true;
    }
}
