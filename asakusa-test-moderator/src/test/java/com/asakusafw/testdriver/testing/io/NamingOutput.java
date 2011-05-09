package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.testdriver.testing.model.Naming;
import java.io.IOException;
/**
 * <code>naming</code>をTSVなどのレコード形式で出力する。
 */
public final class NamingOutput implements ModelOutput<Naming> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public NamingOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Naming model) throws IOException {
        emitter.emit(model.getAOption());
        emitter.emit(model.getVeryVeryVeryLongNameOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}