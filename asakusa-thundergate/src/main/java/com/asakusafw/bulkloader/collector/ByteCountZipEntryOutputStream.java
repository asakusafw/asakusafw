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
package com.asakusafw.bulkloader.collector;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

import com.asakusafw.runtime.io.ZipEntryOutputStream;


/**
 * 出力したバイト数をカウントするZipEntryOutputStream。
 * @author yuta.shirai
 */
public class ByteCountZipEntryOutputStream extends ZipEntryOutputStream {
    private long size = 0;

    /**
     * インスタンスを生成する。
     * @param zipped ラップする{@link ZipOutputStream}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ByteCountZipEntryOutputStream(ZipOutputStream zipped) {
        super(zipped);
    }

    @Override
    public void write(int b) throws IOException {
        size = size + 1;
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (b != null) {
            size = size + b.length;
        }
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        size = size + len;
        super.write(b, off, len);
    }

    /**
     * これまでにカウントしたバイト数を返す。
     * @return これまでにカウントしたバイト数
     */
    public long getSize() {
        return size;
    }
}
