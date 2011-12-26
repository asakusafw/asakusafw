package com.asakusafw.compiler.flow.testing.io;
import java.io.IOException;

import com.asakusafw.compiler.flow.testing.model.KeyConflict;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>key_conflict</code>を読み出す
 */
public final class KeyConflictInput implements ModelInput<KeyConflict> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public KeyConflictInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(KeyConflict model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getKeyOption());
        parser.fill(model.getCountOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}