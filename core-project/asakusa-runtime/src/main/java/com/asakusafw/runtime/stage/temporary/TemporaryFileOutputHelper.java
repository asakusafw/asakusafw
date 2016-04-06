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
package com.asakusafw.runtime.stage.temporary;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.io.util.DataBuffer;

class TemporaryFileOutputHelper implements Closeable {

    static final Log LOG = LogFactory.getLog(TemporaryFileOutputHelper.class);

    static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(String.format("TemporaryFileOutput-%d", THREAD_COUNTER.incrementAndGet())); //$NON-NLS-1$
            return t;
        }
    };

    private final OutputStream output;

    private final ExecutorService executor = Executors.newFixedThreadPool(1, DAEMON_THREAD_FACTORY);

    private final LinkedList<DataBuffer> available = new LinkedList<>();

    private Future<Result> running;

    private final String dataTypeName;

    private int positionInBlock;

    TemporaryFileOutputHelper(OutputStream output, String dataTypeName) {
        this.output = output;
        this.dataTypeName = dataTypeName;
    }

    public synchronized void initialize(int initialBufferSize) {
        available.addFirst(new DataBuffer(initialBufferSize));
        available.addFirst(new DataBuffer(initialBufferSize));
    }

    public synchronized DataBuffer acquireBuffer() throws IOException, InterruptedException {
        // if no available buffers, first we try to flush the active buffer
        if (available.isEmpty()) {
            flushBuffer();
            if (available.isEmpty()) {
                throw new IllegalStateException();
            }
        }
        DataBuffer first = available.removeFirst();
        first.reset(0, 0);
        return first;
    }

    public synchronized void putNextPage(DataBuffer buffer) throws IOException, InterruptedException {
        // wait for the current active task
        flushBuffer();

        // submit task for writing contents in the buffer
        assert running == null;
        this.running = executor.submit(new Task(output, buffer, dataTypeName, positionInBlock));
    }

    private void flushBuffer() throws IOException, InterruptedException {
        if (running == null) {
            return;
        }
        Result result;
        try {
            result = running.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw new IOException("Exception occurred while writing contents", cause);
            } else if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException(cause);
        } finally {
            running = null;
        }
        this.positionInBlock = result.positionInBlock;

        // releases the written buffer
        this.available.addFirst(result.buffer);
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            try {
                flushBuffer();
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        } finally {
            executor.shutdownNow();
            output.close();
        }
    }

    private static final class Result {

        final DataBuffer buffer;

        final int positionInBlock;

        Result(DataBuffer buffer, int positionInBlock) {
            this.buffer = buffer;
            this.positionInBlock = positionInBlock;
        }
    }

    private static final class Task implements Callable<Result> {

        private static final byte[] ZEROS = new byte[64 * 1024];

        private final OutputStream output;

        private final DataBuffer buffer;

        private final String dataTypeName;

        private int positionInBlock;

        Task(OutputStream output, DataBuffer buffer, String dataTypeName, int positionInBlock) {
            this.output = output;
            this.buffer = buffer;
            this.dataTypeName = dataTypeName;
            this.positionInBlock = positionInBlock;
        }

        @Override
        public Result call() throws Exception {
            flush();
            return new Result(buffer, positionInBlock);
        }

        private void flush() throws IOException {
            if (positionInBlock == 0) {
                positionInBlock += TemporaryFile.writeBlockHeader(output);
                positionInBlock += TemporaryFile.writeString(output, dataTypeName);
            }
            int length = buffer.getWritePosition();
            if (length <= 0) {
                return;
            }
            byte[] buf = TemporaryFile.getInstantBuffer(Snappy.maxCompressedLength(length));
            int compressed = Snappy.compress(buffer.getData(), 0, length, buf, 0);
            writeContentPage(buf, compressed);
            buffer.reset(0, 0);
        }

        private void writeContentPage(byte[] contents, int length) throws IOException {
            if (TemporaryFile.canWritePage(positionInBlock, length) == false) {
                if (TemporaryFile.canWritePage(0, length) == false) {
                    throw new IOException(MessageFormat.format(
                            "Page size is too large: {1} (> {0})",
                            TemporaryFile.BLOCK_SIZE,
                            length));
                }
                writeEndOfPage();
                positionInBlock = 0;
                positionInBlock += TemporaryFile.writeBlockHeader(output);
                positionInBlock += TemporaryFile.writeString(output, dataTypeName);
            }
            TemporaryFile.writeContentPageMark(output, length);
            output.write(contents, 0, length);
            positionInBlock += TemporaryFile.PAGE_HEADER_SIZE + length;
        }

        private void writeEndOfPage() throws IOException {
            TemporaryFile.writeEndOfBlockMark(output);
            positionInBlock += TemporaryFile.PAGE_HEADER_SIZE;
            int rest = TemporaryFile.BLOCK_SIZE - positionInBlock;
            assert rest >= 0;
            byte[] zeros = ZEROS;
            while (rest > 0) {
                int size = Math.min(rest, zeros.length);
                output.write(zeros, 0, size);
                rest -= size;
            }
            positionInBlock = TemporaryFile.BLOCK_SIZE;
        }
    }
}
