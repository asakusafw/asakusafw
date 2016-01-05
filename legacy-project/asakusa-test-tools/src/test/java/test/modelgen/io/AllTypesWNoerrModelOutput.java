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
package test.modelgen.io;
import java.io.IOException;

import test.modelgen.model.AllTypesWNoerr;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * {@link AllTypesWNoerr}をTSV形式で書きだす
 */
public final class AllTypesWNoerrModelOutput implements ModelOutput<AllTypesWNoerr> {
    /**
     * 内部で利用するエミッター。
     */
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する
     * @param emitter 利用するエミッター
     * @throw IllegalArgumentException 引数にnullが指定された場合
     */
    public AllTypesWNoerrModelOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(AllTypesWNoerr model) throws IOException {
        emitter.emit(model.getCTagOption());
        emitter.emit(model.getCCommentOption());
        emitter.emit(model.getCBigintOption());
        emitter.emit(model.getCIntOption());
        emitter.emit(model.getCSmallintOption());
        emitter.emit(model.getCTinyintOption());
        emitter.emit(model.getCCharOption());
        emitter.emit(model.getCDatetimeOption());
        emitter.emit(model.getCDateOption());
        emitter.emit(model.getCDecimal200Option());
        emitter.emit(model.getCDecimal255Option());
        emitter.emit(model.getCVcharOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}