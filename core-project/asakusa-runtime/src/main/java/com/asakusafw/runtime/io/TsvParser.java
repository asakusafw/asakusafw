/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import static com.asakusafw.runtime.io.TsvConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * TSVファイルを解析してレコードを読み出す。
 * <p>
 * 次のように利用する。
 * </p>
<pre><code>
Reader reader = ...;
SomeModel model = new SomeModel();
try {
    TsvParser parser = new TsvParser(reader);
    while (parser.hasNext()) {
        parser.fill(model.getHogeOption());
        parser.fill(model.getFooOption());
        parser.fill(model.getBarOption());

        performModelAction(model);
    }
}
finally {
    reader.close();
}
</code></pre>
 * <p>
 * 特に指定がない限り、このクラスのメソッドの引数に{@code null}を指定した場合には
 * {@link NullPointerException}がスローされる。
 * </p>
 */
@SuppressWarnings("deprecation")
public final class TsvParser implements RecordParser {

    private static final Pattern SPECIAL_FLOAT = Pattern.compile("(\\+?Inf.*)|(-Inf.*)|((\\+|-)?[Nn]a[Nn])");

    private static final int SPECIAL_FLOAT_POSITIVE_INF = 1;

    private static final int SPECIAL_FLOAT_NEGATIVE_INF = 2;

    private static final Charset TEXT_ENCODE = Charset.forName("UTF-8");

    private static final int INITIAL_BUFFER_SIZE = 2048;

    private final Reader reader;

    private final CharsetEncoder encoder;

    private int lastSeparator;

    private int lookAhead;

    private char[] charBuffer;

    private CharBuffer wrappedCharBuffer;

    private final ByteBuffer encodeBuffer;

    /**
     * インスタンスを生成する。
     * @param reader TSVの内容を読み出すリーダー
     * @throws IOException 初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public TsvParser(Reader reader) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null"); //$NON-NLS-1$
        }
        if (reader instanceof BufferedReader) {
            this.reader = reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
        this.encoder = TEXT_ENCODE
            .newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        this.charBuffer = new char[INITIAL_BUFFER_SIZE];
        this.lastSeparator = RECORD_SEPARATOR;
        this.encodeBuffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
        fillLookAhead();
    }

    /**
     * 先読みバッファに一文字先読みする。
     * <p>
     * 現在の実装では、全てのメソッドの終了時に{@link #lookAhead}に
     * ストリームの次の文字が格納されているものとする。
     * </p>
     * @throws IOException 先読みに失敗した場合
     */
    private void fillLookAhead() throws IOException {
        this.lookAhead = reader.read();
    }

    // 拡張のため、IOExceptionを残す
    @Override
    public boolean next() throws RecordFormatException, IOException {
        lastSeparator = CELL_SEPARATOR;
        return lookAhead != -1;
    }

    /**
     * 現在の解析位置がセルの先頭であることを検証する。
     * @throws RecordFormatException セルの先頭でない場合
     */
    private void checkCellStart() throws RecordFormatException {
        // パフォーマンスによってはこの呼び出しを省略する
        if (lastSeparator != CELL_SEPARATOR || lookAhead == -1) {
            throw new RecordFormatException("Next cell is not started");
        }
    }

    @Override
    public void fill(BooleanOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);

