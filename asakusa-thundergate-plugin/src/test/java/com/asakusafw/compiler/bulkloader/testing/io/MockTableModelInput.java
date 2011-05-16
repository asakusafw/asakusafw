package com.asakusafw.compiler.bulkloader.testing.io;
import com.asakusafw.compiler.bulkloader.testing.model.MockTableModel;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>mock_table_model</code>を読み出す
 */
public final class MockTableModelInput implements ModelInput<MockTableModel> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public MockTableModelInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(MockTableModel model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getAOption());
        parser.fill(model.getBOption());
        parser.fill(model.getCOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}