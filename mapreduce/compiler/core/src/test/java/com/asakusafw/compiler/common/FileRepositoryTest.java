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
import java.util.zip.ZipInputStream;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.batch.ResourceRepository.Cursor;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.runtime.configuration.FrameworkDeployer;

/**
 * Test for {@link FileRepository}.
 */
public class FileRepositoryTest {

    /**
     * A temporary area for testing.
     */
    @Rule
    public FrameworkDeployer framework = new FrameworkDeployer(false);

    /**
     * w/ single file.
     * @throws Exception if exception was occurred
     */
    @Test
    public void single() throws Exception {
        FileRepository repository = new FileRepository(open("single.zip"));
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
        FileRepository repository = new FileRepository(open("multiple.zip"));
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
        FileRepository repository = new FileRepository(open("structured.zip"));
        Cursor cur = repository.createCursor();
        Map<String, List<String>> entries = drain(cur);

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("a.txt", Arrays.asList("aaa"));
        expected.put("a/b.txt", Arrays.asList("bbb"));
        expected.put("a/b/c.txt", Arrays.asList("ccc"));

        assertThat(entries, is(expected));
    }

    /**
     * only an empty directory.
     * @throws Exception if exception was occurred
     */
    @Test
    public void empty() throws Exception {
        FileRepository repository = new FileRepository(framework.getWork("empty"));
        Cursor cur = repository.createCursor();
        Map<String, List<String>> entries = drain(cur);

        Map<String, List<String>> expected = new HashMap<>();

        assertThat(entries, is(expected));
    }

    private File open(String name) {
        String path = getClass().getSimpleName() + ".files/" + name;
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(path, input, not(nullValue()));
            try (ZipInputStream zip = new ZipInputStream(input)) {
                File result = new File(framework.getWork("work"), name);
                framework.extract(zip, result);
                return result;
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private Map<String, List<String>> drain(Cursor cur) throws IOException {
        try {
            Map<String, List<String>> entries = new TreeMap<>();
            while (cur.next()) {
                Location location = cur.getLocation();
                try (InputStream input = cur.openResource();
                        Scanner scanner = new Scanner(input, "UTF-8")) {
                    List<String> contents = new ArrayList<>();
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        contents.add(line);
                    }
                    entries.put(location.toPath('/'), contents);
                }
            }
            return entries;
        } finally {
            cur.close();
        }
    }
}
