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
import java.io.Flushable;
import java.io.IOException;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;


/**
 * {@link ValueOption}の内容をそれぞれのメンバーとして、レコードの形式で出力する。
 * <p>
 * 次のように利用する。
 * </p>
<pre><code>
Iterator&lt;SomeModel&gt; models = ...;
try {
    RecordEmitter emitter = ...;
    while (models.hasNext();) {
        SomeModel model = models.next();
        emitter.emit(model.getHogeOption());
        emitter.emit(model.getFooOption());
        emitter.emit(model.getBarOption());
        emitter.endRecord();
    }
    emitter.close();
}
finally {
    writer.close();
}
</code></pre>
 * <p>
 * 特に指定がない限り、このインターフェースのメソッドの引数に{@code null}を指定した場合には
 * {@link NullPointerException}がスローされる。
 * </p>
 */
public interface RecordEmitter extends Flushable, Closeable {

    /**
     * 現在のレコードの出力を終了し、次のレコードの出力に備える。
     * @throws IOException 書き出しに失敗した場合
     */
    void endRecord() throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(BooleanOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(ByteOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(ShortOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(IntOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(LongOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(FloatOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(DoubleOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(DecimalOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(StringOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(DateOption option) throws IOException;

    /**
     * 指定のオブジェクトの内容を、次のレコード上のセルとして書き出す。
     * @param option 書き出す対象のオブジェクト
     * @throws IOException 書き出しに失敗した場合
     */
    void emit(DateTimeOption option) throws IOException;

}