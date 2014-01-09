/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.windgate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;

/**
 * Mock {@link DataModelStreamSupport}.
 * @param <T> target type
 */
public abstract class MockStreamSupport<T> implements DataModelStreamSupport<T> {

    @Override
    public DataModelReader<T> createReader(String path, InputStream stream) throws IOException {
        throw new AssertionError();
    }

    @Override
    public DataModelWriter<T> createWriter(String path, OutputStream stream) throws IOException {
        throw new AssertionError();
    }
}
