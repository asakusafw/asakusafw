/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.core.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * Test for {@link Shared}.
 */
public class SharedTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        final AtomicInteger counter = new AtomicInteger(0);
        Shared<Integer> shared = new Shared<Integer>() {
            @Override
            protected Integer initialValue() throws IOException {
                return counter.getAndIncrement();
            }
        };
        assertThat(shared.isInitialzed(), is(false));
        assertThat(shared.get(), is(0));
        assertThat(shared.isInitialzed(), is(true));
        assertThat(shared.get(), is(0));
        assertThat(shared.get(), is(0));

        shared.remove();
        assertThat(shared.isInitialzed(), is(false));
        assertThat(shared.get(), is(1));
        assertThat(shared.isInitialzed(), is(true));

        shared.remove();
        assertThat(shared.isInitialzed(), is(false));
        shared.set(100);
        assertThat(shared.isInitialzed(), is(true));
        assertThat(shared.get(), is(100));

        shared.remove();
        assertThat(shared.isInitialzed(), is(false));
        assertThat(shared.get(), is(2));
        assertThat(shared.isInitialzed(), is(true));
    }

    /**
     * raises exception.
     */
    @Test(expected = Shared.InitializationException.class)
    public void raise_exception() {
        Shared<Integer> shared = new Shared<Integer>() {
            @Override
            protected Integer initialValue() throws IOException {
                throw new IOException();
            }
        };
        shared.get();
    }
}
