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
package com.asakusafw.runtime.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * モデルを読み出すオブジェクトのインターフェース。
 * @param <T> 読み出すモデルの種類
 */
public interface ModelInput<T> extends Closeable {

    /**
     * このオブジェクトからモデルを読み出して、指定のオブジェクトに書き出す。
     * @param model 書き出す先のモデル
     * @return モデルを読み出した場合は{@code null}、
     *     これ以上読み出すモデルが存在しない場合は{@code null}
     * @throws IOException モデルの読み出しに失敗した場合
     */
    boolean readTo(T model) throws IOException;
}
