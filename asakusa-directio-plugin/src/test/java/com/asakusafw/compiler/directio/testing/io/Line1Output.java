package com.asakusafw.compiler.directio.testing.io;
import com.asakusafw.compiler.directio.testing.model.Line1;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import java.io.IOException;
/**
 * <code>line1</code>をTSVなどのレコード形式で出力する。
 */
public final class Line1Output implements ModelOutput<Line1> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public Line1Output(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Line1 model) throws IOException {
        emitter.emit(model.getValueOption());
        emitter.emit(model.getFirstOption());
        emitter.emit(model.getPositionOption());
        emitter.emit(model.getLengthOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}