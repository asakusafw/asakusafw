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
package com.asakusafw.runtime.io.json.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.runtime.value.BooleanOption;

/**
 * Test for {@link BooleanOptionPropertyAdapter}.
 */
public class BooleanOptionPropertyAdapterTest {

    final BooleanOptionPropertyAdapter.Builder builder = BooleanOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        BooleanOptionPropertyAdapter adapter = builder.build();
        BooleanOption option = new BooleanOption();

        adapter.read(new MockValue(true), option);
        assertThat(option, is(new BooleanOption(true)));
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        BooleanOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new BooleanOption(false), writer);
        assertThat(writer.get(), is(false));
    }
}
