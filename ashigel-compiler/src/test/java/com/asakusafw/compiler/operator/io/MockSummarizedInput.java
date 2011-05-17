package com.asakusafw.compiler.operator.io;
import com.asakusafw.compiler.operator.model.MockSummarized;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>mock_summarized</code>を読み出す
 */
public final class MockSummarizedInput implements ModelInput<MockSummarized> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public MockSummarizedInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(MockSummarized model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getKeyOption());
        parser.fill(model.getCountOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}