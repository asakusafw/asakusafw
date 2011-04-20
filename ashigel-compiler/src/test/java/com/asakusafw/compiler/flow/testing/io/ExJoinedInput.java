package com.asakusafw.compiler.flow.testing.io;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>ex_joined</code>を読み出す
 */
@SuppressWarnings("deprecation") public final class ExJoinedInput implements ModelInput<ExJoined> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public ExJoinedInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(ExJoined model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getSid1Option());
        parser.fill(model.getValueOption());
        parser.fill(model.getSid2Option());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}