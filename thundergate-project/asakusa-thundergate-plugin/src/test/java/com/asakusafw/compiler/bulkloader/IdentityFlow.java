/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.compiler.bulkloader;

import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * テスト用の恒等フロー。
 * @param <T> フローのデータ種
 */
public class IdentityFlow<T> extends FlowDescription {

    private In<T> in;

    private Out<T> out;

    /**
     * インスタンスを生成する。
     * @param in 入力
     * @param out 出力
     */
    public IdentityFlow(In<T> in, Out<T> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        out.add(in);
    }
}
