package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.testdriver.testing.model.Simple;
import java.io.IOException;
/**
 * <code>simple</code>をTSVなどのレコード形式で出力する。
 */
@SuppressWarnings("deprecation") public final class SimpleOutput implements ModelOutput<Simple> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public SimpleOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Simple model) throws IOException {
        emitter.emit(model.getDataOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}