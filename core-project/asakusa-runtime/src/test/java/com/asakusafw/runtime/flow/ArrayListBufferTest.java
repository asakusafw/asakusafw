/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link ArrayListBuffer}.
 */
public class ArrayListBufferTest {

    /**
     * 空のリストを作成する。
     */
    @Test
    public void createEmpty() {
        ArrayListBuffer<Holder> buf = new ArrayListBuffer<Holder>();
        buf.begin();
        buf.end();
        assertThat(buf.size(), is(0));

        buf.shrink();
    }

    /**
     * 要素がひとつのリストを作成する。
     */
    @Test
    public void createSingle() {
        ArrayListBuffer<Holder> buf = new ArrayListBuffer<Holder>();
        buf.begin();
        assertThat(buf.isExpandRequired(), is(true));
        buf.expand(new Holder(""));
        assertThat(buf.isExpandRequired(), is(false));
        buf.advance().value = "Hello";
        buf.end();
        assertThat(buf.size(), is(1));
        assertThat(buf.get(0), is(new Holder("Hello")));

        buf.shrink();
    }

    /**
     * 要素がひとつのリストを作成し、再利用する。
     */
    @Test
    public void reuse() {
        ArrayListBuffer<Holder> buf = new ArrayListBuffer<Holder>();
        buf.begin();
        assertThat(buf.isExpandRequired(), is(true));
        buf.expand(new Holder(""));
        buf.advance().value = "Hello";
        buf.end();
        buf.shrink();

        buf.begin();
        assertThat(buf.getCursorPosition(), is(0));
        buf.advance().value = "World";
        buf.end();

        assertThat(buf.size(), is(1));
        assertThat(buf.get(0), is(new Holder("World")));
        buf.shrink();
    }

    /**
     * 巨大なリストを生成する。。
     */
    @Test
    public void createBigList() {
        int size = 100000;

        ArrayListBuffer<Holder> buf = new ArrayListBuffer<Holder>();
        buf.begin();
        for (int i = 0; i < size; i++) {
            if (buf.isExpandRequired()) {
                buf.expand(new Holder(""));
            }
            buf.advance().value = String.valueOf(i);
        }
        buf.end();

        assertThat(buf.size(), is(size));

        for (int i = 0; i < size; i++) {
            assertThat(buf.get(i).value, is(String.valueOf(i)));
        }

        buf.shrink();
    }

    /**
     * リストの構築時にexpandを行わない。
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void advance_OutOfBounds() {
        ArrayListBuffer<Holder> buf = new ArrayListBuffer<Holder>();
        try {
            buf.begin();
            while (true) {
                buf.advance();
            }
        } finally {
            buf.shrink();
        }
    }

    /**
     * リストの構築時にadvanceを行わない。
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void get_UpperOutOfBounds() {
        ArrayListBuffer<Holder> buf = new ArrayListBuffer<Holder>();
        try {
            buf.begin();
            buf.end();
            buf.get(0);
        } finally {
            buf.shrink();
        }
    }

    /**
     * 負のインデックスを参照する。
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void get_LowerOutOfBounds() {
        ArrayListBuffer<Holder> buf = new ArrayListBuffer<Holder>();
        try {
            buf.begin();
            buf.end();
            buf.get(-1);
        } finally {
            buf.shrink();
        }
    }

    static class Holder {

        String value;

        Holder(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Holder other = (Holder) obj;
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
