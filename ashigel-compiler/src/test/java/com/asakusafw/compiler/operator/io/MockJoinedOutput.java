package com.asakusafw.compiler.operator.io;
import com.asakusafw.compiler.operator.model.MockJoined;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import java.io.IOException;
/**
 * <code>mock_joined</code>をTSVなどのレコード形式で出力する。
 */
public final class MockJoinedOutput implements ModelOutput<MockJoined> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public MockJoinedOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(MockJoined model) throws IOException {
        emitter.emit(model.getHogeValueOption());
        emitter.emit(model.getFooValueOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}