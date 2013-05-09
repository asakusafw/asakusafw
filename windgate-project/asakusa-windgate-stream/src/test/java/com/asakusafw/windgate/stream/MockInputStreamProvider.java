/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.windgate.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Mock {@link InputStreamProvider}.
 */
public class MockInputStreamProvider extends InputStreamProvider {

    private final Iterator<? extends StreamProvider<? extends InputStream>> iterator;

    private StreamProvider<? extends InputStream> current;

    /**
     * Creates a new instance.
     * @param provider the provider
     */
    public MockInputStreamProvider(StreamProvider<? extends InputStream> provider) {
        List<StreamProvider<? extends InputStream>> list = new ArrayList<StreamProvider<? extends InputStream>>();
        list.add(provider);
        iterator = list.iterator();
    }

    @Override
    public boolean next() throws IOException {
        current = null;
        if (iterator.hasNext()) {
            current = iterator.next();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getCurrentPath() {
        return current.getDescription();
    }

    @Override
    public CountingInputStream openStream() throws IOException {
        return new CountingInputStream(current.open());
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
