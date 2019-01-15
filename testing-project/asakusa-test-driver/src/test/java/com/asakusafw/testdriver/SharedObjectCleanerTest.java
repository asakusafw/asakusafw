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
package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.asakusafw.runtime.core.util.Shared;

/**
 * Test for {@link SharedObjectCleaner}.
 */
public class SharedObjectCleanerTest {

    /**
     * Test for {@link SharedObjectCleaner#add(Shared...)}.
     */
    @Test
    public void objects() {
        AtomicInteger counter = new AtomicInteger();
        Shared<Integer> shared = new Shared<Integer>() {
            @Override
            protected Integer initialValue() throws IOException {
                return counter.getAndIncrement();
            }
        };
        SharedObjectCleaner cleaner = new SharedObjectCleaner().add(shared);
        cleaner.before();
        assertThat(shared.get(), is(0));
        assertThat(shared.get(), is(0));

        cleaner.after();
        assertThat(shared.get(), is(1));
    }

    /**
     * Test for {@link SharedObjectCleaner#add(Class...)}.
     */
    @Test
    public void classes() {
        Shared<Integer> shared = Valid.SHARED;
        SharedObjectCleaner cleaner = new SharedObjectCleaner().add(Valid.class);
        cleaner.before();
        Valid.base = 0;
        assertThat(shared.get(), is(0));
        assertThat(shared.get(), is(0));

        cleaner.after();
        assertThat(shared.get(), is(1));
    }

    /**
     * Test for {@link SharedObjectCleaner#add(Class...)}.
     */
    @Test
    public void not_static() {
        // no exceptions
        SharedObjectCleaner cleaner = new SharedObjectCleaner().add(NotStatic.class);
        cleaner.before();
        cleaner.after();
    }

    /**
     * Test for {@link SharedObjectCleaner#add(Class...)}.
     */
    @Test
    public void not_initialized() {
        // no exceptions
        SharedObjectCleaner cleaner = new SharedObjectCleaner().add(NotInitialized.class);
        cleaner.before();
        cleaner.after();
    }

    private static final class Valid {

        static int base;

        static final Shared<Integer> SHARED = new Shared<Integer>() {
            @Override
            protected Integer initialValue() {
                return base++;
            }
        };
    }

    private static final class NotStatic {

        @SuppressWarnings("unused")
        final Shared<Integer> shared = new Shared<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
    }

    private static final class NotInitialized {

        @SuppressWarnings("unused")
        static final Shared<Integer> SHARED = null;
    }
}
