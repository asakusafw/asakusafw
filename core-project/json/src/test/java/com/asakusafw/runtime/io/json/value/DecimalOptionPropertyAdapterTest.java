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
package com.asakusafw.runtime.io.json.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.asakusafw.runtime.value.DecimalOption;

/**
 * Test for {@link DecimalOptionPropertyAdapter}.
 */
public class DecimalOptionPropertyAdapterTest {

    final DecimalOptionPropertyAdapter.Builder builder = DecimalOptionPropertyAdapter.builder();

    /**
     * read.
     * @throws Exception if failed
     */
    @Test
    public void read() throws Exception {
        DecimalOptionPropertyAdapter adapter = builder.build();
        DecimalOption option = new DecimalOption();

        adapter.read(new MockValue(new BigDecimal("123.45")), option);
        assertThat(option, is(new DecimalOption(new BigDecimal("123.45"))));
    }

    /**
     * write.
     * @throws Exception if failed
     */
    @Test
    public void write() throws Exception {
        DecimalOptionPropertyAdapter adapter = builder.build();
        MockValue writer = new MockValue();

        adapter.write(new DecimalOption(new BigDecimal("6.789")), writer);
        assertThat(writer.get(), is(new BigDecimal("6.789")));
    }
}
