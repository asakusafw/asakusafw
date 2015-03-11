/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.tools.log.summarize;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.utils.io.RecordWriter;

/**
 * An implementation of {@link RecordWriter} into {@link List}.
 */
public class ListWriter implements RecordWriter {

    private final List<List<String>> lines = new ArrayList<List<String>>();

    private final List<String> current = new ArrayList<String>();

    /**
     * Returns the written lines.
     * @return the lines
     */
    public List<List<String>> getLines() {
        return lines;
    }

    @Override
    public void putField(CharSequence value) {
        current.add(String.valueOf(value));
    }

    @Override
    public void putEndOfRecord() {
        lines.add(new ArrayList<String>(current));
        current.clear();
    }

    @Override
    public void flush() {
        return;
    }

    @Override
    public void close() {
        return;
    }
}
