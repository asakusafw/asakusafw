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
package com.asakusafw.runtime.io.util;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelInput;

/**
 * Void implementation of {@link ModelInput}.
 * @param <T> data type
 * @since 0.4.0
 */
public class VoidModelInput<T> implements ModelInput<T> {

    @Override
    public boolean readTo(T model) throws IOException {
        return false;
    }

    @Override
    public void close() {
        return;
    }
}
