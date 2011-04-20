package com.asakusafw.compiler.flow.testing.io;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import java.io.IOException;
/**
 * <code>ex_summarized2</code>をTSVなどのレコード形式で出力する。
 */
@SuppressWarnings("deprecation") public final class ExSummarized2Output implements ModelOutput<ExSummarized2> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public ExSummarized2Output(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(ExSummarized2 model) throws IOException {
        emitter.emit(model.getKeyOption());
        emitter.emit(model.getValueOption());
        emitter.emit(model.getCountOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}