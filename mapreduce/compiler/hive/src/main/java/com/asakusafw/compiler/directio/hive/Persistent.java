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
package com.asakusafw.compiler.directio.hive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;

final class Persistent {

    private Persistent() {
        return;
    }

    static <T> void write(Class<T> type, Iterable<? extends T> elements, OutputStream output) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        try (SequenceWriter writer = mapper.writerFor(type).writeValues(output).init(false)) {
            writer.writeAll(elements);
        }
    }

    static <T> List<T> read(Class<T> type, InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<T> results = new ArrayList<>();
            Iterator<T> iterator = mapper.readerFor(type).readValues(input);
            while (iterator.hasNext()) {
                results.add(iterator.next());
            }
            return results;
        } catch (IOException e) {
            throw new IOException("error occurred while parsing JSON list", e);
        }
    }
}
