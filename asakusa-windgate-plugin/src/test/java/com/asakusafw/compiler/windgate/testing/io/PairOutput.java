package com.asakusafw.compiler.windgate.testing.io;
import com.asakusafw.compiler.windgate.testing.model.Pair;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import java.io.IOException;
/**
 * <code>pair</code>をTSVなどのレコード形式で出力する。
 */
public final class PairOutput implements ModelOutput<Pair> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public PairOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Pair model) throws IOException {
        emitter.emit(model.getKeyOption());
        emitter.emit(model.getValueOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}