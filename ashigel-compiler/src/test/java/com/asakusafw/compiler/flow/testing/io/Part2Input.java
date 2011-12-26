package com.asakusafw.compiler.flow.testing.io;
import java.io.IOException;

import com.asakusafw.compiler.flow.testing.model.Part2;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>part2</code>を読み出す
 */
public final class Part2Input implements ModelInput<Part2> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public Part2Input(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Part2 model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getSidOption());
        parser.fill(model.getStringOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}