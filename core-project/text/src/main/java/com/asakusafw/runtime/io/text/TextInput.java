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
package com.asakusafw.runtime.io.text;

import com.asakusafw.runtime.io.ModelInput;

/**
 * Input from text files.
 * @param <T> the data type
 * @since 0.9.1
 */
public interface TextInput<T> extends ModelInput<T> {

    /**
     * Returns the number of line where the last input starts.
     * @return the line number (0-origin), or {@code -1} if it is not sure
     */
    default long getLineNumber() {
        return -1L;
    }

    /**
     * Returns the record index of the last input.
     * @return the record index (0-origin), or {@code -1} if it is not sure
     */
    default long getRecordIndex() {
        return -1L;
    }
}
