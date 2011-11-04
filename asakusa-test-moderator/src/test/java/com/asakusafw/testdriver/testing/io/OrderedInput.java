package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.testdriver.testing.model.Ordered;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>ordered</code>を読み出す
 */
public final class OrderedInput implements ModelInput<Ordered> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public OrderedInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Ordered model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getFirstOption());
        parser.fill(model.getSecondPropertyOption());
        parser.fill(model.getAOption());
        parser.fill(model.getLastOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}