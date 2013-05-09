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
package com.asakusafw.runtime.value;

import java.io.IOException;

/**
 * バイト配列の情報から復元可能であることを表すインターフェース。
 */
public interface Restorable {

    /**
     * バイト配列の指定の位置からこのオブジェクトの内容を復元する。
     * @param bytes 復元に利用するバイト配列
     * @param offset バイト配列の利用可能な位置の先頭 (inclusive)
     * @param limit バイト配列の利用可能な位置の終端 (exclusive)
     * @return 実際に利用したバイト数
     * @throws IOException 復元に失敗した場合
     */
    int restore(byte[] bytes, int offset, int limit) throws IOException;
}
