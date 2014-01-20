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
package com.asakusafw.testdriver.testing.io;
import java.io.IOException;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.testdriver.testing.model.Variety;
/**
 * <code>variety</code>をTSVなどのレコード形式で出力する。
 */
public final class VarietyOutput implements ModelOutput<Variety> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public VarietyOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Variety model) throws IOException {
        emitter.emit(model.getPIntOption());
        emitter.emit(model.getPLongOption());
        emitter.emit(model.getPByteOption());
        emitter.emit(model.getPShortOption());
        emitter.emit(model.getPDecimalOption());
        emitter.emit(model.getPFloatOption());
        emitter.emit(model.getPDoubleOption());
        emitter.emit(model.getPTextOption());
        emitter.emit(model.getPBooleanOption());
        emitter.emit(model.getPDateOption());
        emitter.emit(model.getPDatetimeOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}