package com.asakusafw.compiler.bulkloader.testing.io;
import java.io.IOException;

import com.asakusafw.compiler.bulkloader.testing.model.MockErrorModel;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * <code>mock_error_model</code>をTSVなどのレコード形式で出力する。
 */
public final class MockErrorModelOutput implements ModelOutput<MockErrorModel> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public MockErrorModelOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(MockErrorModel model) throws IOException {
        emitter.emit(model.getAOption());
        emitter.emit(model.getBOption());
        emitter.emit(model.getCOption());
        emitter.emit(model.getDOption());
        emitter.emit(model.getEOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}