package com.asakusafw.compiler.flow.testing.io;
import com.asakusafw.compiler.flow.testing.model.Part1;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import java.io.IOException;
/**
 * <code>part1</code>をTSVなどのレコード形式で出力する。
 */
public final class Part1Output implements ModelOutput<Part1> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public Part1Output(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Part1 model) throws IOException {
        emitter.emit(model.getSidOption());
        emitter.emit(model.getValueOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}