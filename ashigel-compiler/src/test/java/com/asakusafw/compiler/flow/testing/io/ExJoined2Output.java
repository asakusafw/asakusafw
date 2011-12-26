package com.asakusafw.compiler.flow.testing.io;
import java.io.IOException;

import com.asakusafw.compiler.flow.testing.model.ExJoined2;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * <code>ex_joined2</code>をTSVなどのレコード形式で出力する。
 */
public final class ExJoined2Output implements ModelOutput<ExJoined2> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public ExJoined2Output(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(ExJoined2 model) throws IOException {
        emitter.emit(model.getSid1Option());
        emitter.emit(model.getKeyOption());
        emitter.emit(model.getSid2Option());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}