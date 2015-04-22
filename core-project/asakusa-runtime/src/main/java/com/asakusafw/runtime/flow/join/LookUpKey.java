/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.io.util.DataBuffer;

/**
 * {@link LookUpTable}に利用可能なキー。
 */
public class LookUpKey {

    private static final int INITIAL_SIZE = 256;

    private final DataBuffer buffer;

    /**
     * インスタンスを生成する。
     */
    public LookUpKey() {
        this(INITIAL_SIZE);
    }

    /**
     * 初期バッファサイズを指定してインスタンスを生成する。
     * @param bufferSize 初期バッファサイズ
     */
    public LookUpKey(int bufferSize) {
        this.buffer = new DataBuffer(bufferSize);
    }

    /**
     * バッファの内容をリセットする。
     * @throws IOException リセットに失敗した場合
     */
    public void reset() throws IOException {
        buffer.reset(0, 0);
    }

    /**
     * 指定の要素をキーに追加する。
     * @param writable 追加する要素
     * @throws IOException 追加に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void add(Writable writable) throws IOException {
        if (writable == null) {
            throw new IllegalArgumentException("writable must not be null"); //$NON-NLS-1$
        }
        writable.write(buffer);
    }

    /**
     * ここまでに追加した要素の情報を持つ、このオブジェクトのコピーを返す。
     * @return このオブジェクトのコピー
     * @throws IOException コピーに失敗した場合
     */
    public LookUpKey copy() throws IOException {
        LookUpKey result = new LookUpKey(0);
        byte[] contents = Arrays.copyOfRange(buffer.getData(), buffer.getReadPosition(), buffer.getReadLimit());
        result.buffer.reset(contents, 0, contents.length);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        byte[] b = buffer.getData();
        for (int i = buffer.getReadPosition(), n = buffer.getReadLimit(); i < n; i++) {
            result = result * prime + b[i];
        }
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
        LookUpKey other = (LookUpKey) obj;
        if (buffer.getReadRemaining() != other.buffer.getReadRemaining()) {
            return false;
        }
        byte[] b1 = buffer.getData();
        byte[] b2 = other.buffer.getData();
        int o1 = buffer.getReadPosition();
        int o2 = other.buffer.getReadPosition();
        for (int i = 0, n = buffer.getReadRemaining(); i < n; i++) {
            if (b1[o1 + i] != b2[o2 + i]) {
                return false;
            }
        }
        return true;
    }
}
