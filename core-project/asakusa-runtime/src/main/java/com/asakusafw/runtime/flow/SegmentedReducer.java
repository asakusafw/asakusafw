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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * {@link SegmentedWritable}をキーとして、セグメントごとの処理を行う{@link Reducer}の骨格実装。
 * @param <KEYIN> 入力するキーの種類
 * @param <VALUEIN> 入力する値の種類
 * @param <KEYOUT> 出力するキーの種類
 * @param <VALUEOUT> 出力する値の種類
 */
public abstract class SegmentedReducer<
        KEYIN extends SegmentedWritable,
        VALUEIN extends SegmentedWritable,
        KEYOUT extends Writable,
        VALUEOUT extends Writable>
        extends Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

    /**
     * {@link #getRendezvous(SegmentedWritable)}のメソッド名。
     */
    public static final String GET_RENDEZVOUS = "getRendezvous";

    /**
     * キーのグループが変更された際、または最初のキーに対して呼び出され、
     * グループに対応する処理断片を返す。
     * @param key 対象のキー
     * @return 対応する処理断片
     */
    protected abstract Rendezvous<VALUEIN> getRendezvous(KEYIN key);

    @Override
    protected void reduce(
            KEYIN key,
            Iterable<VALUEIN> values,
            Context context) throws IOException, InterruptedException {
        Iterator<VALUEIN> iter = values.iterator();
        if (iter.hasNext() == false) {
            // may not occur
            return;
        }
        Rendezvous<VALUEIN> group = getRendezvous(key);
        group.begin();
        while (iter.hasNext()) {
            VALUEIN row = iter.next();
            group.process(row);
        }
        group.end();
    }
}
