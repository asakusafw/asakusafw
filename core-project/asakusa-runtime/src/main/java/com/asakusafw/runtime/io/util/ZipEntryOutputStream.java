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
package com.asakusafw.runtime.io.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * {@link ZipOutputStream}の一エントリ分だけを書き出すストリーム。
 */
public class ZipEntryOutputStream extends OutputStream {

    private ZipOutputStream zipped;

    /**
     * インスタンスを生成する。
     * @param zipped 対象の{@link ZipOutputStream}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ZipEntryOutputStream(ZipOutputStream zipped) {
        if (zipped == null) {
            throw new IllegalArgumentException("zipped must not be null"); //$NON-NLS-1$
        }
        this.zipped = zipped;
    }

    @Override
    public void close() throws IOException {
        zipped.closeEntry();
    }

    @Override
    public void write(byte[] b) throws IOException {
        zipped.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        zipped.write(b);
    }

    @Override
    public void flush() throws IOException {
        zipped.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        zipped.write(b, off, len);
    }
}
