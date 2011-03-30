/**
 * Copyright 2011 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.runtime.io;

import java.io.Closeable;
import java.io.IOException;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;


/**
 * レコードを表現するデータを解析し、それぞれのメンバーを{@link ValueOption}にその値を設定する。
 * <p>
 * 次のように利用する。
 * </p>
<pre><code>
Reader reader = ...;
SomeModel model = new SomeModel();
try {
    RecordParser parser = ...;
    while (parser.hasNext()) {
        parser.fill(model.getHogeOption());
        parser.fill(model.getFooOption());
        parser.fill(model.getBarOption());
        // 任意の処理
        performModelAction(model);
    }
}
finally {
    reader.close();
}
</code></pre>
 * <p>
 * 特に指定がない限り、このインターフェースのメソッドの引数に{@code null}を指定した場合には
 * {@link NullPointerException}がスローされる。
 * </p>
 */
public interface RecordParser extends Closeable {

    /**
     * 次のレコードに処理を移す。
     * <p>
     * このメソッドは、オブジェクトの初期化直後、またはレコードを読み終わった後に
     * 実行される事を想定しており、それ以外で呼び出された場合には
     * レコードの形式が間違っているとみなされる。
     * </p>
     * 次のレコードが存在する場合のみ{@code true}を返す。
     * @return 次のレコードが存在する場合のみ{@code true}
     * @throws RecordFormatException TSVの形式が正しくない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    boolean next() throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(BooleanOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(ByteOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(ShortOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(IntOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(LongOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(DecimalOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(StringOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * <p>
     * 年、月、日のいずれかに0が指定された場合、同セルを空のセルとして取扱う。
     * </p>
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(DateOption option) throws RecordFormatException, IOException;

    /**
     * このパーサーが読む次のセルの内容を、指定のオブジェクトに格納する。
     * <p>
     * 年、月、日のいずれかに0が指定された場合、同セルを空のセルとして取扱う。
     * </p>
     * @param option 格納先のオブジェクト
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    void fill(DateTimeOption option) throws RecordFormatException, IOException;

}