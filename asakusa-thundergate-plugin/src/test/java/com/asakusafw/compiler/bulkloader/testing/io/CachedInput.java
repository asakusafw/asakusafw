package com.asakusafw.compiler.bulkloader.testing.io;
import com.asakusafw.compiler.bulkloader.testing.model.Cached;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import java.io.IOException;
/**
 * TSVファイルなどのレコードを表すファイルを入力として<code>cached</code>を読み出す
 */
public final class CachedInput implements ModelInput<Cached> {
    private final RecordParser parser;
    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     * @throws IllegalArgumentException 引数に<code>null</code>が指定された場合
     */
    public CachedInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException("parser");
        }
        this.parser = parser;
    }
    @Override public boolean readTo(Cached model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getSidOption());
        parser.fill(model.getTimestampOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}