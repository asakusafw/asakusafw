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
package ${package}.operator;

import ${package}.modelgen.dmdl.model.Ex1;

import java.util.List;

import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.Branch;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.Update;

/**
 * テスト用の演算子。
 */
public abstract class ExOperator {

    /**
     * モデルの値を指定の値だけ増加させる。
     * @param model 対象のモデル
     * @param value 増加させる値
     */
    @Update
    public void update(Ex1 model, int value) {
        model.setValue(model.getValueOption().or(0) + value);
    }

    /**
     * モデルの値が1ならばyes, 0ならばno, それ以外ならばcancelを返す。
     * @param model 対象のモデル
     * @return yes, no, cancelのいずれか
     */
    @Branch
    public Answer branch(Ex1 model) {
        int value = model.getValue();
        if (value == 1) {
            return Answer.YES;
        }
        if (value == 0) {
            return Answer.NO;
        }
        return Answer.CANCEL;
    }

    /**
     * 指定のモデルをそれぞれグループ化して整列し、それぞれの先頭要素を結果に書き出す。
     * グループに値が存在しない場合はその値に関しては何も行わない。
     * @param ex1 モデル1 (VALUEでグループ化、SIDでソート)
     * @param ex2 モデル1 (VALUEでグループ化、STRINGでソート)
     * @param r1 モデル1の先頭要素を書き出す先
     * @param r2 モデル2の先頭要素を書き出す先
     */
    @CoGroup
    public void cogroup(
            @Key(group = "value", order = "sid") List<Ex1> ex1,
            @Key(group = "value", order = "string") List<Ex1> ex2,
            Result<Ex1> r1,
            Result<Ex1> r2) {
        if (ex1.isEmpty() == false) {
            r1.add(ex1.get(0));
        }
        if (ex2.isEmpty() == false) {
            r2.add(ex2.get(0));
        }
    }

    /**
     * YES, NO, CANCEL。
     */
    public enum Answer {

        /**
         * yes (1の場合)。
         */
        YES,

        /**
         * no (0の場合)。
         */
        NO,

        /**
         * cancel (0, 1のいずれでもない場合)。
         */
        CANCEL,
    }
}
