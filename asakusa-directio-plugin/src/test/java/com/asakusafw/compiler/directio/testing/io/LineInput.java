package com.asakusafw.compiler.directio.testing.io;
import com.asakusafw.compiler.directio.testing.model.Line;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>line</code>を読み出す
 */
public final class LineInput implements ModelInput<Line> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public LineInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Line model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getValueOption());
        parser.fill(model.getFirstOption());
        parser.fill(model.getPositionOption());
        parser.fill(model.getLengthOption());
        parser.endRecord();
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}