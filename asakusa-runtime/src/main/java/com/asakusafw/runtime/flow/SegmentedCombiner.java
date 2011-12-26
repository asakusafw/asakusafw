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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.mapreduce.Reducer;

/**
 * {@link SegmentedWritable}をキーとして、セグメントごとの処理を行う{@code Combiner}の骨格実装。
 * @param <KEY> キーの種類
 * @param <VALUE> 値の種類
 */
public abstract class SegmentedCombiner<
        KEY extends SegmentedWritable,
        VALUE extends SegmentedWritable>
        extends Reducer<KEY, VALUE, KEY, VALUE> {

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
    protected abstract Rendezvous<VALUE> getRendezvous(KEY key);

    @Override
    protected void reduce(
            KEY key,
            Iterable<VALUE> values,
            Context context) throws IOException, InterruptedException {
        Iterator<VALUE> iter = values.iterator();
        if (iter.hasNext() == false) {
            // may not occur
            return;
        }
        Rendezvous<VALUE> group = getRendezvous(key);
        if (group == null) {
            while (iter.hasNext()) {
                VALUE row = iter.next();
                KEY current = context.getCurrentKey();
                context.write(current, row);
            }
        } else {
            group.begin();
            while (iter.hasNext()) {
                VALUE row = iter.next();
                group.process(row);
            }
            group.end();
        }
    }
}
