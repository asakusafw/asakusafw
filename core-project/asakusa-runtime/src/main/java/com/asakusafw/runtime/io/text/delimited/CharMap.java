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
package com.asakusafw.runtime.io.text.delimited;

import java.util.Arrays;
import java.util.List;

final class CharMap {

    static final int ABSENT = -1;

    static final int NULL_CHARACTER = Character.MAX_VALUE + 1;

    static final CharMap EMPTY = new CharMap(Character.MAX_VALUE + 1, new int[0], ABSENT);

    private final int baseIndex;

    private final int[] entries;

    private final int nullKey;

    private CharMap(int baseIndex, int[] entries, int nullKey) {
        this.baseIndex = baseIndex;
        this.entries = entries;
        this.nullKey = nullKey;
    }

    static CharMap forward(EscapeSequence sequence) {
        List<EscapeSequence.Entry> mappings = sequence.getEntries();
        if (mappings.isEmpty()) {
            return EMPTY;
        }
        int baseIndex = mappings.stream().mapToInt(c -> c.from).min().getAsInt();
        int size = mappings.stream().mapToInt(c -> c.from).max().getAsInt() - baseIndex + 1;
        int[] entries = new int[size];
        Arrays.fill(entries, ABSENT);
        int nullChar = ABSENT;
        for (EscapeSequence.Entry entry : mappings) {
            int index = entry.from - baseIndex;
            if (entries[index] != ABSENT) {
                continue;
            }
            if (entry.to == null) {
                nullChar = entry.from;
                entries[index] = NULL_CHARACTER;
            } else {
                entries[index] = entry.to;
            }
        }
        return new CharMap(baseIndex, entries, nullChar);
    }

    static CharMap backward(EscapeSequence sequence) {
        List<EscapeSequence.Entry> mappings = sequence.getEntries();
        if (mappings.stream().noneMatch(c -> c.to != null)) {
            return EMPTY;
        }
        int baseIndex = mappings.stream().filter(c -> c.to != null).mapToInt(c -> c.to).min().getAsInt();
        int size = mappings.stream().filter(c -> c.to != null).mapToInt(c -> c.to).max().getAsInt() - baseIndex + 1;
        int[] entries = new int[size];
        Arrays.fill(entries, ABSENT);
        int nullChar = ABSENT;
        for (EscapeSequence.Entry entry : mappings) {
            if (entry.to == null) {
                if (nullChar != ABSENT) {
                    continue;
                }
                nullChar = entry.from;
            } else {
                int index = entry.to - baseIndex;
                if (entries[index] != ABSENT) {
                    continue;
                }
                entries[index] = entry.from;
            }
        }
        return new CharMap(baseIndex, entries, nullChar);
    }

    public int get(int c) {
        int begin = baseIndex;
        if (c < begin) {
            return ABSENT;
        }
        int index = c - begin;
        int[] es = entries;
        return index < es.length ? es[index] : ABSENT;
    }

    public int getNullKey() {
        return nullKey;
    }
}
