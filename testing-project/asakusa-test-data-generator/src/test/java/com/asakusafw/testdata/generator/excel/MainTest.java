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
package com.asakusafw.testdata.generator.excel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

/**
 * Test for {@link Main}.
 */
public class MainTest extends ExcelTesterRoot {

    /**
     * simple.
     * @throws Exception if occur
     */
    @Test
    public void simple() throws Exception {
        File output = folder.newFolder("output");
        File source = folder.newFolder("source");
        deploy("simple.dmdl", source);
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, "-output", output.getAbsolutePath());
        Collections.addAll(args, "-source", source.getAbsolutePath());
        Collections.addAll(args, "-format", WorkbookFormat.DATA.name());
        int exit = Main.start(args.toArray(new String[args.size()]));
        assertThat(exit, is(0));

        Workbook book = open(output, "simple");
        assertThat(cell(book.getSheetAt(0), 0, 0), is("value"));
    }

    /**
     * using xlsx.
     * @throws Exception if occur
     */
    @Test
    public void xssf() throws Exception {
        File output = folder.newFolder("output");
        File source = folder.newFolder("source");
        deploy("simple.dmdl", source);
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, "-output", output.getAbsolutePath());
        Collections.addAll(args, "-source", source.getAbsolutePath());
        Collections.addAll(args, "-format", WorkbookFormat.DATAX.name());
        int exit = Main.start(args.toArray(new String[args.size()]));
        assertThat(exit, is(0));

        Workbook book = open(output, "simple");
        assertThat(cell(book.getSheetAt(0), 0, 0), is("value"));
    }

    /**
     * less options.
     */
    @Test
    public void less() {
        List<String> args = new ArrayList<String>();
        Collections.addAll(args);
        int exit = Main.start(args.toArray(new String[args.size()]));
        assertThat(exit, not(0));
    }

    /**
     * invalid DMDL.
     * @throws Exception if occur
     */
    @Test
    public void invalid_dmdl() throws Exception {
        File output = folder.newFolder("output");
        File source = folder.newFolder("source");
        deploy("invalid.dmdl", source);
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, "-output", output.getAbsolutePath());
        Collections.addAll(args, "-source", source.getAbsolutePath());
        Collections.addAll(args, "-format", WorkbookFormat.DATA.name());
        int exit = Main.start(args.toArray(new String[args.size()]));
        assertThat(exit, is(1));
    }

    /**
     * invalid output.
     * @throws Exception if occur
     */
    @Test
    public void invalid_output() throws Exception {
        File output = folder.newFile("output");
        File source = folder.newFolder("source");
        deploy("simple.dmdl", source);
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, "-output", output.getAbsolutePath());
        Collections.addAll(args, "-source", source.getAbsolutePath());
        Collections.addAll(args, "-format", WorkbookFormat.DATA.name());
        int exit = Main.start(args.toArray(new String[args.size()]));
        assertThat(exit, is(1));
    }

    /**
     * invalid_source.
     * @throws Exception if occur
     */
    @Test
    public void invalid_source() throws Exception {
        File output = folder.newFolder("output");
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, "-output", output.getAbsolutePath());
        Collections.addAll(args, "-source", "INVALID_SOURCE_PATH");
        Collections.addAll(args, "-format", WorkbookFormat.DATA.name());
        int exit = Main.start(args.toArray(new String[args.size()]));
        assertThat(exit, is(1));
    }

    /**
     * invalid format.
     * @throws Exception if occur
     */
    @Test
    public void invalid_format() throws Exception {
        File output = folder.newFolder("output");
        File source = folder.newFolder("source");
        deploy("simple.dmdl", source);
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, "-output", output.getAbsolutePath());
        Collections.addAll(args, "-source", source.getAbsolutePath());
        Collections.addAll(args, "-format", "INVALID_FORMAT");
        int exit = Main.start(args.toArray(new String[args.size()]));
        assertThat(exit, is(1));
    }

    private void deploy(String name, File target) throws IOException {
        InputStream in = getClass().getResourceAsStream(name);
        assertThat(name, in, not(nullValue()));
        try {
            OutputStream out = new FileOutputStream(new File(target, name));
            try {
                byte[] buf = new byte[1024];
                while (true) {
                    int read = in.read(buf);
                    if (read < 0) {
                        break;
                    }
                    out.write(buf, 0, read);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private Workbook open(File dir, String prefix) throws IOException {
        File file = null;
        for (File f : dir.listFiles()) {
            if (f.isFile() && f.getName().startsWith(prefix)) {
                file = f;
                break;
            }
        }
        assertThat(prefix, file, not(nullValue()));
        return openWorkbook(file);
    }
}
