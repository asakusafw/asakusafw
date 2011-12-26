package com.asakusafw.compiler.operator.io;
import java.io.IOException;

import com.asakusafw.compiler.operator.model.MockKey;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * <code>mock_key</code>をTSVなどのレコード形式で出力する。
 */
public final class MockKeyOutput implements ModelOutput<MockKey> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public MockKeyOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(MockKey model) throws IOException {
        emitter.emit(model.getKeyOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}