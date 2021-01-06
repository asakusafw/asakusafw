/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.value;

import java.io.IOException;

/**
 * An abstract super interface of values that can restore their contents from a sequence of bytes.
 */
public interface Restorable {

    /**
     * Restores the contents from the slice of the byte array.
     * @param bytes the byte array
     * @param offset the beginning index in the byte array (inclusive)
     * @param limit the ending index in the byte array (exclusive)
     * @return the number of bytes to restore the contents
     * @throws IOException if failed to restore the contents
     */
    int restore(byte[] bytes, int offset, int limit) throws IOException;
}
