/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.text.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.runtime.io.text.FieldWriter;
import com.asakusafw.runtime.io.text.UnmappableOutput;
import com.asakusafw.runtime.io.text.UnmappableOutputException;
import com.asakusafw.runtime.io.text.driver.FieldOutput;

/**
 * Mock {@link FieldWriter}.
 */
public class MockFieldWriter implements FieldWriter {

    private final List<String[]> rows = new ArrayList<>();

    private final List<String> cols = new ArrayList<>();

    private final List<UnmappableOutput> unmappables = new ArrayList<>();

    @Override
    public void putField(FieldOutput output) throws IOException {
        CharSequence contents = output.get();
        if (contents == null) {
            cols.add(null);
        } else {
            String string = contents.toString();
            try {
                UnmappableOutput.ErrorCode code = UnmappableOutput.ErrorCode.valueOf(string);
                unmappables.add(new UnmappableOutput(code, cols.size(), "T"));
            } catch (RuntimeException e) {
                // ok.
            }
            cols.add(string);
        }
    }

    @Override
    public void putEndOfRecord() {
        rows.add(cols.toArray(new String[cols.size()]));
        cols.clear();
        if (unmappables.isEmpty() == false) {
            UnmappableOutputException e = new UnmappableOutputException(unmappables);
            unmappables.clear();
            throw e;
        }
    }

    /**
     * Returns the written contents.
     * @return the written contents
     */
    public String[][] get() {
        return rows.stream().toArray(String[][]::new);
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
