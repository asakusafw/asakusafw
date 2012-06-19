/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceRepository.Cursor;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.util.Emitter;

/**
 * Test for {@link Main}.
 */
public class MainTest {

    /**
     * Temporary folder for the test cases.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * minimum arguments.
     * @throws Exception if test was failed
     */
    @Test
    public void minimum() throws Exception {
        File output = folder.newFolder("output");
        File source = folder.newFile("example.dmdl");

        List<String> arguments = Lists.create();

        Collections.addAll(arguments, "-output", output.getPath());
        Collections.addAll(arguments, "-source", source.getPath());
        Collections.addAll(arguments, "-package", "com.example.testing");
        Configuration config = Main.configure(arguments.toArray(new String[arguments.size()]));

        assertThat(config.getSource(), is(source("example.dmdl")));
        assertThat(config.getOutput(), is(target(output)));
        assertThat(config.getBasePackage().toNameString(), is("com.example.testing"));
    }

    /**
     * source directory.
     * @throws Exception if test was failed
     */
    @Test
    public void source_directory() throws Exception {
        File output = folder.newFolder("output");
        File source = folder.newFolder("dmdl");
        new File(source, "a.dmdl").createNewFile();
        new File(source, "b.dmdl").createNewFile();

        List<String> arguments = Lists.create();

        Collections.addAll(arguments, "-output", output.getPath());
        Collections.addAll(arguments, "-source", source.getPath());
        Collections.addAll(arguments, "-package", "com.example.testing");
        Configuration config = Main.configure(arguments.toArray(new String[arguments.size()]));

        assertThat(config.getSource(), is(source("a.dmdl", "b.dmdl")));
    }

    /**
     * source directory.
     * @throws Exception if test was failed
     */
    @Test
    public void multi_source() throws Exception {
        File output = folder.newFolder("output");
        File source1 = folder.newFolder("dmdl");
        new File(source1, "a.dmdl").createNewFile();
        new File(source1, "b.dmdl").createNewFile();
        File source2 = folder.newFile("file.dmdl");

        List<String> arguments = Lists.create();

        Collections.addAll(arguments, "-output", output.getPath());
        Collections.addAll(arguments, "-source",
                source1.getPath() + File.pathSeparatorChar + source2.getPath());
        Collections.addAll(arguments, "-package", "com.example.testing");
        Configuration config = Main.configure(arguments.toArray(new String[arguments.size()]));

        assertThat(config.getSource(), is(source("a.dmdl", "b.dmdl", "file.dmdl")));
    }

    private Matcher<DmdlSourceRepository> source(String... fileNames) {
        final Set<String> files = new TreeSet<String>();
        Collections.addAll(files, fileNames);
        return new BaseMatcher<DmdlSourceRepository>() {
            @Override
            public boolean matches(Object target) {
                if ((target instanceof DmdlSourceRepository) == false) {
                    return false;
                }
                Set<String> saw = new TreeSet<String>();
                try {
                    DmdlSourceRepository repo = (DmdlSourceRepository) target;
                    Cursor cursor = repo.createCursor();
                    try {
                        while (cursor.next()) {
                            String path = cursor.getIdentifier().getRawPath();
                            if (path.endsWith("/")) {
                                path = path.substring(0, path.length() - 1);
                            }
                            String file = path.substring(path.lastIndexOf('/') + 1);
                            saw.add(file);
                        }
                    } finally {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return saw.equals(files);
            }

            @Override
            public void describeTo(Description desc) {
                desc.appendText(files.toString());
            }
        };
    }

    private Matcher<Emitter> target(final File output) {
        return new BaseMatcher<Emitter>() {
            @Override
            public boolean matches(Object target) {
                if ((target instanceof Emitter) == false) {
                    return false;
                }
                Emitter emitter = (Emitter) target;
                try {
                    PrintWriter writer = emitter.openFor(null, "__TESTING__");
                    try {
                        writer.println("testing!");
                    } finally {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return new File(output, "__TESTING__").isFile();
            }

            @Override
            public void describeTo(Description desc) {
                desc.appendText(output.getPath());
            }
        };
    }
}
