package com.asakusafw.compiler.bulkloader.testing.io;
import com.asakusafw.compiler.bulkloader.testing.model.MockUnionModel;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>mock_union_model</code>を読み出す
 */
@SuppressWarnings("deprecation") public final class MockUnionModelInput implements ModelInput<MockUnionModel> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public MockUnionModelInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(MockUnionModel model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getAOption());
        parser.fill(model.getBOption());
        parser.fill(model.getCOption());
        parser.fill(model.getDOption());
        parser.fill(model.getXOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}