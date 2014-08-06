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
package com.asakusafw.runtime.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.hadoop.io.Writable;

/**
 * {@link ValueOption}のテストに関する基底。
 */
public class ValueOptionTestRoot {

    /**
     * 比較結果を返す。
     * @param <T> データの種類
     * @param a 比較される値
     * @param b 比較する値
     * @return 比較結果
     */
    protected <T extends ValueOption<T>> int compare(T a, T b) {
        int object = a.compareTo(b);
        Class<?> klass = a.getClass();
        try {
            byte[] b1 = toBytes(a);
            byte[] b2 = toBytes(b);

            Method method = klass.getMethod("compareBytes",
                    byte[].class, int.class, int.class,
                    byte[].class, int.class, int.class);
            int bytes = (Integer) method.invoke(null,
                    b1, 0, b1.length,
                    b2, 0, b2.length);
            assertThat(sign(bytes), is(sign(object)));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return object;
    }

    private int sign(int value) {
        if (value == 0) {
            return 0;
        }
        if (value < 0) {
            return -1;
        }
        return +1;
    }

    /**
     * Writableとして書き出した後に復元する。
     * @param <T> データの種類
     * @param value 対象のデータ
     * @return 復元したデータ
     */
    protected <T extends ValueOption<T>> T restore(T value) {
        checkLength(value);
        restoreRestorable(value);
        return restoreWritable(value);
    }

    private <T extends ValueOption<T>> void restoreRestorable(T value) {
        try {
            byte[] bytes = toBytes(value);
            Restorable copy = value.getClass().newInstance();
            int offset = copy.restore(bytes, 0, bytes.length);
            assertThat(offset, is(bytes.length));
            assertThat(copy, is((Restorable) value));
            assertThat(copy.hashCode(), is(value.hashCode()));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Writable> T restoreWritable(T value) {
        try {
            ByteArrayInputStream read = new ByteArrayInputStream(toBytes(value));
            DataInputStream in = new DataInputStream(read);
            Writable copy = value.getClass().newInstance();
            copy.readFields(in);
            assertThat(in.read(), is(-1));
            assertThat(copy, is((Writable) value));
            assertThat(copy.hashCode(), is(value.hashCode()));
            return (T) copy;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void checkLength(Writable value) {
        Class<?> klass = value.getClass();
        try {
            byte[] bytes = toBytes(value);
            Method method = klass.getMethod("getBytesLength",
                    byte[].class, int.class, int.class);
            int length = (Integer) method.invoke(null, bytes, 0, bytes.length);
            assertThat(length, is(bytes.length));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    byte[] toBytes(Writable value) {
        try {
            ByteArrayOutputStream write = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(write);
            value.write(out);
            out.close();
            byte[] bytes = write.toByteArray();
            return bytes;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
