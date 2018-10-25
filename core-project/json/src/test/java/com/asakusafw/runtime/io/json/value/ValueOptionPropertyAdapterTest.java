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
package com.asakusafw.runtime.io.json.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.function.Supplier;

import org.junit.Test;

import com.asakusafw.runtime.io.json.value.ValueOptionPropertyAdapter.NullStyle;
import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link ValueOptionPropertyAdapter}.
 */
public class ValueOptionPropertyAdapterTest {

    final IntOptionPropertyAdapter.Builder builder = IntOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        IntOptionPropertyAdapter adapter = builder.build();
        IntOption option = new IntOption();

        adapter.read(new MockValue(100), option);
        assertThat(option, is(new IntOption(100)));
    }

    /**
     * read null.
     * @throws Exception if failed
     */
    @Test
    public void read_null() throws Exception {
        IntOptionPropertyAdapter adapter = builder.build();
        IntOption option = new IntOption(1234);

        adapter.read(new MockValue(null), option);
        assertThat(option, is(new IntOption()));
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        IntOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new IntOption(100), writer);
        assertThat(writer.get(), is(100));
    }

    /**
     * write null as value.
     * @throws Exception if failed
     */
    @Test
    public void write_null_value() throws Exception {
        IntOptionPropertyAdapter adapter = builder
                .withNullStyle(NullStyle.VALUE)
                .build();
        MockValue writer = new MockValue("ABSENT");

        adapter.write(new IntOption(), writer);
        assertThat(writer.get(), is(nullValue()));
    }

    /**
     * write null as absent.
     * @throws Exception if failed
     */
    @Test
    public void write_null_absent() throws Exception {
        IntOptionPropertyAdapter adapter = builder
                .withNullStyle(NullStyle.ABSENT)
                .build();
        MockValue writer = new MockValue("ABSENT");

        adapter.write(new IntOption(), writer);
        assertThat(writer.get(), is("ABSENT"));
    }

    /**
     * lazy build.
     * @throws Exception if failed
     */
    @Test
    public void lazy() throws Exception {
        Supplier<IntOptionPropertyAdapter> sup = builder.lazy();
        IntOptionPropertyAdapter a = sup.get();
        IntOptionPropertyAdapter b = sup.get();
        assertThat(a, is(not(sameInstance(b))));
    }
}
