/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.text.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import com.asakusafw.runtime.io.text.FieldReader;
import com.asakusafw.runtime.io.text.TextInput;
import com.asakusafw.runtime.io.text.TextOutput;
import com.asakusafw.runtime.io.text.mock.MockFieldAdapter;
import com.asakusafw.runtime.io.text.mock.MockFieldReader;
import com.asakusafw.runtime.io.text.mock.MockFieldWriter;

final class TestUtil {

    private TestUtil() {
        return;
    }

    static UnaryOperator<String[]> self() {
        return UnaryOperator.identity();
    }

    static FieldDefinition.Builder<String[]> field(String name, int index) {
        return new FieldDefinition.Builder<>(name, MockFieldAdapter.supplier(index));
    }

    static FieldDefinition.Builder<String[]> field(int index) {
        return field(header(index), index);
    }

    static FieldDefinition.Builder<String[]> malformField(int index) {
        return new FieldDefinition.Builder<>(header(index), () -> new MockFieldAdapter(index) {
            @Override
            public void parse(CharSequence contents, String[] property) {
                throw new MalformedFieldException();
            }
        });
    }

    private static String header(int index) {
        return "p" + index;
    }

    static String[][] collect(RecordDefinition<String[]> def, String[][] fields) throws IOException {
        try (FieldReader reader = new MockFieldReader(fields);
                TextInput<String[]> input = def.newInput(reader, "testing")) {
            return doCollect(def, input);
        }
    }

    static String[][] collect(
            RecordDefinition<String[]> def, Collection<InputOption> options, String[][] fields) throws IOException {
        try (FieldReader reader = new MockFieldReader(fields);
                TextInput<String[]> input = def.newInput(reader, "testing", options)) {
            return doCollect(def, input);
        }
    }

    private static String[][] doCollect(RecordDefinition<String[]> def, TextInput<String[]> input) throws IOException {
        List<String[]> results = new ArrayList<>();
        String[] object = new String[def.getNumberOfFields()];
        while (input.readTo(object)) {
            results.add(object.clone());
        }
        return results.toArray(new String[results.size()][]);
    }

    static String[][] emit(RecordDefinition<String[]> def, String[][] fields) throws IOException {
        try (MockFieldWriter writer = new MockFieldWriter();
                TextOutput<String[]> output = def.newOutput(writer, "testing")) {
            for (String[] row : fields) {
                output.write(row);
            }
            return writer.get();
        }
    }
}
