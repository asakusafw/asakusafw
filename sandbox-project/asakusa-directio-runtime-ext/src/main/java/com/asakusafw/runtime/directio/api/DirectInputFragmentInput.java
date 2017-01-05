/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.api;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Iterator;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.io.ModelInput;

class DirectInputFragmentInput<T> implements ModelInput<T> {

    private final DirectDataSource dataSource;

    private final DataDefinition<T> definition;

    private final Iterator<DirectInputFragment> fragments;

    private final Counter counter;

    private ModelInput<T> current;

    DirectInputFragmentInput(
            DirectDataSource dataSource,
            DataDefinition<T> definition,
            Iterator<DirectInputFragment> fragments,
            Counter counter) {
        this.dataSource = dataSource;
        this.definition = definition;
        this.fragments = fragments;
        this.counter = counter;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        while (true) {
            if (current == null) {
                current = prepare();
                if (current == null) {
                    break;
                }
            }
            if (current.readTo(model)) {
                return true;
            } else {
                current = null;
            }
        }
        return false;
    }

    private ModelInput<T> prepare() throws IOException {
        if (fragments.hasNext()) {
            DirectInputFragment f = fragments.next();
            try {
                return dataSource.openInput(definition, f, counter);
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        } else {
            return null;
        }

    }

    @Override
    public void close() throws IOException {
        if (current != null) {
            current.close();
            current = null;
        }
        // discard rest fragments
        while (fragments.hasNext()) {
            fragments.next();
        }
    }
}
