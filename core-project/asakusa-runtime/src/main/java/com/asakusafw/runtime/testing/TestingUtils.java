/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.testing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Utilities for testing.
 * @since 0.4.0
 */
public final class TestingUtils {

    private TestingUtils() {
        return;
    }

    /**
     * Appends a message into the target path.
     * @param path target path
     * @param message message
     * @throws IOError if failed to output
     */
    public static void append(String path, String message) {
        File file = new File(path);
        try {
            OutputStream output = new FileOutputStream(file, true);
            try {
                PrintWriter w = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
                w.println(message);
                w.close();
            } finally {
                output.close();
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }
}
