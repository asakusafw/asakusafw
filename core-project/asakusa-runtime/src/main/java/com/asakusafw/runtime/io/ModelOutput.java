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

import java.io.Closeable;
import java.io.IOException;

/**
 * モデルを書き出すオブジェクトのインターフェース。
 * @param <T> 読み出すモデルの種類
 */
public interface ModelOutput<T> extends Closeable {

    /**
     * 指定のモデルオブジェクトの内容を、このオブジェクトを利用して書き出す。
     * @param model 書き出す対象のモデル
     * @throws IOException モデルの書き出しに失敗した場合
     */
    void write(T model) throws IOException;
}
