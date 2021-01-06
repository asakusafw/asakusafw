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

import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link StringOptionPropertyAdapter}.
 */
public class StringOptionPropertyAdapterTest {

    final StringOptionPropertyAdapter.Builder builder = StringOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        StringOptionPropertyAdapter adapter = builder.build();
        StringOption option = new StringOption();

        adapter.read(new MockValue("A"), option);
        assertThat(option, is(new StringOption("A")));
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        StringOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new StringOption("B"), writer);
        assertThat(writer.get(), is("B"));
    }
}
