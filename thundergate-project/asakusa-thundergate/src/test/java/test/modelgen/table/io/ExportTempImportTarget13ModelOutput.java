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
package test.modelgen.table.io;
import java.io.IOException;

import javax.annotation.Generated;

import test.modelgen.table.model.ExportTempImportTarget13;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * {@link ExportTempImportTarget13}をTSV形式で書きだす
 */
@Generated("ModelOutputEmitter:0.0.1")@SuppressWarnings("deprecation") public final class
        ExportTempImportTarget13ModelOutput implements ModelOutput<ExportTempImportTarget13> {
    /**
     * 内部で利用するエミッター。
     */
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する
     * @param emitter 利用するエミッター
     * @throw IllegalArgumentException 引数にnullが指定された場合
     */
    public ExportTempImportTarget13ModelOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(ExportTempImportTarget13 model) throws IOException {
        emitter.emit(model.getVersionNoOption());
        emitter.emit(model.getTempSidOption());
        emitter.emit(model.getSidOption());
        emitter.emit(model.getUpdtDateOption());
        emitter.emit(model.getDuplicateFlgOption());
        emitter.emit(model.getTextdata1Option());
        emitter.emit(model.getIntdata1Option());
        emitter.emit(model.getDatedata1Option());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}