package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.testdriver.testing.model.Simple;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>simple</code>を読み出す
 */
@SuppressWarnings("deprecation") public final class SimpleInput implements ModelInput<Simple> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public SimpleInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Simple model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getDataOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}