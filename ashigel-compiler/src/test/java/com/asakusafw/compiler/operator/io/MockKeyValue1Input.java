package com.asakusafw.compiler.operator.io;
import com.asakusafw.compiler.operator.model.MockKeyValue1;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>mock_key_value1</code>を読み出す
 */
public final class MockKeyValue1Input implements ModelInput<MockKeyValue1> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public MockKeyValue1Input(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(MockKeyValue1 model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getKeyOption());
        parser.fill(model.getValueOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}