/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.windgate.hadoopfs.jsch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.runtime.core.context.RuntimeContextKeeper;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.hadoopfs.ssh.SshProfile;

/**
 * Test for {@link JschConnection}.
 */
public class JschConnectionTest {

    /**
     * Keeps runtime context.
     */
    @Rule
    public final RuntimeContextKeeper rc = new RuntimeContextKeeper();

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private SshProfile profile;

    private File target;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        InputStream in = getClass().getResourceAsStream("ssh.properties");
        if (in == null) {
            System.err.println("ssh.properties does not exist, skip this class");
            Assume.assumeNotNull(in);
            return;
        }
        try {
            p.load(in);
        } finally {
            in.close();
        }
        target = folder.newFolder("dummy-install");
        putScript(target, SshProfile.COMMAND_GET);
        putScript(target, SshProfile.COMMAND_PUT);
        putScript(target, SshProfile.COMMAND_DELETE);

        p.setProperty("resource.fs.target", target.getAbsolutePath());

        Collection<? extends ResourceProfile> rps = ResourceProfile.loadFrom(
                p,
                ProfileContext.system(getClass().getClassLoader()));
        assertThat(rps.size(), is(1));
        ResourceProfile rp = rps.iterator().next();
        this.profile = SshProfile.convert(new Configuration(), rp);
    }

    private void putScript(File directory, String sourcePath) throws IOException {
        String targetPath = sourcePath;
        putScript(directory, sourcePath, targetPath);
    }

    private void putScript(File directory, String sourcePath, String targetPath) throws IOException {
        InputStream in = getClass().getResourceAsStream(sourcePath);
        assertThat(sourcePath, in, is(notNullValue()));
        try {
            File targetFile = new File(directory, targetPath);
            targetFile.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(targetFile);
            byte[] buf = new byte[256];
            while (true) {
                int read = in.read(buf);
                if (read < 0) {
                    break;
                }
                output.write(buf, 0, read);
            }
            output.close();
            assertThat(targetFile.getName(), targetFile.setExecutable(true), is(true));
        } finally {
            in.close();
        }
    }

    /**
     * Uses remote standard output.
     * @throws Exception if failed
     */
    @Test
    public void get() throws Exception {
        File file = folder.newFile("testing");
        put(file, "Hello, world!");
        JschConnection conn = new JschConnection(
                profile,
                Arrays.asList(profile.getGetCommand(), file.getAbsolutePath()));
        try {
            InputStream output = conn.openStandardOutput();
            conn.connect();
            String result = get(output);
            assertThat(result, is("Hello, world!"));
            int exit = conn.waitForExit(10000);
            assertThat(exit, is(0));
        } finally {
            conn.close();
        }
    }

    /**
     * Uses remote standard input.
     * @throws Exception if failed
     */
    @Test
    public void put() throws Exception {
        File file = folder.newFile("testing");
        file.delete();
        JschConnection conn = new JschConnection(
                profile,
                Arrays.asList(profile.getPutCommand(), file.getAbsolutePath()));
        try {
            conn.redirectStandardOutput(System.out, true);
            OutputStream out = conn.openStandardInput();
            conn.connect();
            out.write("Hello, world!".getBytes("UTF-8"));
            out.close();
            int exit = conn.waitForExit(10000);
            assertThat(exit, is(0));
        } finally {
            conn.close();
        }
        String result = get(file);
        assertThat(result, is("Hello, world!"));
    }

    /**
     * Execute delete.
     * @throws Exception if failed
     */
    @Test
    public void delete() throws Exception {
        File file = folder.newFile("testing");
        JschConnection conn = new JschConnection(
                profile,
                Arrays.asList(profile.getDeleteCommand(), file.getAbsolutePath()));
        try {
            conn.redirectStandardOutput(System.out, true);
            OutputStream out = conn.openStandardInput();
            conn.connect();
            out.write("Hello, world!".getBytes("UTF-8"));
            out.close();
            int exit = conn.waitForExit(10000);
            assertThat(exit, is(0));
        } finally {
            conn.close();
        }
        assertThat(file.toString(), file.exists(), is(false));
    }

    /**
     * Test for passing environment variables.
     * @throws Exception if failed
     */
    @Test
    public void env() throws Exception {
        putScript(target, "libexec/check-env.sh", SshProfile.COMMAND_GET);
        JschConnection conn = new JschConnection(
                profile,
                Arrays.asList(profile.getGetCommand()));
        try {
            InputStream output = conn.openStandardOutput();
            conn.connect();
            String result = get(output);
            assertThat(result, is("1"));
            int exit = conn.waitForExit(10000);
            assertThat(exit, is(0));
        } finally {
            conn.close();
        }
    }

    /**
     * Test for passing runtime context.
     * @throws Exception if failed
     */
    @Test
    public void inherit_context() throws Exception {
        RuntimeContext context = RuntimeContext.DEFAULT
            .mode(ExecutionMode.SIMULATION)
            .batchId("testbatch")
            .buildId("testverify");

        RuntimeContext.set(context);

        putScript(target, "libexec/check-context.sh", SshProfile.COMMAND_GET);
        Map<String, String> results = new HashMap<String, String>();
        JschConnection conn = new JschConnection(profile, Arrays.asList(profile.getGetCommand()));
        try {
            InputStream output = conn.openStandardOutput();
            conn.connect();
            Scanner s = new Scanner(output, "UTF-8");
            while (s.hasNextLine()) {
                String[] pair = s.nextLine().split("=", 2);
                if (pair.length == 2) {
                    results.put(pair[0], pair[1]);
                }
            }
            int exit = conn.waitForExit(10000);
            assertThat(exit, is(0));
        } finally {
            conn.close();
        }

        RuntimeContext restored = RuntimeContext.DEFAULT.apply(results);
        assertThat(results.toString(), restored, is(context));
    }

    private void put(File file, String content) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(content.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private String get(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            return get(in);
        } finally {
            in.close();
        }
    }

    private String get(InputStream in) throws IOException, UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        while (true) {
            int read = in.read(buf);
            if (read < 0) {
                break;
            }
            baos.write(buf, 0, read);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }
}
