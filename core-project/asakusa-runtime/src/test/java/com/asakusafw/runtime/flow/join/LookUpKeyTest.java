/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow.join;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link LookUpKey}.
 */
public class LookUpKeyTest {

    /**
     * {@link LookUpKey#add(org.apache.hadoop.io.Writable)}のテスト。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void add() throws Exception {
        LookUpKey k1 = new LookUpKey();
        LookUpKey k2 = new LookUpKey(1000);
        LookUpKey k3 = new LookUpKey();

        k1.add(new IntOption(100));
        k2.add(new IntOption(100));
        k3.add(new IntOption(100));

        k1.add(new IntOption(101));
        k2.add(new IntOption(101));
        k3.add(new IntOption(101));

        k1.add(new IntOption(102));
        k2.add(new IntOption(102));

        assertThat(k1.equals(k2), is(true));
        assertThat(k1.equals(k3), is(false));
        assertThat(k1.hashCode(), is(k2.hashCode()));
    }

    /**
     * {@link LookUpKey#copy()}のテスト。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void copy() throws Exception {
        LookUpKey k1 = new LookUpKey();
        k1.add(new IntOption(100));

        LookUpKey k2 = k1.copy();
        k2.add(new IntOption(200));

        LookUpKey k3 = new LookUpKey();
        k3.add(new IntOption(100));
        k3.add(new IntOption(200));

        assertThat(k1.equals(k2), is(false));
        assertThat(k2.equals(k3), is(true));
        assertThat(k2.hashCode(), is(k3.hashCode()));
    }

    /**
     * {@link LookUpKey#reset()}のテスト。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void reset() throws Exception {
        LookUpKey k1 = new LookUpKey();
        k1.add(new IntOption(100));
        k1.reset();
        k1.add(new IntOption(200));

        LookUpKey k2 = new LookUpKey();
        k2.add(new IntOption(200));

        assertThat(k1.equals(k2), is(true));
        assertThat(k1.hashCode(), is(k2.hashCode()));
    }
}
