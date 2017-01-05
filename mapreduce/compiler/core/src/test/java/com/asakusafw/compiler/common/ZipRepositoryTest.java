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
package com.asakusafw.compiler.common;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.batch.ResourceRepository.Cursor;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.runtime.configuration.FrameworkDeployer;

/**
 * Test for {@link ZipRepository}.
 */
public class ZipRepositoryTest {

    /**
     * temporary folder.
     */
    @Rule
    public FrameworkDeployer framework = new FrameworkDeployer(false);

    /**
     * w/ single file.
     * @throws Exception if exception was occurred
     */
    @Test
    public void single() throws Exception {
        ZipRepository repository = new ZipRepository(open("single.zip"));
        Cursor cur = repository.createCursor();
        Map<String, List<String>> entries = drain(cur);

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("hello.txt", Arrays.asList("Hello, world!"));

        assertThat(entries, is(expected));
    }

    /**
     * w/ multiple files.
     * @throws Exception if exception was occurred
     */
    @Test
    public void multiple() throws Exception {
        ZipRepository repository = new ZipRepository(open("multiple.zip"));
        Cursor cur = repository.createCursor();
        Map<String, List<String>> entries = drain(cur);

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("a.txt", Arrays.asList("aaa"));
        expected.put("b.txt", Arrays.asList("bbb"));
        expected.put("c.txt", Arrays.asList("ccc"));

        assertThat(entries, is(expected));
    }

    /**
     * w/ sub-directories.
     * @throws Exception if exception was occurred
     */
    @Test
    public void structured() throws Exception {
        ZipRepository repository = new ZipRepository(open("structured.zip"));
        Cursor cur = repository.createCursor();
        Map<String, List<String>> entries = drain(cur);

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("a.txt", Arrays.asList("aaa"));
        expected.put("a/b.txt", Arrays.asList("bbb"));
        expected.put("a/b/c.txt", Arrays.asList("ccc"));

        assertThat(entries, is(expected));
    }

    /**
     * not a zip file.
     * @throws Exception if exception was occurred
     */
    @Test(expected = IOException.class)
    public void notarchive() throws Exception {
        ZipRepository repository = new ZipRepository(open("notarchive.zip"));
        Cursor cur = repository.createCursor();
        drain(cur);
    }

    private File open(String name) {
        String path = getClass().getSimpleName() + ".files/" + name;
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(path, input, not(nullValue()));
            File file = new File(framework.getWork("temp"), name);
            framework.dump(input, file);
            return file;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private Map<String, List<String>> drain(Cursor cur) throws IOException {
        try {
            Map<String, List<String>> entries = new TreeMap<>();
            while (cur.next()) {
                try (InputStream input = cur.openResource();
                        Scanner scanner = new Scanner(input, "UTF-8");) {
                    List<String> contents = new ArrayList<>();
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        contents.add(line);
                    }
                    Location location = cur.getLocation();
                    entries.put(location.toPath('/'), contents);
                }
            }
            return entries;
        } finally {
            cur.close();
        }
    }
}
