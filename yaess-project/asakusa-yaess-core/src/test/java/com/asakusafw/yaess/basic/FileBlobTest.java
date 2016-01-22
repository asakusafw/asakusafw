/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.asakusafw.yaess.core.Blob;
import com.asakusafw.yaess.core.util.TemporaryFiles;

/**
 * Test for {@link FileBlob}.
 */
public class FileBlobTest {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        try (TemporaryFiles temporary = new TemporaryFiles()) {
            File file = temporary.create("asakusa", ".bin", in("A"));
            FileBlob b = new FileBlob(file);
            assertThat(b.getFile().getCanonicalFile(), is(file.getCanonicalFile()));
            assertThat(get(b), is("A"));
            assertThat(b.getSize(), is(1L));
            assertThat(b.getFileExtension(), is("bin"));
        }
    }

    /**
     * w/o file extension.
     * @throws Exception if failed
     */
    @Test
    public void no_extension() throws Exception {
        try (TemporaryFiles temporary = new TemporaryFiles()) {
            File file = temporary.create("asakusa", "-noext");
            FileBlob b = new FileBlob(file);
            assertThat(b.getFile().getCanonicalFile(), is(file.getCanonicalFile()));
            assertThat(b.getFileExtension(), is(Blob.DEFAULT_FILE_EXTENSION));
        }
    }

    private ByteArrayInputStream in(String contents) {
        return new ByteArrayInputStream(contents.getBytes(CHARSET));
    }

    private String get(FileBlob b) throws IOException {
        byte[] buf = new byte[256];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = b.open()) {
            while (true) {
                int read = in.read(buf);
                if (read < 0) {
                    break;
                }
                out.write(buf, 0, read);
            }
        }
        return new String(out.toByteArray(), CHARSET);
    }
}
