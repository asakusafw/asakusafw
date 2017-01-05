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
package com.asakusafw.testdriver.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.junit.Test;

/**
 * Test for {@link PluginClassLoader}.
 */
public class PluginClassLoaderTest {

    /**
     * Test method for {@link PluginClassLoader#loadDirect(java.lang.Class)}.
     * @throws Exception if failed
     */
    @Test
    public void loadDirect_simple() throws Exception {
        PluginClassLoader loader = new PluginClassLoader(getClass().getClassLoader());
        try {
            Callable<?> callable = (Callable<?>) loader.loadDirect(MockCallable.class).newInstance();
            assertThat(callable.getClass(), is(not((Object) MockCallable.class)));
            assertThat(callable.call(), is((Object) "Hello, world!"));
        } finally {
            dispose(loader);
        }
    }

    /**
     * Test method for {@link PluginClassLoader#loadDirect(java.lang.Class)}.
     * @throws Exception if failed
     */
    @Test
    public void loadDirect_cached() throws Exception {
        PluginClassLoader loader = new PluginClassLoader(getClass().getClassLoader());
        try {
            Class<?> c1 = loader.loadDirect(MockCallable.class);
            Class<?> c2 = loader.loadDirect(MockCallable.class);
            assertThat(c2, is(sameInstance((Object) c1)));
        } finally {
            dispose(loader);
        }
    }

    private void dispose(Object object) throws IOException {
        if (object instanceof Closeable) {
            ((Closeable) object).close();
        }
    }

    /**
     * {@link Callable} class for testing.
     */
    public static class MockCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            return "Hello, world!";
        }
    }
}
