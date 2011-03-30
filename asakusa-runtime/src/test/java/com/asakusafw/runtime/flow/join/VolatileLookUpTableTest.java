/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link VolatileLookUpTable}.
 */
public class VolatileLookUpTableTest {

    /**
     * 空のテーブル。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void empty() throws Exception {
        VolatileLookUpTable.Builder<IntOption> builder = new VolatileLookUpTable.Builder<IntOption>();

        LookUpTable<IntOption> table = builder.build();
        assertThat(sort(table.get(key(100))), is(values()));
    }

    /**
     * 単純なテスト。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void simple() throws Exception {
        VolatileLookUpTable.Builder<IntOption> builder = new VolatileLookUpTable.Builder<IntOption>();
        builder.add(key(100), new IntOption(100));

        LookUpTable<IntOption> table = builder.build();
        assertThat(sort(table.get(key(100))), is(values(100)));
        assertThat(sort(table.get(key(101))), is(values()));
    }

    /**
     * 同じキーの重複。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void duplicate() throws Exception {
        VolatileLookUpTable.Builder<IntOption> builder = new VolatileLookUpTable.Builder<IntOption>();
        builder.add(key(100), new IntOption(100));
        builder.add(key(100), new IntOption(101));
        builder.add(key(100), new IntOption(102));

        LookUpTable<IntOption> table = builder.build();
        assertThat(sort(table.get(key(100))), is(values(100, 101, 102)));
        assertThat(sort(table.get(key(101))), is(values()));
    }

    /**
     * 同じキーの重複。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void reuseKeys() throws Exception {
        VolatileLookUpTable.Builder<IntOption> builder = new VolatileLookUpTable.Builder<IntOption>();
        LookUpKey key = key();

        key.add(new IntOption(100));
        builder.add(key, new IntOption(100));
        key.reset();

        key.add(new IntOption(101));
        builder.add(key, new IntOption(101));
        key.reset();

        key.add(new IntOption(102));
        builder.add(key, new IntOption(102));
        key.reset();

        LookUpTable<IntOption> table = builder.build();
        assertThat(sort(table.get(key(100))), is(values(100)));
        assertThat(sort(table.get(key(101))), is(values(101)));
        assertThat(sort(table.get(key(102))), is(values(102)));
    }

    private LookUpKey key(int... values) throws IOException {
        LookUpKey result = new LookUpKey();
        for (int value : values) {
            result.add(new IntOption(value));
        }
        return result;
    }

    private List<IntOption> sort(List<IntOption> list) {
        Collections.sort(list);
        return list;
    }

    private List<IntOption> values(int...values) {
        List<IntOption> options = new ArrayList<IntOption>();
        for (int value : values) {
            options.add(new IntOption(value));
        }
        return sort(options);
    }
}
