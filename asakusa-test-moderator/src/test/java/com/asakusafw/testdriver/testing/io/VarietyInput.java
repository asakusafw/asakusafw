package com.asakusafw.testdriver.testing.io;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.testdriver.testing.model.Variety;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>variety</code>を読み出す
 */
public final class VarietyInput implements ModelInput<Variety> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public VarietyInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Variety model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getPIntOption());
        parser.fill(model.getPLongOption());
        parser.fill(model.getPByteOption());
        parser.fill(model.getPShortOption());
        parser.fill(model.getPDecimalOption());
        parser.fill(model.getPFloatOption());
        parser.fill(model.getPDoubleOption());
        parser.fill(model.getPTextOption());
        parser.fill(model.getPBooleanOption());
        parser.fill(model.getPDateOption());
        parser.fill(model.getPDatetimeOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}