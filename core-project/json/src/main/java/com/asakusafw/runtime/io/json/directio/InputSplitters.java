/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.json.directio;

import com.asakusafw.runtime.io.util.LineFeedDelimitedInputStream;

/**
 * Utilities about {@link InputSplitter}.
 * @since 0.10.3
 */
public final class InputSplitters {

    private InputSplitters() {
        return;
    }

    /**
     * Returns an {@link InputSplitter} for LF separated text without line separator escape.
     * @return the {@link InputSplitter} for LF separated text
     * @see LineFeedDelimitedInputStream
     */
    public static InputSplitter byLineFeed() {
        return (input, offset, splitSize) -> {
            if (isWhole(offset, splitSize)) {
                return input;
            }
            return new LineFeedDelimitedInputStream(input, offset, splitSize);
        };
    }

    static boolean isWhole(long offset, long splitSize) {
        return offset == 0 && splitSize == Long.MAX_VALUE;
    }
}
