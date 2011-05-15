package com.asakusafw.compiler.flow.testing.io;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>ex_summarized2</code>を読み出す
 */
public final class ExSummarized2Input implements ModelInput<ExSummarized2> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public ExSummarized2Input(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(ExSummarized2 model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getKeyOption());
        parser.fill(model.getValueOption());
        parser.fill(model.getCountOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}