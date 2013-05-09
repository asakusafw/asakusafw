/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.asakusafw.compiler.flow.Location;


/**
 * リソースを保持するリポジトリ。
 */
public interface ResourceRepository {

    /**
     * このリポジトリの内容を反復するカーソルを、新しく作成して返す。
     * @return 新しいカーソル
     * @throws IOException カーソルの作成に失敗した場合
     */
    Cursor createCursor() throws IOException;

    /**
     * リポジトリの内部を反復するカーソル。
     */
    interface Cursor extends Closeable {

        /**
         * 次の要素にカーソルを移動し、存在する場合のみ{@code true}を返す。
         * @return 次の要素が存在する場合のみ{@code true}
         * @throws IOException カーソルの移動に失敗した場合
         */
        boolean next() throws IOException;

        /**
         * 現在の要素に関する位置を返す。
         * @return 現在の要素に関する位置
         */
        Location getLocation();

        /**
         * 現在の要素の内容を開く。
         * @return 現在の要素の内容を返すストリーム
         * @throws IOException 要素を開けなかった場合
         */
        InputStream openResource() throws IOException;
    }
}
