/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;


/**
 * Reads set of records and set each cell into {@link ValueOption}.
<pre><code>
SomeModel model = new SomeModel();
try (RecordParser parser = ...) {
    while (parser.next()) {
        parser.fill(model.getHogeOption());
        parser.fill(model.getFooOption());
        parser.fill(model.getBarOption());
        parser.endRecord();
        performSomeAction(model);
    }
}
</code></pre>
 * Each method in this interface may raise {@link NullPointerException} if parameters were {@code null}.
 * @since 0.1.0
 * @version 0.2.4
 */
public interface RecordParser extends Closeable {

    /**
     * Prepare for reading the next record.
     * If the current record has any more remaining cells, this will raise an {@link RecordFormatException}.
     * @return {@code true} if the next record exists, or otherwise {@code false}
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the head of the next record
     */
    boolean next() throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(BooleanOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(ByteOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(ShortOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(IntOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(LongOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(FloatOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(DoubleOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(DecimalOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(StringOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * If either year, month, or day field was {@code 0}, this set {@code null} to the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(DateOption option) throws RecordFormatException, IOException;

    /**
     * Reads the next cell and set the value into the target object.
     * If either year, month, or day field was {@code 0}, this set {@code null} to the target object.
     * @param option the target object
     * @throws RecordFormatException if the record format is something wrong
     * @throws IOException if error occurred while reading the next cell
     */
    void fill(DateTimeOption option) throws RecordFormatException, IOException;

    /**
     * Finalizes current record.
     * @throws RecordFormatException if current record is wrong
     * @throws IOException if failed to finalize this record
     */
    void endRecord() throws RecordFormatException, IOException;
}