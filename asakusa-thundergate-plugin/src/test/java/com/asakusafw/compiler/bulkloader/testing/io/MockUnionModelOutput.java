package com.asakusafw.compiler.bulkloader.testing.io;
import java.io.IOException;

import com.asakusafw.compiler.bulkloader.testing.model.MockUnionModel;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * <code>mock_union_model</code>をTSVなどのレコード形式で出力する。
 */
public final class MockUnionModelOutput implements ModelOutput<MockUnionModel> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public MockUnionModelOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(MockUnionModel model) throws IOException {
        emitter.emit(model.getAOption());
        emitter.emit(model.getBOption());
        emitter.emit(model.getCOption());
        emitter.emit(model.getDOption());
        emitter.emit(model.getXOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}