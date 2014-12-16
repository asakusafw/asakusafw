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

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.text.MessageFormat;

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
 * {@link ValueOption}の内容をTSV形式で出力する。
 * <p>
 * 次のように利用する。
 * </p>
<pre><code>
Writer writer = ...;
Iterator&lt;SomeModel&gt; models = ...;
try {
    TsvEmitter emitter = new TsvEmitter(writer);
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
 * 特に指定がない限り、このクラスのメソッドの引数に{@code null}を指定した場合には
 * {@link NullPointerException}がスローされる。
 * </p>
 */
public class TsvEmitter implements RecordEmitter {

    private static final Charset TEXT_ENCODE = Charset.forName("UTF-8"); //$NON-NLS-1$

    private static final int BUFFER_SIZE = 2048;

    private final Writer writer;

    private final CharsetDecoder decoder;

    private final StringBuilder lineBuffer;

    private final char[] writeBuffer;

    private boolean headOfLine;

    private final CharBuffer decodeBuffer;

    // MEMO: 全体的に throws IOException は残しておく
    // これは拡張時に互換性を保つため。

    /**
     * インスタンスを生成する。
     * @param writer 出力先のライター
     * @throws IOException 初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public TsvEmitter(Writer writer) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        this.writer = writer;
        this.decoder = TEXT_ENCODE.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        this.lineBuffer = new StringBuilder();
        this.writeBuffer = new char[BUFFER_SIZE];
        this.headOfLine = true;
        this.decodeBuffer = CharBuffer.wrap(writeBuffer);
    }

    @Override
    public void endRecord() throws IOException {
        flushLineBuffer();
        writer.write(RECORD_SEPARATOR);
        headOfLine = true;
    }

    private void flushLineBuffer() throws IOException {
        int rest = lineBuffer.length();
        int cursor = 0;
        while (rest > 0) {
            int chunkSize = Math.min(rest, writeBuffer.length);
            lineBuffer.getChars(cursor, cursor + chunkSize, writeBuffer, 0);
            writer.write(writeBuffer, 0, chunkSize);
            rest -= chunkSize;
            cursor += chunkSize;
        }
        lineBuffer.setLength(0);
    }

    private void startCell() {
        if (headOfLine == false) {
            lineBuffer.append(CELL_SEPARATOR);
        }
        headOfLine = false;
    }

    @Override
    public void emit(BooleanOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get() ? BOOLEAN_TRUE : BOOLEAN_FALSE);
    }

    @Override
    public void emit(ByteOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get());
    }

    @Override
    public void emit(ShortOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get());
    }

    @Override
    public void emit(IntOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get());
    }

    @Override
    public void emit(LongOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get());
    }

    @Override
    public void emit(FloatOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get());
    }

    @Override
    public void emit(DoubleOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get());
    }

    @Override
    public void emit(DecimalOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        lineBuffer.append(option.get());
    }

    @Override
    public void emit(StringOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        Text text = option.get();
        if (text.getLength() == 0) {
            return;
        }

        byte[] bytes = text.getBytes();
        ByteBuffer source = ByteBuffer.wrap(bytes, 0, text.getLength());
        decoder.reset();
        decodeBuffer.clear();
        while (true) {
            CoderResult result = decoder.decode(source, decodeBuffer, true);
            if (result.isError()) {
                throw new RecordFormatException(MessageFormat.format(
                        "Cannot process a character string (\"{0}\")",
                        result));
            }
            if (result.isUnderflow()) {
                consumeDecoded();
                break;
            }
            if (result.isOverflow()) {
                consumeDecoded();
            }
        }
        while (true) {
            CoderResult result = decoder.flush(decodeBuffer);
            if (result.isError()) {
                throw new RecordFormatException(MessageFormat.format(
                        "Cannot process a character string (\"{0}\")",
                        result));
            }
            if (result.isUnderflow()) {
                consumeDecoded();
                break;
            }
            if (result.isOverflow()) {
                consumeDecoded();
            }
        }
    }

    private void consumeDecoded() {
        decodeBuffer.flip();
        if (decodeBuffer.hasRemaining()) {
            char[] array = decodeBuffer.array();
            for (int i = decodeBuffer.position(), n = decodeBuffer.limit(); i < n; i++) {
                char c = array[i];
                if (c == '\t') {
                    lineBuffer.append(ESCAPE_CHAR);
                    lineBuffer.append(ESCAPE_HT);
                } else if (c == '\n') {
                    lineBuffer.append(ESCAPE_CHAR);
                    lineBuffer.append(ESCAPE_LF);
                } else if (c == '\\') {
                    lineBuffer.append(ESCAPE_CHAR);
                    lineBuffer.append(ESCAPE_CHAR);
                } else {
                    lineBuffer.append(c);
                }
            }
        }
        decodeBuffer.clear();
    }

    @Override
    public void emit(DateOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        int days = option.get().getElapsedDays();
        emitDate(days);
    }

    @Override
    public void emit(DateTimeOption option) throws IOException {
        startCell();
        if (emitNull(option)) {
            return;
        }
        long seconds = option.get().getElapsedSeconds();
        int days = DateUtil.getDayFromSeconds(seconds);
        emitDate(days);

        lineBuffer.append(DATE_TIME_SEPARATOR);

        int sec = DateUtil.getSecondOfDay(seconds);
        emitTime(sec);
    }

    private void emitDate(int days) {
        int year = DateUtil.getYearFromDay(days);
        int daysInYear = days - DateUtil.getDayFromYear(year);
        boolean leap = DateUtil.isLeap(year);
        int month = DateUtil.getMonthOfYear(daysInYear, leap);
        int day = DateUtil.getDayOfMonth(daysInYear, leap);

        fill('0', YEAR_FIELD_LENGTH, year);
        lineBuffer.append(DATE_FIELD_SEPARATOR);
        fill('0', MONTH_FIELD_LENGTH, month);
        lineBuffer.append(DATE_FIELD_SEPARATOR);
        fill('0', DATE_FIELD_LENGTH, day);
    }

    private void emitTime(int sec) {
        fill('0', HOUR_FIELD_LENGTH, sec / (60 * 60));
        lineBuffer.append(TIME_FIELD_SEPARATOR);
        fill('0', MINUTE_FIELD_LENGTH, sec / 60 % 60);
        lineBuffer.append(TIME_FIELD_SEPARATOR);
        fill('0', SECOND_FIELD_LENGTH, sec % 60);
    }

    private void fill(char filler, int columns, int value) {
        for (int i = 0, n = countToFill(columns, value); i < n; i++) {
            lineBuffer.append(filler);
        }
        lineBuffer.append(value);
    }

    private int countToFill(int columns, int value) {
        if (value < 0) {
            return 0;
        }
        for (int count = columns - 1, figure = 10; count >= 0; count--, figure *= 10) {
            if (value < figure) {
                return count;
            }
        }
        return 0;
    }

    private boolean emitNull(ValueOption<?> option) {
        if (option.isNull()) {
            lineBuffer.append(ESCAPE_CHAR);
            lineBuffer.append(ESCAPE_NULL_COLUMN);
            return true;
        }
        return false;
    }

    @Override
    public void flush() throws IOException {
        flushLineBuffer();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        if (headOfLine == false) {
            endRecord();
        }
        writer.close();
    }
}
