package com.asakusafw.compiler.operator.io;
import java.io.IOException;

import com.asakusafw.compiler.operator.model.MockProjection;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>mock_projection</code>を読み出す
 */
public final class MockProjectionInput implements ModelInput<MockProjection> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public MockProjectionInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(MockProjection model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getValueOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}