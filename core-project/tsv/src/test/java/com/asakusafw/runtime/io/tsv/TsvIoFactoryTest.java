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
package com.asakusafw.runtime.io.tsv;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Test;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.testing.model.MockModel;
import com.asakusafw.runtime.io.tsv.TsvIoFactory;

/**
 * Test for {@link TsvIoFactory}.
 */
public class TsvIoFactoryTest {

    /**
     * simple case for inputs.
     * @throws Exception if failed
     */
    @Test
    public void input() throws Exception {
        TsvIoFactory<MockModel> factory = new TsvIoFactory<>(MockModel.class);
        MockModel object = factory.createModelObject();
        InputStream in = new ByteArrayInputStream(
                "Hello\nWorld\nTSV\nINPUT\n".getBytes("UTF-8"));

        LinkedList<String> expected = new LinkedList<>();
        Collections.addAll(expected, "Hello", "World", "TSV", "INPUT");

        try (ModelInput<MockModel> modelIn = factory.createModelInput(in)) {
            while (modelIn.readTo(object)) {
                assertThat(expected.isEmpty(), is(false));
                object.assertValueIs(expected.removeFirst());
            }
            assertThat(expected.isEmpty(), is(true));
        }
    }

    /**
     * simple case for outputs.
     * @throws Exception if failed
     */
    @SuppressWarnings("deprecation")
    @Test
    public void output() throws Exception {
        TsvIoFactory<MockModel> factory = new TsvIoFactory<>(MockModel.class);
        MockModel object = factory.createModelObject();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ModelOutput<MockModel> modelOut = factory.createModelOutput(out)) {
            object.value.modify("Hello");
            modelOut.write(object);
            object.value.modify("World");
            modelOut.write(object);
            object.value.modify("TSV");
            modelOut.write(object);
            object.value.modify("OUTPUT");
            modelOut.write(object);
        }

        String result = new String(out.toByteArray(), "UTF-8");
        assertThat(result, is("Hello\nWorld\nTSV\nOUTPUT\n"));
    }
}
