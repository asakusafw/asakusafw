package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.testdriver.testing.model.Ordered;
import java.io.IOException;
/**
 * <code>ordered</code>をTSVなどのレコード形式で出力する。
 */
public final class OrderedOutput implements ModelOutput<Ordered> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public OrderedOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Ordered model) throws IOException {
        emitter.emit(model.getFirstOption());
        emitter.emit(model.getSecondPropertyOption());
        emitter.emit(model.getAOption());
        emitter.emit(model.getLastOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}