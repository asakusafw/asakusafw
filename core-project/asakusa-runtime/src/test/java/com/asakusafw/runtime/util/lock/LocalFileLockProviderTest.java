/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.util.lock;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link LocalFileLockProvider}.
 */
@SuppressWarnings("resource")
public class LocalFileLockProviderTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final List<Closeable> closeables = new ArrayList<>();

    /**
     * Closes all.
     */
    @After
    public void closeAll() {
        for (Closeable closeable : closeables) {
            closeQuiet(closeable);
        }
    }

    private void closeQuiet(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        LocalFileLockObject<?> lock0 = acquire(0);
        assertThat(lock0, is(notNullValue()));

        LocalFileLockObject<?> lock1 = acquire(0);
        assertThat(lock1, is(nullValue()));

        assertThat(lock0.getLockFile().exists(), is(true));
        lock0.close();
        assertThat(lock0.getLockFile().exists(), is(false));
    }

    /**
     * re-entrant.
     * @throws Exception if failed
     */
    @Test
    public void reenter() throws Exception {
        LocalFileLockObject<?> lock0 = acquire(0);
        assertThat(lock0, is(notNullValue()));
        lock0.close();

        LocalFileLockObject<?> lock1 = acquire(0);
        assertThat(lock1, is(notNullValue()));
    }

    /**
     * concurrent lock.
     * @throws Exception if failed
     */
    @Test
    public void concurrent() throws Exception {
        LocalFileLockObject<?> lock0 = acquire(0);
        assertThat(lock0, is(notNullValue()));

        LocalFileLockObject<?> lock1 = acquire(1);
        assertThat(lock1, is(notNullValue()));
    }

    private LocalFileLockObject<?> acquire(int value) throws IOException {
        LocalFileLockObject<Object> result = new LocalFileLockProvider<>(folder.getRoot()).tryLock(value);
        closeables.add(result);
        return result;
    }
}
