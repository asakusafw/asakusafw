package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.testdriver.testing.model.Variety;
import java.io.IOException;
/**
 * <code>variety</code>をTSVなどのレコード形式で出力する。
 */
@SuppressWarnings("deprecation") public final class VarietyOutput implements ModelOutput<Variety> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public VarietyOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Variety model) throws IOException {
        emitter.emit(model.getPIntOption());
        emitter.emit(model.getPLongOption());
        emitter.emit(model.getPByteOption());
        emitter.emit(model.getPShortOption());
        emitter.emit(model.getPDecimalOption());
        emitter.emit(model.getPTextOption());
        emitter.emit(model.getPBooleanOption());
        emitter.emit(model.getPDateOption());
        emitter.emit(model.getPDatetimeOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}