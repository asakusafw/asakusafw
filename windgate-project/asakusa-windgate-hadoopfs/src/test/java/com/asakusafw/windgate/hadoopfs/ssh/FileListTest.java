/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.windgate.hadoopfs.ssh;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link FileList}.
 */
public class FileListTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File file = folder.newFile("testing.filelist");
        FileOutputStream output = new FileOutputStream(file);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            write(writer, "example.txt", "Hello, world!");
            writer.close();
        } finally {
            output.close();
        }

        FileInputStream input = new FileInputStream(file);
        try {
            FileList.Reader reader = FileList.createReader(input);
            read(reader, "example.txt", "Hello, world!");
            assertThat(reader.next(), is(false));
            reader.close();
        } finally {
            input.close();
        }
    }

    /**
     * empty files.
     * @throws Exception if failed
     */
    @Test
    public void empty() throws Exception {
        File file = folder.newFile("testing.filelist");
        FileOutputStream output = new FileOutputStream(file);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            writer.close();
        } finally {
            output.close();
        }

        FileInputStream input = new FileInputStream(file);
        try {
            FileList.Reader reader = FileList.createReader(input);
            assertThat(reader.next(), is(false));
            reader.close();
        } finally {
            input.close();
        }
    }

    /**
     * write and read multiple files.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        File file = folder.newFile("testing.filelist");
        FileOutputStream output = new FileOutputStream(file);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            write(writer, "example1.txt", "Hello1, world!");
            write(writer, "example2.txt", "Hello2, world!");
            write(writer, "example3.txt", "Hello3, world!");
            writer.close();
        } finally {
            output.close();
        }

        FileInputStream input = new FileInputStream(file);
        try {
            FileList.Reader reader = FileList.createReader(input);
            read(reader, "example1.txt", "Hello1, world!");
            read(reader, "example2.txt", "Hello2, world!");
            read(reader, "example3.txt", "Hello3, world!");
            assertThat(reader.next(), is(false));
            reader.close();
        } finally {
            input.close();
        }
    }

    /**
     * file list is not closed.
     * @throws Exception if failed
     */
    @Test
    public void unexpected_eof() throws Exception {
        File file = folder.newFile("testing.filelist");
        FileOutputStream output = new FileOutputStream(file);
        try {
            FileList.Writer writer = FileList.createWriter(output);
            write(writer, "example1.txt", "Hello1, world!");
            write(writer, "example2.txt", "Hello2, world!");
            write(writer, "example3.txt", "Hello3, world!");
            // writer.close();
        } finally {
            output.close();
        }

        FileInputStream input = new FileInputStream(file);
        try {
            FileList.Reader reader = FileList.createReader(input);
            read(reader, "example1.txt", "Hello1, world!");
            read(reader, "example2.txt", "Hello2, world!");
            read(reader, "example3.txt", "Hello3, world!");
            reader.next();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            input.close();
        }
    }

    /**
     * unknown format.
     * @throws Exception if failed
     */
    @Test
    public void invalid_stream() throws Exception {
        File file = folder.newFile("testing.filelist");
        FileInputStream input = new FileInputStream(file);
        try {
            FileList.Reader reader = FileList.createReader(input);
            reader.next();
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            input.close();
        }
    }

    private void write(FileList.Writer writer, String path, String content) throws IOException {
        OutputStream f = writer.openNext(FileList.createFileStatus(new Path(path)));
        try {
            f.write(content.getBytes("UTF-8"));
        } finally {
            f.close();
        }
    }

    private void read(FileList.Reader reader, String path, String content) throws IOException {
        assertThat(reader.next(), is(true));
        assertThat(reader.getCurrentFile().getPath().toString(), is(path));
        InputStream f = reader.openContent();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            while (true) {
                int read = f.read(buf);
                if (read < 0) {
                    break;
                }
                baos.write(buf, 0, read);
            }
            String result = new String(baos.toByteArray(), "UTF-8");
            assertThat(path, result, is(content));
        } finally {
            f.close();
        }
    }
}
