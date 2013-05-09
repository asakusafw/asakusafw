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
package com.asakusafw.compiler.flow.processor.operator;

import java.util.List;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.GroupSort;


/**
 * {@link GroupSort}のテスト。
 */
public abstract class GroupSortFlow {

    /**
     * グループ内で値が最小のものを返す。
     * @param a1 グループ
     * @param r1 結果
     */
    @GroupSort
    public void min(
            @Key(group = "string", order = "value asc") List<Ex1> a1,
            Result<Ex1> r1) {
        r1.add(a1.get(0));
    }

    /**
     * グループ内で値が最大のものを返す。
     * @param a1 グループ
     * @param r1 結果
     */
    @GroupSort
    public void max(
            @Key(group = "string", order = "value desc") List<Ex1> a1,
            Result<Ex1> r1) {
        r1.add(a1.get(0));
    }

    /**
     * 値がパラメーター以下かそうでないかで結果の出力先を変える。
     * @param a1 グループ
     * @param r1 パラメーター以下の値を持つ結果
     * @param r2 パラメーターを超える値を持つ結果
     * @param parameter パラメーター
     */
    @CoGroup
    public void withParameter(
            @Key(group = "string", order = "value asc") List<Ex1> a1,
            Result<Ex1> r1,
            Result<Ex1> r2,
            int parameter) {
        boolean over = false;
        for (Ex1 e : a1) {
            over |= (e.getValue() > parameter);
            if (over) {
                r2.add(e);
            } else {
                r1.add(e);
            }
        }
    }
}
