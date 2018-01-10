/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.io.InputStream;
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
import org.apache.hadoop.io.IOUtils;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.io.util.DataBuffer;

final class TemporaryFileInputHelper implements Closeable {

    static final Log LOG = LogFactory.getLog(TemporaryFileInputHelper.class);

    static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(String.format("TemporaryFileInput-%d", THREAD_COUNTER.incrementAndGet())); //$NON-NLS-1$
            return t;
        }
    };

    private final InputStream input;

    private final ExecutorService executor = Executors.newFixedThreadPool(1, DAEMON_THREAD_FACTORY);

    private final LinkedList<DataBuffer> available = new LinkedList<>();

    private Future<Result> running;

    private int positionInBlock;

    private int currentBlock;

    private int blockRest;

    private boolean sawEof;

    TemporaryFileInputHelper(InputStream input, int blocks) {
        this.input = input;
        this.blockRest = Math.max(blocks - 1, -1);
    }

    public void initialize() {
        releaseBuffer(new DataBuffer());
        releaseBuffer(new DataBuffer());
    }

    public synchronized void releaseBuffer(DataBuffer buffer) {
        available.addFirst(buffer);
        submitIfAvailable();
    }

    public synchronized Result getNextPage() throws IOException, InterruptedException {
        if (sawEof) {
            return new Result(null, positionInBlock, currentBlock, blockRest, null, true);
        }
        // if no any tasks were running, first we submit a new task for reading the next contents
        submitIfAvailable();
        if (running == null) {
            throw new IllegalStateException();
        }
        assert running != null;
        Result result;
        try {
            result = running.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw new IOException("Exception occurred while reading contents", cause);
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
        this.sawEof = result.sawEof;
        this.positionInBlock = result.positionInBlock;
        this.currentBlock = result.currentBlock;
        this.blockRest = result.blockRest;

        // submit a task for reading the successive page (only if available)
        submitIfAvailable();
        return result;
    }

    private void submitIfAvailable() {
        if (sawEof || running != null || available.isEmpty()) {
            return;
        }
        // acquires an available buffer for reading next page, and submit the task
        DataBuffer buffer = available.removeFirst();
        Task task = new Task(input, buffer, positionInBlock, currentBlock, blockRest);
        running = executor.submit(task);
    }

    @Override
    public synchronized void close() throws IOException {
        executor.shutdownNow();
        input.close();
    }

    static final class Result {

        final DataBuffer buffer;

        final int positionInBlock;

        final int currentBlock;

        final int blockRest;

        final String dataTypeName;

        final boolean sawEof;

        Result(
                DataBuffer buffer,
                int positionInBlock, int currentBlock,
                int blockRest, String dataTypeName,
                boolean sawEof) {
            this.buffer = buffer;
            this.positionInBlock = positionInBlock;
            this.currentBlock = currentBlock;
            this.blockRest = blockRest;
            this.dataTypeName = dataTypeName;
            this.sawEof = sawEof;
        }
    }

    static final class Task implements Callable<Result> {

        private final InputStream input;

        private final DataBuffer buffer;

        private int positionInBlock;

        private int currentBlock;

        private int blockRest;

        private String dataTypeName;

        Task(InputStream input, DataBuffer buffer, int positionInBlock, int currentBlock, int blockRest) {
            this.input = input;
            this.buffer = buffer;
            this.positionInBlock = positionInBlock;
            this.currentBlock = currentBlock;
            this.blockRest = blockRest;
            buffer.reset(0, 0);
        }

        @Override
        public Result call() throws IOException {
            boolean read = readPage();
            return new Result(buffer, positionInBlock, currentBlock, blockRest, dataTypeName, read == false);
        }

        private boolean readPage() throws IOException {
            if (positionInBlock == 0) {
                StringBuilder buf = new StringBuilder();
                int headSize = TemporaryFile.readBlockHeader(input);
                if (headSize < 0) {
                    return false;
                }
                positionInBlock += headSize;
                int size = TemporaryFile.readString(input, buf);
                if (size < 0) {
                    return false;
                }
                positionInBlock += size;
                this.dataTypeName = buf.toString();
            }
            int value = TemporaryFile.readPageHeader(input);
            if (value == TemporaryFile.PAGE_HEADER_EOF) {
                return false;
            }
            positionInBlock += TemporaryFile.PAGE_HEADER_SIZE;
            if (value == TemporaryFile.PAGE_HEADER_EOB) {
                if (blockRest == 0) {
                    return false;
                }
                IOUtils.skipFully(input, TemporaryFile.BLOCK_SIZE - positionInBlock);
                positionInBlock = 0;
                currentBlock++;
                if (blockRest > 0) {
                    blockRest--;
                }
                return readPage();
            }
            byte[] b = readFully(value);
            fillBuffer(b, value);
            return true;
        }

        private byte[] readFully(int length) throws IOException {
            byte[] b = TemporaryFile.getInstantBuffer(length);
            IOUtils.readFully(input, b, 0, length);
            positionInBlock += length;
            return b;
        }

        private void fillBuffer(byte[] bytes, int length) throws IOException {
            int rawLength = Snappy.uncompressedLength(bytes, 0, length);
            byte[] data = buffer.getData();
            if (data.length < rawLength) {
                data = new byte[(int) (rawLength * 1.2)];
            }
            Snappy.uncompress(bytes, 0, length, data, 0);
            buffer.reset(data, 0, rawLength);
        }
    }
}
