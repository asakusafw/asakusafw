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
package com.asakusafw.bulkloader.common;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * {@link ModelInput}のスレッドと{@link ModelOutput}のスレッドを分けて読み書きを行う。
 * @param <T> 対象データの種類
 */
public final class MultiThreadedCopier<T> {

    /**
     * 毎回のポーリングの最大待ち時間 (ms)。
     */
    static final long POLL_BREAK_INTERVAL = 100L;

    private final BlockingQueue<T> outputChannel;

    private final BlockingQueue<T> buffer;

    private final ModelInput<T> input;

    private final OutputTask<T> task;

    private MultiThreadedCopier(
            ModelInput<T> input,
            ModelOutput<T> output,
            Collection<T> working) {
        assert input != null;
        assert output != null;
        assert working != null;
        this.outputChannel = new ArrayBlockingQueue<T>(working.size() + 1);
        this.buffer = new ArrayBlockingQueue<T>(working.size() + 1, false, working);
        this.input = input;
        this.task = new OutputTask<T>(outputChannel, buffer, output);
        this.task.setDaemon(true);
    }

    /**
     * 指定の入力の内容を全て出力にコピーする。
     * @param <T> コピーするデータの種類
     * @param input 入力
     * @param output 出力
     * @param working コピー時に仲介するデータモデルクラスの一覧
     * @return コピーした件数
     * @throws IOException 入出力に失敗した場合
     * @throws InterruptedException スレッドに割り込まれた場合
     * @throws IllegalArgumentException {@code working}が空であった場合、
     *      または引数に{@code null}が指定された場合
     */
    public static <T> long copy(
            ModelInput<T> input,
            ModelOutput<T> output,
            Collection<T> working) throws IOException, InterruptedException {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (working == null) {
            throw new IllegalArgumentException("working must not be null"); //$NON-NLS-1$
        }
        if (working.isEmpty()) {
            throw new IllegalArgumentException("working must not be empty"); //$NON-NLS-1$
        }
        return new MultiThreadedCopier<T>(input, output, working).process();
    }

    private long process() throws IOException, InterruptedException {
        task.start();
        while (true) {
            T model = takeBuffer();
            if (input.readTo(model) == false) {
                break;
            }
            outputChannel.put(model);
        }
        task.finished.set(true);
        task.join();
        checkException();
        return task.count;
    }

    private T takeBuffer() throws IOException, InterruptedException {
        while (true) {
            T model = buffer.poll(POLL_BREAK_INTERVAL, TimeUnit.MILLISECONDS);
            if (model == null) {
                if (task.finished.get()) {
                    throw new IllegalStateException();
                }
                checkException();
            } else {
                return model;
            }
        }
    }

    private void checkException() throws InterruptedException, IOException {
        Throwable exception = task.occurred.get();
        if (exception != null) {
            if (exception instanceof InterruptedException) {
                throw (InterruptedException) exception;
            }
            if (exception instanceof IOException) {
                throw (IOException) exception;
            }
            if (exception instanceof Error) {
                throw (Error) exception;
            }
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new IOException(exception);
        }
    }

    static class OutputTask<T> extends Thread {

        private final BlockingQueue<T> source;

        private final BlockingQueue<T> buffer;

        private final ModelOutput<T> sink;

        final AtomicBoolean finished = new AtomicBoolean();

        final AtomicReference<Throwable> occurred = new AtomicReference<Throwable>();

        long count;

        OutputTask(BlockingQueue<T> source, BlockingQueue<T> buffer, ModelOutput<T> sink) {
            assert source != null;
            assert buffer != null;
            assert sink != null;
            this.source = source;
            this.buffer = buffer;
            this.sink = sink;
            this.count = 0;
        }

        @Override
        public void run() {
            try {
                drain();
            } catch (Error e) {
                occurred.set(e);
                throw e;
            } catch (InterruptedException e) {
                if (finished.get()) {
                    return;
                }
                occurred.set(e);
            } catch (Throwable e) {
                occurred.set(e);
            }
        }

        private void drain() throws InterruptedException, IOException {
            while (true) {
                T next = source.poll(POLL_BREAK_INTERVAL, TimeUnit.MILLISECONDS);
                if (next == null) {
                    if (finished.get()) {
                        break;
                    }
                } else {
                    sink.write(next);
                    buffer.add(next);
                    count++;
                }
            }
        }
    }
}
