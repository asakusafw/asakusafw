/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

/**
 * 任意の{@link Writable}を保持するスロット。
 */
public class WritableSlot implements Writable {

    private final DataOutputBuffer output = new DataOutputBuffer();

    private final DataInputBuffer input = new DataInputBuffer();

    /**
     * このオブジェクトに指定の{@link Writable}オブジェクトの内容を書き出す。
     * @param data 書き出すオブジェクト
     * @throws IOException 書き出せなかった場合
     */
    public void store(Writable data) throws IOException {
        output.reset();
        data.write(output);
    }

    /**
     * 指定のオブジェクトにこのオブジェクトの内容を書き出す。
     * @param data 書き出すオブジェクト
     * @throws IOException 書き出せなかった場合
     */
    public void loadTo(Writable data) throws IOException {
        input.reset(output.getData(), output.getLength());
        data.readFields(input);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeVInt(out, output.getLength());
        out.write(output.getData(), 0, output.getLength());
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        output.reset();
        int length = WritableUtils.readVInt(in);
        output.write(in, length);
    }
}
