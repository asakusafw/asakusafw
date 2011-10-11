package com.asakusafw.compiler.yaess.testing.io;
import com.asakusafw.compiler.yaess.testing.model.Dummy;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import java.io.IOException;
/**
 * <code>dummy</code>をTSVなどのレコード形式で出力する。
 */
public final class DummyOutput implements ModelOutput<Dummy> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public DummyOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Dummy model) throws IOException {
        emitter.emit(model.getValueOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}