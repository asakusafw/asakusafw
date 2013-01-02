/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.hadoop.fs;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.SystemUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link Clean}.
 */
public class CleanTest {

    private static final String OPT_KEEP = "-keep-days";

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * simple test case.
     * @throws Exception if failed to execute
     */
    @Test
    public void simple() throws Exception {
        File file = touch("file", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, path("file")));

        assertThat(exit, is(0));
        assertThat(file.toString(), file.exists(), is(false));
    }

    /**
     * simple test case for keep.
     * @throws Exception if failed to execute
     */
    @Test
    public void keep() throws Exception {
        File file = touch("file", 50);

        Clean c = createService(100);
        int exit = c.run(args(51, path("file")));

        assertThat(exit, is(0));
        assertThat(file.toString(), file.exists(), is(true));
    }

    /**
     * simple test case for folder.
     * @throws Exception if failed to execute
     */
    @Test
    public void folder() throws Exception {
        File file = touch("folder/file", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, path("folder")));

        assertThat(exit, is(0));
        assertThat(file.toString(), file.exists(), is(true));
    }

    /**
     * simple test case for recursive delete.
     * @throws Exception if failed to execute
     */
    @Test
    public void recursive() throws Exception {
        File file = touch("folder/file", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, "-r", path("folder")));

        assertThat(exit, is(0));
        assertThat(file.toString(), file.exists(), is(false));
    }

    /**
     * simple test case for dryrun.
     * @throws Exception if failed to execute
     */
    @Test
    public void dry_run() throws Exception {
        File file = touch("file", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, "-s", path("file")));

        assertThat(exit, is(0));
        assertThat(file.toString(), file.exists(), is(true));
    }

    /**
     * test case using wildcard.
     * @throws Exception if failed to execute
     */
    @Test
    public void wildcard() throws Exception {
        File f1 = touch("file1.txt", 50);
        File f2 = touch("file2.csv", 50);
        File f3 = touch("file3.txt", 50);
        File f4 = touch("file4.csv", 50);
        File f5 = touch("file5.txt", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, path("*.csv")));

        assertThat(exit, is(0));
        assertThat(f1.toString(), f1.exists(), is(true));
        assertThat(f2.toString(), f2.exists(), is(false));
        assertThat(f3.toString(), f3.exists(), is(true));
        assertThat(f4.toString(), f4.exists(), is(false));
        assertThat(f5.toString(), f5.exists(), is(true));
    }

    /**
     * deletes multiple files.
     * @throws Exception if failed to execute
     */
    @Test
    public void multiple() throws Exception {
        File f1 = touch("file1", 50);
        File f2 = touch("file2", 50);
        File f3 = touch("file3", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, path("file1"), path("file3")));

        assertThat(exit, is(0));
        assertThat(f1.toString(), f1.exists(), is(false));
        assertThat(f2.toString(), f2.exists(), is(true));
        assertThat(f3.toString(), f3.exists(), is(false));
    }

    /**
     * test case using deep wildcard.
     * @throws Exception if failed to execute
     */
    @Test
    public void deep_wildcard() throws Exception {
        File f1 = touch("a/file1.txt", 50);
        File f2 = touch("a/file2.csv", 50);
        File f3 = touch("a/file3.txt", 50);
        File f4 = touch("b/file4.csv", 50);
        File f5 = touch("b/file5.txt", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, path("*/*.csv")));

        assertThat(exit, is(0));
        assertThat(f1.toString(), f1.exists(), is(true));
        assertThat(f2.toString(), f2.exists(), is(false));
        assertThat(f3.toString(), f3.exists(), is(true));
        assertThat(f4.toString(), f4.exists(), is(false));
        assertThat(f5.toString(), f5.exists(), is(true));
    }

    /**
     * test case using deep wildcard.
     * @throws Exception if failed to execute
     */
    @Test
    public void skip_folder() throws Exception {
        File f1 = touch("a/file1", 50);
        File f2 = touch("a/file2", 100);
        File f3 = touch("a/file3", 50);
        File f4 = touch("b/file4", 50);
        File f5 = touch("b/file5", 50);
        touch("a", 50);
        touch("b", 50);

        Clean c = createService(100);
        int exit = c.run(args(30, "-r", path("*")));

        assertThat(exit, is(0));
        assertThat(f1.toString(), f1.exists(), is(false));
        assertThat(f2.toString(), f2.exists(), is(true));
        assertThat(f3.toString(), f3.exists(), is(false));
        assertThat(f4.toString(), f4.exists(), is(false));
        assertThat(f5.toString(), f5.exists(), is(false));

        assertThat("a", file("a").exists(), is(true));
        assertThat("a", file("b").exists(), is(false));
    }

    /**
     * test case using deep wildcard.
     * @throws Exception if failed to execute
     */
    @Test
    public void keep_folder() throws Exception {
        File f1 = touch("a/file1", 50);
        File f2 = touch("a/file2", 50);
        File f3 = touch("a/file3", 50);
        File f4 = touch("b/file4", 50);
        File f5 = touch("b/file5", 50);
        touch("a", 100);
        touch("b", 50);

        Clean c = createService(100);
        int exit = c.run(args(30, "-r", path("*")));

        assertThat(exit, is(0));
        assertThat(f1.toString(), f1.exists(), is(false));
        assertThat(f2.toString(), f2.exists(), is(false));
        assertThat(f3.toString(), f3.exists(), is(false));
        assertThat(f4.toString(), f4.exists(), is(false));
        assertThat(f5.toString(), f5.exists(), is(false));

        assertThat("a", file("a").exists(), is(true));
        assertThat("a", file("b").exists(), is(false));
    }

    /**
     * minus prefixed file.
     * @throws Exception if failed to execute
     */
    @Test
    public void minus_file() throws Exception {
        File file = touch("-r", 50);

        Clean c = createService(100);
        int exit = c.run(args(1, "--", path("-r")));

        assertThat(exit, is(0));
        assertThat(file.toString(), file.exists(), is(false));
    }

    /**
     * missing path.
     * @throws Exception if failed to execute
     */
    @Test
    public void missing_path() throws Exception {
        Clean c = createService(100);
        int exit = c.run(args(1, path("file")));

        assertThat(exit, is(not(0)));
    }

    /**
     * missing wildcard.
     * @throws Exception if failed to execute
     */
    @Test
    public void missing_wildcard() throws Exception {
        Clean c = createService(100);
        int exit = c.run(args(1, path("dir/*")));

        assertThat(exit, is(not(0)));
    }

    /**
     * no keep options.
     * @throws Exception if failed to execute
     */
    @Test
    public void missing_keep() throws Exception {
        touch("file", 50);

        Clean c = createService(100);
        int exit = c.run(new String[] { path("file") });

        assertThat(exit, is(not(0)));
    }

    /**
     * invalid keep options.
     * @throws Exception if failed to execute
     */
    @Test
    public void invalid_keep() throws Exception {
        touch("file", 50);

        Clean c = createService(100);
        int exit = c.run(new String[] { OPT_KEEP, "INVALID", path("file") });

        assertThat(exit, is(not(0)));
    }

    /**
     * unknown options.
     * @throws Exception if failed to execute
     */
    @Test
    public void unknown_opts() throws Exception {
        touch("file", 50);

        Clean c = createService(100);
        int exit = c.run(args(0, "-r", "-unknown", path("*")));

        assertThat(exit, is(not(0)));
    }

    /**
     * no targets.
     * @throws Exception if failed to execute
     */
    @Test
    public void empty_path() throws Exception {
        Clean c = createService(100);
        int exit = c.run(args(1));

        assertThat(exit, is(not(0)));
    }

    /**
     * invalid file system path.
     * @throws Exception if failed to execute
     */
    @Test
    public void malformed_path() throws Exception {
        Clean c = createService(100);
        int exit = c.run(args(1, ":MALFORMED:"));

        assertThat(exit, is(not(0)));
    }

    /**
     * invalid file system.
     * @throws Exception if failed to execute
     */
    @Test
    public void invalid_filesystem() throws Exception {
        Clean c = createService(100);
        int exit = c.run(args(1, "INVALID:///"));

        assertThat(exit, is(not(0)));
    }

    /**
     * test case for inaccessible folder.
     * @throws Exception if failed to execute
     */
    @Test
    public void inaccessible_folder() throws Exception {
        assumeAccessRestrictionAvailable();

        File f1 = touch("a/file1", 50);
        File f2 = touch("a/RESTRICTED/file", 50);
        File f3 = touch("a/file3", 50);

        file("a/RESTRICTED").setExecutable(false, false);
        int exit;
        try {
            Clean c = createService(100);
            exit = c.run(args(0, "-r", path("*")));
        } finally {
            file("a/RESTRICTED").setExecutable(true, false);
        }
        assertThat(exit, is(not(0)));
        assertThat(f1.toString(), f1.exists(), is(false));
        assertThat(f2.toString(), f2.exists(), is(true));
        assertThat(f3.toString(), f3.exists(), is(false));

        assertThat("a", file("a").exists(), is(true));
    }

    /**
     * test case for readonly file.
     * @throws Exception if failed to execute
     */
    @Test
    public void readonly_folder() throws Exception {
        assumeAccessRestrictionAvailable();

        File f1 = touch("a/file1", 50);
        File f2 = touch("a/RESTRICTED/file", 50);
        File f3 = touch("a/file3", 50);

        file("a/RESTRICTED").setWritable(false, false);
        int exit;
        try {
            Clean c = createService(100);
            exit = c.run(args(0, "-r", path("*")));
        } finally {
            file("a/RESTRICTED").setWritable(true, false);
        }
        assertThat(exit, is(not(0)));
        assertThat(f1.toString(), f1.exists(), is(false));
        assertThat(f2.toString(), f2.exists(), is(true));
        assertThat(f3.toString(), f3.exists(), is(false));

        assertThat("a", file("a").exists(), is(true));
    }

    /**
     * test case for symlink to file.
     * @throws Exception if failed to execute
     */
    @Test
    public void symlink_file() throws Exception {
        File f1 = touch("a/file1", 50);
        File f2 = touch("b/file2", 50);
        File f3 = link("b/link", f1, 50);

        Clean c = createService(100);
        int exit = c.run(args(0, "-r", path("b")));

        assertThat(exit, is(not(0)));
        assertThat(f1.toString(), f1.exists(), is(true));
        assertThat(f2.toString(), f2.exists(), is(false));
        assertThat(f3.toString(), f3.exists(), is(true));
    }

    /**
     * test case for symlink to dir.
     * @throws Exception if failed to execute
     */
    @Test
    public void symlink_dir() throws Exception {
        File f1 = touch("a/file1", 50);
        File f2 = touch("b/file2", 50);
        File f3 = link("b/link", file("a"), 50);

        Clean c = createService(100);
        int exit = c.run(args(0, "-r", path("b")));

        assertThat(exit, is(not(0)));
        assertThat(f1.toString(), f1.exists(), is(true));
        assertThat(f2.toString(), f2.exists(), is(false));
        assertThat(f3.toString(), f3.exists(), is(true));
    }

    /**
     * test case for symlink to file.
     * @throws Exception if failed to execute
     */
    @Test
    public void symlink_on_same_name() throws Exception {
        File f1 = touch("a/file", 50);
        File f2 = touch("b/file2", 50);
        File f3 = link("b/file", f1, 50);

        Clean c = createService(100);
        int exit = c.run(args(0, "-r", path("b")));

        assertThat(exit, is(not(0)));
        assertThat(f1.toString(), f1.exists(), is(true));
        assertThat(f2.toString(), f2.exists(), is(false));
        assertThat(f3.toString(), f3.exists(), is(true));
    }

    /**
     * test case for symlink to file.
     * @throws Exception if failed to execute
     */
    @Test
    public void symlink_on_same_dir() throws Exception {
        File f1 = touch("a/file1", 50);
        File f2 = touch("b/file2", 50);
        File f3 = link("b/file3.lnk", f2, 50);

        Clean c = createService(100);
        int exit = c.run(args(0, "-r", path("b/*.lnk")));

        assertThat(exit, is(not(0)));
        assertThat(f1.toString(), f1.exists(), is(true));
        assertThat(f2.toString(), f2.exists(), is(true));
        assertThat(f3.toString(), f3.exists(), is(true));
    }

    /**
     * test case for symlink to file.
     * @throws Exception if failed to execute
     */
    @Test
    public void symlink_lost() throws Exception {
        File f1 = touch("a/file1", 50);
        File f2 = touch("b/file2", 50);
        link("c/link", f1, 50);
        Assume.assumeThat(f1.delete(), is(true));

        Clean c = createService(100);
        c.run(args(0, "-r", path("*")));

        assertThat(f2.toString(), f2.exists(), is(false));
    }

    /**
     * test case for symlink to file.
     * @throws Exception if failed to execute
     */
    @Test
    public void symlink_self() throws Exception {
        File f1 = touch("a/file1", 50);
        File f2 = touch("b/file2", 50);
        File f3 = link("c/link", f1, 50);
        Assume.assumeThat(f1.delete(), is(true));
        f3.renameTo(f1);

        Clean c = createService(100);
        c.run(args(0, "-r", path("*")));

        assertThat(f2.toString(), f2.exists(), is(false));
    }

    private Clean createService(long days) {
        Clean service = new Clean(TimeUnit.DAYS.toMillis(days));
        service.setConf(new Configuration());
        return service;
    }

    private String[] args(int keep, String... args) {
        List<String> list = new ArrayList<String>();
        Collections.addAll(list, OPT_KEEP, String.valueOf(keep));
        Collections.addAll(list, args);
        return list.toArray(new String[list.size()]);
    }

    private String path(String path) {
        String uri = folder.getRoot().toURI().toString();
        return uri + "/" + path;
    }

    private File link(String path, File target, int day) throws IOException {
        Assume.assumeFalse("In Windows, tests with symlink are skipped", SystemUtils.IS_OS_WINDOWS);
        File link = file(path);
        link.getParentFile().mkdirs();
        try {
            Process process = new ProcessBuilder()
                .command("ln", "-s", target.getCanonicalPath(), link.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
            try {
                int exit = process.waitFor();
                Assume.assumeThat(exit, is(0));
            } finally {
                process.destroy();
            }
        } catch (Exception e) {
            Assume.assumeNoException(e);
        }
        touch(path, day);
        return link;
    }

    private File touch(String path, double day) throws IOException {
        File file = file(path);
        if (file.exists() == false) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        File root = folder.getRoot().getCanonicalFile();
        File current = file;
        while (true) {
            current = current.getCanonicalFile();
            if (root.equals(current)) {
                break;
            }
            boolean succeed = current.setLastModified((long) (day * TimeUnit.DAYS.toMillis(1)));
            if (succeed == false) {
                break;
            }
            current = current.getParentFile();
            if (current == null) {
                break;
            }
        }
        return file;
    }

    private File file(String path) {
        File file = new File(folder.getRoot(), path);
        return file;
    }

    private void assumeAccessRestrictionAvailable() throws IOException {
        File f = File.createTempFile("access-restriction-check", ".dummy");
        f.setReadable(false, false);
        try {
            if (f.canRead()) {
                System.err.println("Current context does not support access restriction.");
                Assume.assumeTrue(false);
            }
        } finally {
            f.setReadable(true, true);
            if (f.delete() == false) {
                System.err.printf("Failed to delete a dummy file: %s%n", f);
            }
        }
    }
}
