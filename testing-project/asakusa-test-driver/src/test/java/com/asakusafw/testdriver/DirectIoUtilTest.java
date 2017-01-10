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
package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.model.DefaultDataModelDefinition;
import com.asakusafw.testdriver.testing.dsl.SimpleFileFormat;
import com.asakusafw.testdriver.testing.dsl.SimpleStreamFormat;
import com.asakusafw.testdriver.testing.model.Simple;

/**
 * Test for {@code DirectIoUtil}.
 */
public class DirectIoUtilTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    static final DataModelDefinition<Simple> DEF = new DefaultDataModelDefinition<>(Simple.class);

    /**
     * stream - from file.
     * @throws Exception if failed
     */
    @Test
    public void stream_file() throws Exception {
        File f = asFile("directio/simple.txt");
        List<String> results = extract(DirectIoUtil.load(new Configuration(), DEF, SimpleStreamFormat.class, f));
        assertThat(results, containsInAnyOrder("Hello, world!"));
    }

    /**
     * file - from file.
     * @throws Exception if failed
     */
    @Test
    public void file_file() throws Exception {
        File f = asFile("directio/simple.txt");
        List<String> results = extract(DirectIoUtil.load(new Configuration(), DEF, SimpleFileFormat.class, f));
        assertThat(results, containsInAnyOrder("Hello, world!"));
    }

    /**
     * stream - from file URL.
     * @throws Exception if failed
     */
    @Test
    public void stream_fileUrl() throws Exception {
        URL f = asFileUrl("directio/simple.txt");
        List<String> results = extract(DirectIoUtil.load(new Configuration(), DEF, SimpleStreamFormat.class, f));
        assertThat(results, containsInAnyOrder("Hello, world!"));
    }

    /**
     * file - from file URL.
     * @throws Exception if failed
     */
    @Test
    public void file_fileUrl() throws Exception {
        URL f = asFileUrl("directio/simple.txt");
        List<String> results = extract(DirectIoUtil.load(new Configuration(), DEF, SimpleFileFormat.class, f));
        assertThat(results, containsInAnyOrder("Hello, world!"));
    }

    /**
     * stream - from Jar entry.
     * @throws Exception if failed
     */
    @Test
    public void stream_jarUrl() throws Exception {
        URL f = asJarUrl("directio/simple.txt");
        List<String> results = extract(DirectIoUtil.load(new Configuration(), DEF, SimpleStreamFormat.class, f));
        assertThat(results, containsInAnyOrder("Hello, world!"));
    }

    /**
     * file - from Jar entry.
     * @throws Exception if failed
     */
    @Test
    public void file_jarUrl() throws Exception {
        URL f = asJarUrl("directio/simple.txt");
        List<String> results = extract(DirectIoUtil.load(new Configuration(), DEF, SimpleFileFormat.class, f));
        assertThat(results, containsInAnyOrder("Hello, world!"));
    }

    /**
     * dump.
     * @throws Exception if failed
     */
    @Test
    public void dump() throws Exception {
        File dest = new File(temporary.getRoot(), "output/file.txt");
        try (DataModelSink sink = DirectIoUtil.dump(new Configuration(), DEF, SimpleStreamFormat.class, dest)
                .createSink(DEF, new TestContext.Empty())) {
            Simple buf = new Simple();

            buf.setValueAsString("A");
            sink.put(DEF.toReflection(buf));

            buf.setValueAsString("B");
            sink.put(DEF.toReflection(buf));

            buf.setValueAsString("C");
            sink.put(DEF.toReflection(buf));
        }
        assertThat(dest.isFile(), is(true));
        List<String> results = extract(DirectIoUtil.load(new Configuration(), DEF, SimpleFileFormat.class, dest));
        assertThat(results, containsInAnyOrder("A", "B", "C"));
    }

    private File asFile(String name) {
        URL url = getClass().getResource(name);
        assertThat(url, is(notNullValue()));
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            Assume.assumeNoException(e);
            throw new AssertionError(e);
        }
    }

    private URL asFileUrl(String name) {
        File f = asFile(name);
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            Assume.assumeNoException(e);
            throw new AssertionError(e);
        }
    }

    private URL asJarUrl(String name) throws IOException {
        URL url = getClass().getResource(name);
        assertThat(url, is(notNullValue()));
        File target = temporary.newFile();
        try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(target));
                InputStream input = url.openStream()) {
            output.putNextEntry(new ZipEntry("root"));
            IOUtils.copy(input, output);
        }
        try {
            String normalized = target.toURI().toASCIIString();
            URL nested = new URL("jar", null, normalized + "!/root");
            nested.openStream().close();
            return nested;
        } catch (Exception e) {
            Assume.assumeNoException(e);
            throw new AssertionError(e);
        }
    }

    private static List<String> extract(DataModelSourceFactory input) throws IOException {
        List<String> results = new ArrayList<>();
        try (DataModelSource source = input.createSource(DEF, new TestContext.Empty())) {
            while (true) {
                DataModelReflection ref = source.next();
                if (ref == null) {
                    break;
                }
                results.add(DEF.toObject(ref).getValueAsString());
            }
        }
        return results;
    }
}