        if (lookAhead == BOOLEAN_TRUE) {
            option.modify(true);
        } else if (lookAhead == BOOLEAN_FALSE) {
            option.modify(false);
        } else {
            throw new RecordFormatException(MessageFormat.format(
                    "Invalid character {0} for boolean",
                    (char) lookAhead));
        }
        int next = reader.read();
        if (isSeparator(next) == false) {
            throw new RecordFormatException(MessageFormat.format(
                    "Invalid character {0} for boolean",
                    (char) next));
        }
        setLastSeparator(next);
        fillLookAhead();
    }

    @Override
    public void fill(ByteOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        option.modify((byte) readInt(option));
        fillLookAhead();
    }

    @Override
    public void fill(ShortOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        option.modify((short) readInt(option));
        fillLookAhead();
    }

    @Override
    public void fill(IntOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        int value = readInt(option);
        option.modify(value);
        fillLookAhead();
    }

    @Override
    public void fill(LongOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        boolean negative = false;
        if (lookAhead == '-') {
            lookAhead = reader.read();
            negative = true;
        }
        assertHasRest(option, lookAhead);
        long value = toNumber(lookAhead);
        while (true) {
            int c = reader.read();
            if (isSeparator(c)) {
                setLastSeparator(c);
                break;
            }
            value = value * 10L + toNumber(c);
        }
        if (negative) {
            value = -value;
        }
        option.modify(value);
        fillLookAhead();
    }

    @Override
    public void fill(FloatOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);
        charBuffer[0] = (char) lookAhead;
        int length = readString(1, option);
        String string = new String(charBuffer, 0, length + 1);
        try {
            option.modify(Float.parseFloat(string));
        } catch (NumberFormatException e) {
            Matcher matcher = SPECIAL_FLOAT.matcher(string);
            if (matcher.matches()) {
                if (matcher.group(SPECIAL_FLOAT_POSITIVE_INF) != null) {
                    option.modify(Float.POSITIVE_INFINITY);
                } else if (matcher.group(SPECIAL_FLOAT_NEGATIVE_INF) != null) {
                    option.modify(Float.NEGATIVE_INFINITY);
                } else {
                    option.modify(Float.NaN);
                }
            } else {
                throw new RecordFormatException(MessageFormat.format(
                        "Invalid character in floating-point context {0}",
                        string), e);
            }
        }
        fillLookAhead();
    }

    @Override
    public void fill(DoubleOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);
        charBuffer[0] = (char) lookAhead;
        int length = readString(1, option);
        String string = new String(charBuffer, 0, length + 1);
        try {
            option.modify(Double.parseDouble(string));
        } catch (NumberFormatException e) {
            Matcher matcher = SPECIAL_FLOAT.matcher(string);
            if (matcher.matches()) {
                if (matcher.group(SPECIAL_FLOAT_POSITIVE_INF) != null) {
                    option.modify(Double.POSITIVE_INFINITY);
                } else if (matcher.group(SPECIAL_FLOAT_NEGATIVE_INF) != null) {
                    option.modify(Double.NEGATIVE_INFINITY);
                } else {
                    option.modify(Double.NaN);
                }
            } else {
                throw new RecordFormatException(MessageFormat.format(
                        "Invalid character in floating-point context {0}",
                        string), e);
            }
        }
        fillLookAhead();
    }

    @Override
    public void fill(DecimalOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        assertHasRest(option, lookAhead);
        charBuffer[0] = (char) lookAhead;
        int length = readString(1, option);
        // TODO BigDecimal
        option.modify(new BigDecimal(charBuffer, 0, length + 1));
        fillLookAhead();
    }

    @Override
    public void fill(StringOption option) throws RecordFormatException, IOException {
        checkCellStart();

        // 書き込み用にcharBufferを初期化
        if (wrappedCharBuffer == null) {
            wrappedCharBuffer = CharBuffer.wrap(charBuffer);
        } else {
            wrappedCharBuffer.clear();
        }

        // 書き込み先のTextも初期化
        option.reset();

        if (lookAhead == ESCAPE_CHAR) {
            int c = reader.read();
            if (c == ESCAPE_NULL_COLUMN) {
                option.setNull();
                int next = reader.read();
                if (isSeparator(next) == false) {
                    throw new RecordFormatException(MessageFormat.format(
                            "Missing separator for {0}",
                            option.getClass().getSimpleName()));
                }
                setLastSeparator(next);
                fillLookAhead();
                return;
            }
            wrappedCharBuffer.append(unescape(c));
        } else if (isSeparator(lookAhead)) {
            setLastSeparator(lookAhead);
            fillLookAhead();
            return;
        } else {
            wrappedCharBuffer.append((char) lookAhead);
        }

        while (true) {
            int c = reader.read();
            if (isSeparator(c)) {
                setLastSeparator(c);
                break;
            } else if (c == ESCAPE_CHAR) {
                int trailing = reader.read();
                wrappedCharBuffer.append(unescape(trailing));
            } else {
                wrappedCharBuffer.append((char) c);
            }

            // バッファが溢れる前に書き出す
            if (wrappedCharBuffer.position() == wrappedCharBuffer.limit()) {
                wrappedCharBuffer.flip();
                append(wrappedCharBuffer, option);
                wrappedCharBuffer.clear();
            }
        }
        // 残りのバッファを書き出す
        wrappedCharBuffer.flip();
        append(wrappedCharBuffer, option);
        wrappedCharBuffer.clear();

        fillLookAhead();
    }

    @Override
    public void fill(DateOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        int year = toNumber(lookAhead) * 1000 + readNumbers(YEAR_FIELD_LENGTH - 1, option);
        consume(DATE_FIELD_SEPARATOR);
        int month = readNumbers(MONTH_FIELD_LENGTH, option);
        consume(DATE_FIELD_SEPARATOR);
        int day = readNumbers(DATE_FIELD_LENGTH, option);

        int last = reader.read();
        if (isSeparator(last) == false) {
            throw new RecordFormatException(MessageFormat.format(
                    "Missing separator for {0}",
                    option.getClass().getSimpleName()));
        }
        setLastSeparator(last);
        if (year == 0 || month == 0 || day == 0) {
            option.setNull();
        } else {
            option.modify(DateUtil.getDayFromDate(year, month, day));
        }
        fillLookAhead();
    }

    @Override
    public void fill(DateTimeOption option) throws RecordFormatException, IOException {
        checkCellStart();
        if (applyNull(option)) {
            return;
        }
        int year = toNumber(lookAhead) * 1000 + readNumbers(YEAR_FIELD_LENGTH - 1, option);
        consume(DATE_FIELD_SEPARATOR);
        int month = readNumbers(MONTH_FIELD_LENGTH, option);
        consume(DATE_FIELD_SEPARATOR);
        int day = readNumbers(DATE_FIELD_LENGTH, option);
        consume(DATE_TIME_SEPARATOR);
        int hour = readNumbers(HOUR_FIELD_LENGTH, option);
        consume(TIME_FIELD_SEPARATOR);
        int minute = readNumbers(MINUTE_FIELD_LENGTH, option);
        consume(TIME_FIELD_SEPARATOR);
        int second = readNumbers(SECOND_FIELD_LENGTH, option);

        int last = reader.read();
        if (isSeparator(last) == false) {
            throw new RecordFormatException(MessageFormat.format(
                    "Missing separator for {0}",
                    option.getClass().getSimpleName()));
        }
        setLastSeparator(last);
        if (year == 0 || month == 0 || day == 0) {
            option.setNull();
        } else {
            long result = DateUtil.getDayFromDate(year, month, day);
            result *= 24L * 60L * 60L;
            result += DateUtil.getSecondFromTime(hour, minute, second);
            option.modify(result);
        }
        fillLookAhead();
    }

    private int readNumbers(int columns, ValueOption<?> option) throws IOException {
        int total = 0;
        for (int i = 0; i < columns; i++) {
            int c = reader.read();
            total = total * 10 + toNumber(c);
        }
        return total;
    }

    private void consume(char expect) throws IOException {
        int c = reader.read();
        if (c != expect) {
            throw new RecordFormatException(MessageFormat.format(
                    "Invalid character in expected ''{0}'' but was \"{1}\"",
                    expect,
                    String.format("\\u%04x", c)));
        }
    }

    private int toNumber(int c) throws RecordFormatException {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        throw new RecordFormatException(MessageFormat.format(
                "Invalid character in number context {0}",
                String.format("\\u%04x", c)));
    }

    private void append(CharBuffer source, StringOption target) throws RecordFormatException {
        if (source.hasRemaining() == false) {
            return;
        }

        Text text = target.get();
        encoder.reset();
        encodeBuffer.clear();
        while (true) {
            CoderResult result = encoder.encode(source, encodeBuffer, true);
            if (result.isError()) {
                throw new RecordFormatException(MessageFormat.format(
                        "Cannot process a character string (\"{0}\")",
                        result));
            }
            if (result.isUnderflow()) {
                consumeEncoded(text);
                break;
            }
            if (result.isOverflow()) {
                consumeEncoded(text);
            }
        }
        while (true) {
            CoderResult result = encoder.flush(encodeBuffer);
            if (result.isError()) {
                throw new RecordFormatException(MessageFormat.format(
                        "Cannot process a character string (\"{0}\")",
                        result));
            }
            if (result.isUnderflow()) {
                consumeEncoded(text);
                break;
            }
            if (result.isOverflow()) {
                consumeEncoded(text);
            }
        }
    }

    private void consumeEncoded(Text text) {
        encodeBuffer.flip();
        if (encodeBuffer.hasRemaining()) {
            text.append(
                    encodeBuffer.array(),
                    encodeBuffer.position(),
                    encodeBuffer.limit());
        }
        encodeBuffer.clear();
    }

    private char unescape(int c) throws RecordFormatException {
        if (c == ESCAPE_CHAR) {
            return ESCAPE_CHAR;
        }
        if (c == ESCAPE_HT) {
            return '\t';
        }
        if (c == ESCAPE_LF) {
            return '\n';
        }
        throw new RecordFormatException(MessageFormat.format(
                "Unknown escape character \\{0} ({1}) for StringOption",
                (char) c,
                String.format("U%04x", c)));
    }

    /**
     * TSVから読み出したセルの数値を返す。
     * <p>
     * この呼び出しによってストリームの位置は次のセパレータの次の文字を指すようになり、
     * 先読みバッファの内容はセパレータの文字を保持する。
     * </p>
     * @param option 最終先に書き出す予定のオブジェクト
     * @return 読み出した文字数
     * @throws RecordFormatException TSVの内容を解釈できない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    private int readInt(ValueOption<?> option) throws IOException,
            RecordFormatException {
        boolean negative = false;
        if (lookAhead == '-') {
            lookAhead = reader.read();
            negative = true;
        }
        assertHasRest(option, lookAhead);
        int value = toNumber(lookAhead);
        while (true) {
            int c = reader.read();
            if (isSeparator(c)) {
                setLastSeparator(c);
                break;
            }
            value = value * 10 + toNumber(c);
        }
        if (negative) {
            value = -value;
        }
        return value;
    }

    private void setLastSeparator(int c) {
        lastSeparator = c;
    }

    /**
     * {@link #charBuffer}にTSVから読み出したセルの文字列の内容を格納する。
     * <p>
     * 読み出した文字列は、バッファの{@code start}から順に書きこんでゆき、
     * セパレータとなる文字の手前までが書き込まれる。
     * </p>
     * <p>
     * この呼び出しによってストリームの位置は次のセパレータの次の文字を指すようになる。
     * </p>
     * @param start 書き出す開始位置
     * @param option 最終先に書き出す予定のオブジェクト
     * @return 読み出した文字数
     * @throws IOException TSVの読み出しに失敗した場合
     */
    private int readString(int start, ValueOption<?> option) throws IOException {
        int current = start;
        while (true) {
            char[] cbuf = charBuffer;
            for (int i = current, n = cbuf.length; i < n; i++) {
                int c = reader.read();
                if (isSeparator(c)) {
                    setLastSeparator(c);
                    return i - start;
                }
                cbuf[i] = (char) c;
            }
            current = cbuf.length;
            expandCharBuffer();
        }
    }

    private void expandCharBuffer() {
        char[] newBuffer = new char[charBuffer.length * 2];
        System.arraycopy(charBuffer, 0, newBuffer, 0, charBuffer.length);
        charBuffer = newBuffer;
        wrappedCharBuffer = null;
    }

    private static boolean isSeparator(int c) {
        return c == -1
                || c == CELL_SEPARATOR
                || c == RECORD_SEPARATOR;
    }

    private void assertHasRest(ValueOption<?> option, int c) throws RecordFormatException {
        if (isSeparator(c)) {
            throw new RecordFormatException(MessageFormat.format(
                    "Empty value for {0}",
                    option.getClass().getSimpleName()));
        }
    }

    /**
     * 次の文字がエスケープ記号である場合、そのセルを{@code null}を表すセルと仮定して
     * 解析を行い、指定の値に{@code null}を設定する。
     * <p>
     * 次の文字がエスケープ記号でない場合はストリームの状態を変更しない。
     * </p>
     * @param option 指定先の値
     * @return 現在のセルが{@code null}を表すセルである場合に{@code true}、
     *     それ以外の場合は{@code false}
     * @throws RecordFormatException エスケープ記号が先頭にありながら、
     *     そのセルが{@code null}を表さない場合
     * @throws IOException TSVの読み出しに失敗した場合
     */
    private boolean applyNull(ValueOption<?> option) throws RecordFormatException, IOException {
        if (lookAhead != ESCAPE_CHAR) {
            return false;
        }
        int c = reader.read();
        if (c == ESCAPE_NULL_COLUMN) {
            option.setNull();
            int next = reader.read();
            if (isSeparator(next) == false) {
                throw new RecordFormatException(MessageFormat.format(
                        "Missing separator for {0}",
                        option.getClass().getSimpleName()));
            }
            setLastSeparator(next);
            fillLookAhead();
            return true;
        } else {
            throw new RecordFormatException(MessageFormat.format(
                    "Cannot recognize \"{1}\" for {0}",
                    option.getClass().getSimpleName(),
                    new StringBuilder().append(ESCAPE_CHAR).append(ESCAPE_NULL_COLUMN)));
        }
    }

    @Override
    public void endRecord() throws RecordFormatException, IOException {
        if (lastSeparator != RECORD_SEPARATOR) {
            throw new RecordFormatException("RECORD_SEPARATOR does not appeared");
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
