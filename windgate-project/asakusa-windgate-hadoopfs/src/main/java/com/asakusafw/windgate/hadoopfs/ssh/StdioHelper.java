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
package com.asakusafw.windgate.hadoopfs.ssh;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Provides standard I/Os safely.
 * @since 0.4.0
 */
public final class StdioHelper {

    private static final InputStream ORIGINAL_STDIN = System.in;

    private static final PrintStream ORIGINAL_STDOUT = System.out;

    private static final PrintStream ORIGINAL_STDERR = System.err;

    private StdioHelper() {
        return;
    }

    /**
     * Load and initialize this class once.
     */
    public static void load() {
        return;
    }

    /**
     * Resets standard I/Os.
     */
    public static void reset() {
        System.setIn(ORIGINAL_STDIN);
        System.setOut(ORIGINAL_STDOUT);
        System.setErr(ORIGINAL_STDERR);
    }

    /**
     * Returns original standard input.
     * @return the stream
     */
    public static InputStream getOriginalStdin() {
        return ORIGINAL_STDIN;
    }

    /**
     * Returns original standard output.
     * @return the stream
     */
    public static PrintStream getOriginalStdout() {
        return ORIGINAL_STDOUT;
    }

    /**
     * Returns original standard error output.
     * @return the stream
     */
    public static PrintStream getOriginalStderr() {
        return ORIGINAL_STDERR;
    }
}
