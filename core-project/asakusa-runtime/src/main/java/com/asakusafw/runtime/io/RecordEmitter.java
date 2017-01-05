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
 * Writes {@link ValueOption} instances as set of records format.
<pre><code>
Iterator&lt;SomeModel&gt; models = ...;
try (RecordEmitter emitter = ...) {
    while (models.hasNext()) {
        SomeModel model = models.next();
        emitter.emit(model.getHogeOption());
        emitter.emit(model.getFooOption());
        emitter.emit(model.getBarOption());
        emitter.endRecord();
    }
}
</code></pre>
 * Each method in this interface may raise {@link NullPointerException} if parameters were {@code null}.
 */
public interface RecordEmitter extends Flushable, Closeable {

    /**
     * Finalizes the current record and prepares writing the next record.
     * @throws IOException if error occurred while writing the value
     */
    void endRecord() throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(BooleanOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(ByteOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(ShortOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(IntOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(LongOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(FloatOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(DoubleOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(DecimalOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(StringOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(DateOption option) throws IOException;

    /**
     * Writes the value as the next cell on in current record.
     * @param option the target value to be written
     * @throws IOException if error occurred while writing the value
     */
    void emit(DateTimeOption option) throws IOException;
}