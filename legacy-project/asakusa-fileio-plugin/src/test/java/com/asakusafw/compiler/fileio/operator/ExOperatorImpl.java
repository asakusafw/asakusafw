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
package com.asakusafw.compiler.fileio.operator;
import javax.annotation.Generated;

import com.asakusafw.compiler.fileio.model.Ex1;
import com.asakusafw.compiler.fileio.model.ExSummarized;
/**
 * {@link ExOperator}に関する演算子実装クラス。
 */
@Generated("OperatorImplementationClassGenerator:0.0.1") public class ExOperatorImpl extends ExOperator {
    /**
     * インスタンスを生成する。
     */
    public ExOperatorImpl() {
        return;
    }
    @Override public ExSummarized summarize(Ex1 model) {
        throw new UnsupportedOperationException("単純集計演算子は組み込みの方法で処理されます");
    }
}