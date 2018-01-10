/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.yaess.iterative;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.iterative.common.IterativeExtensions;
import com.asakusafw.iterative.common.ParameterSet;
import com.asakusafw.iterative.common.ParameterTable;
import com.asakusafw.yaess.basic.BasicExtension;
import com.asakusafw.yaess.core.Extension;
import com.asakusafw.yaess.core.ExtensionHandler;

/**
 * Test for {@link ParameterTableHandler}.
 */
public class ParameterTableHandlerTest {

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File json = json(new String[] {
                "{}",
        });
        ParameterTable table = ParameterTableHandler.parse(json);
        assertThat(table.getRowCount(), is(1));
        List<ParameterSet> rows = table.getRows();
        assertThat(rows.get(0).toMap(), is(map()));
    }

    /**
     * w/ multiple columns.
     * @throws Exception if failed
     */
    @Test
    public void columns() throws Exception {
        File json = json(new String[] {
                "{ a: 'A', b: 'B', c: 'C' }",
        });
        ParameterTable table = ParameterTableHandler.parse(json);
        assertThat(table.getRowCount(), is(1));
        List<ParameterSet> rows = table.getRows();
        assertThat(rows.get(0).toMap(), is(map("a", "A", "b", "B", "c", "C")));
    }

    /**
     * w/ multiple rows.
     * @throws Exception if failed
     */
    @Test
    public void rows() throws Exception {
        File json = json(new String[] {
                "{ a: 'A' }",
                "{ b: 'B' }",
                "{ c: 'C' }",
        });
        ParameterTable table = ParameterTableHandler.parse(json);
        assertThat(table.getRowCount(), is(3));
        List<ParameterSet> rows = table.getRows();
        assertThat(rows.get(0).toMap(), is(map("a", "A")));
        assertThat(rows.get(1).toMap(), is(map("b", "B")));
        assertThat(rows.get(2).toMap(), is(map("c", "C")));
    }

    /**
     * handle.
     * @throws Exception if failed
     */
    @Test
    public void handle() throws Exception {
        File json = json(new String[] {
                "{ a: 'A', b: 'B', c: 'C' }",
        });
        File f;
        try (Extension ext = new ParameterTableHandler().handle(ParameterTableHandler.TAG, json.getAbsolutePath())) {
            assertThat(ext, is(notNullValue()));
            f = ((BasicExtension) ext).getData().getFile();
            try (InputStream in = ext.getData().open()) {
                ParameterTable table = IterativeExtensions.load(in);
                assertThat(table.getRowCount(), is(1));
                List<ParameterSet> rows = table.getRows();
                assertThat(rows.get(0).toMap(), is(map("a", "A", "b", "B", "c", "C")));
            }
        }
        assertThat("delete on exit", f.exists(), is(false));
    }

    /**
     * handle.
     * @throws Exception if failed
     */
    @Test
    public void handle_other() throws Exception {
        File json = json(new String[] {
                "{ a: 'A', b: 'B', c: 'C' }",
        });
        try (Extension ext = new ParameterTableHandler().handle("UNKNOWN", json.getAbsolutePath())) {
            assertThat(ext, is(nullValue()));
        }
    }

    /**
     * check if {@link ParameterTableHandler} is registered.
     */
    @Test
    public void spi() {
        ServiceLoader<ExtensionHandler> services = ServiceLoader.load(ExtensionHandler.class);
        for (ExtensionHandler h : services) {
            if (h instanceof ParameterTableHandler) {
                return;
            }
        }
        fail("not registered");
    }

    private Map<String, String> map(String... pairs) {
        assertThat(pairs.length % 2, is(0));
        Map<String, String> results = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            results.put(pairs[i + 0], pairs[i + 1]);
        }
        return results;
    }

    private File json(String... lines) throws IOException {
        File file = temporary.newFile("testing.json");
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (String s : lines) {
                writer.println(s);
            }
        }
        return file;
    }
}
