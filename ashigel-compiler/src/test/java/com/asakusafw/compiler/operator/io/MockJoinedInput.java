package com.asakusafw.compiler.operator.io;
import java.io.IOException;

import com.asakusafw.compiler.operator.model.MockJoined;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>mock_joined</code>を読み出す
 */
public final class MockJoinedInput implements ModelInput<MockJoined> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public MockJoinedInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(MockJoined model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getHogeValueOption());
        parser.fill(model.getFooValueOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}