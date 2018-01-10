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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Test for {@link ValueOptionList}.
 */
public class ValueOptionListTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        ValueOptionList<IntOption> list = new Mock(3);
        assertThat(list, equalTo(Arrays.asList(new IntOption(), new IntOption(), new IntOption())));

        IntOption e0 = list.get(0);
        IntOption e1 = list.get(1);
        IntOption e2 = list.get(2);

        list.set(0, new IntOption(0));
        list.set(1, new IntOption(1));
        list.set(2, new IntOption(2));

        assertThat(list, equalTo(Arrays.asList(new IntOption(0), new IntOption(1), new IntOption(2))));
        assertThat(e0, equalTo(new IntOption(0)));
        assertThat(e1, equalTo(new IntOption(1)));
        assertThat(e2, equalTo(new IntOption(2)));
    }

    private static class Mock extends ValueOptionList<IntOption> {

        private final IntOption[] entries;

        Mock(int count) {
            this.entries = IntStream.range(0, count)
                    .mapToObj(i -> new IntOption())
                    .toArray(IntOption[]::new);
        }

        @Override
        public IntOption get(int index) {
            return entries[index];
        }

        @Override
        public int size() {
            return entries.length;
        }
    }
}
