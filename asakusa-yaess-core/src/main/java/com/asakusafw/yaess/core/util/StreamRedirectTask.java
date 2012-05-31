/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.asakusafw.yaess.core.YaessCoreLogger;
import com.asakusafw.yaess.core.YaessLogger;

/**
 * Redirects an input stream into other output stream.
 * @since 0.2.3
 */
public class StreamRedirectTask implements Runnable {

    static final YaessLogger YSLOG = new YaessCoreLogger(StreamRedirectTask.class);

    private final InputStream input;

    private final OutputStream output;

    private final boolean closeInput;

    private final boolean closeOutput;

    /**
     * Creates a new instance.
     * @param input a source input stream to be redirected
     * @param output a redirect target output stream
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public StreamRedirectTask(InputStream input, OutputStream output) {
        this(input, output, false, false);
    }

    /**
     * Creates a new instance.
     * @param input a source input stream to be redirected
     * @param output a redirect target output stream
     * @param closeInput close input on exit
     * @param closeOutput close output on exit
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public StreamRedirectTask(
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
                        YSLOG.warn(e, "W99001");
                    }
                }
            }
        } catch (IOException e) {
            YSLOG.warn(e, "W99002");
        } finally {
            if (closeInput) {
                close(input);
            }
            if (closeOutput) {
                close(output);
            }
        }
    }

    private static void close(InputStream c) {
        try {
            c.close();
        } catch (IOException e) {
            YSLOG.warn(e, "W99004");
        }
    }

    private static void close(OutputStream c) {
        try {
            c.close();
        } catch (IOException e) {
            YSLOG.warn(e, "W99003");
        }
    }
}
