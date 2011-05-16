package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.testdriver.testing.model.Projection;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>projection</code>を読み出す
 */
public final class ProjectionInput implements ModelInput<Projection> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public ProjectionInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Projection model) throws IOException {
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