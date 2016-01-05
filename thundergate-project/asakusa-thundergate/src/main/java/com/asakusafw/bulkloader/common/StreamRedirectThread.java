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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.asakusafw.bulkloader.log.Log;

/**
 * ストリームの内容を別のストリームに書き出すスレッド。
 */
public class StreamRedirectThread extends Thread {

    static final Log LOG = new Log(StreamRedirectThread.class);

    private final InputStream input;

    private final OutputStream output;

    private final boolean closeInput;

    private final boolean closeOutput;

    /**
     * インスタンスを生成する。
     * @param input リダイレクトする内容
     * @param output リダイレクト先のストリーム
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StreamRedirectThread(InputStream input, OutputStream output) {
        this(input, output, false, false);
    }

    /**
     * インスタンスを生成する。
     * @param input リダイレクトする内容
     * @param output リダイレクト先のストリーム
     * @param closeInput リダイレクト元のストリームを終了時に閉じる
     * @param closeOutput リダイレクト先のストリームを終了時に閉じる
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @since 0.2.0
     */
    public StreamRedirectThread(
            InputStream input, OutputStream output,
            boolean closeInput, boolean closeOutput) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        this.input = input;
        this.output = output;
        this.closeInput = closeInput;
        this.closeOutput = closeOutput;
    }

    @Override
    public void run() {
        boolean outputFailed = false;
        try {
            InputStream in = input;
            OutputStream out = output;
            byte[] buf = new byte[256];
            while (true) {
                int read = in.read(buf);
                if (read == -1) {
                    break;
                }
                if (outputFailed == false) {
                    try {
                        out.write(buf, 0, read);
                    } catch (IOException e) {
                        outputFailed = true;
                        LOG.warn(e, "TG-COMMON-00028");
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn(e, "TG-COMMON-00028");
        } finally {
            if (closeInput) {
                close(input);
            }
            if (closeOutput) {
                close(output);
            }
        }
    }

    private static void close(Closeable c) {
        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
