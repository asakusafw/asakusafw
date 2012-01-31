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
package com.asakusafw.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * TSVを利用したモデルの入出力に関するオブジェクトを生成するファクトリ。
 * @param <T> 取り扱うモデルオブジェクトの型
 */
public class TsvIoFactory<T> extends ModelIoFactory<T> {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * インスタンスを生成する。
     * @param modelClass 取り扱うモデルオブジェクトの種類
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public TsvIoFactory(Class<T> modelClass) {
        super(modelClass);
    }

    @Override
    protected RecordParser createRecordParser(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        return new TsvParser(new InputStreamReader(in, CHARSET));
    }

    @Override
    protected RecordEmitter createRecordEmitter(OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out must not be null"); //$NON-NLS-1$
        }
        return new TsvEmitter(new OutputStreamWriter(out, CHARSET));
    }
}
