package com.asakusafw.compiler.flow.testing.io;
import java.io.IOException;

import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * <code>ex_summarized</code>をTSVなどのレコード形式で出力する。
 */
public final class ExSummarizedOutput implements ModelOutput<ExSummarized> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public ExSummarizedOutput(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(ExSummarized model) throws IOException {
        emitter.emit(model.getStringOption());
        emitter.emit(model.getValueOption());
        emitter.emit(model.getCountOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}