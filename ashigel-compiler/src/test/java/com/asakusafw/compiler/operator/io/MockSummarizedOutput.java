package com.asakusafw.compiler.operator.io;
import com.asakusafw.compiler.operator.model.MockSummarized;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import java.io.IOException;
/**
 * <code>mock_summarized</code>をTSVなどのレコード形式で出力する。
 */
@SuppressWarnings("deprecation") public final class MockSummarizedOutput implements ModelOutput<MockSummarized> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public MockSummarizedOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(MockSummarized model) throws IOException {
        emitter.emit(model.getKeyOption());
        emitter.emit(model.getCountOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}