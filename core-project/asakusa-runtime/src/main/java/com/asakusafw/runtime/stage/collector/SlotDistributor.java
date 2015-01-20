/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.collector;

import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

import com.asakusafw.runtime.flow.MapperWithRuntimeResource;

/**
 * {@link SlotSorter}にオブジェクトを配布する{@link Mapper}の骨格。
 * @param <T> 配布する{@link Writable}の種類
 * @since 0.1.0
 * @version 0.5.1
 */
public abstract class SlotDistributor<T extends Writable> extends MapperWithRuntimeResource<
        Object, T,
        SortableSlot, WritableSlot> {

    /**
     * {@link #setSlotSpec(Writable, SortableSlot)}のメソッド名。
     */
    public static final String NAME_SET_SLOT_SPEC = "setSlotSpec";

    private final SortableSlot keyOut = new SortableSlot();

    private final WritableSlot valueOut = new WritableSlot();

    /**
     * 指定の値のソート情報を指定のスロットに設定する。
     * <p>
     * スロットは{@link SortableSlot#begin(int)}でスロット番号を設定するところから始めること。
     * </p>
<pre><code>
slot.begin(MY_SLOT_NUMBER);
slot.addWritable(value.getPrimaryKey());
slot.addWritable(value.getSecondaryKey());
...
</code></pre>
     * @param value スロットの情報
     * @param slot 書き込み先のスロット
     * @throws IOException 出力似失敗した場合
     */
    protected abstract void setSlotSpec(T value, SortableSlot slot) throws IOException;

    @Override
    protected void map(Object key, T value, Context context) throws IOException, InterruptedException {
        valueOut.store(value);
        setSlotSpec(value, keyOut);
        context.write(keyOut, valueOut);
    }
}
