/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceProfile;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link HadoopDataSourceUtil}.
 */
public class HadoopDataSourceUtilTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * Temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Loads a simple profile.
     */
    @Test
    public void loadProfiles_simple() {
        Configuration conf = new Configuration();
        conf.set(key("root"), MockHadoopDataSource.class.getName());
        conf.set(key("root", "path"), "/");

        List<DirectDataSourceProfile> profiles = HadoopDataSourceUtil.loadProfiles(conf);
        assertThat(profiles.size(), is(1));

        DirectDataSourceProfile profile = find(profiles, "");
        assertThat(profile.getTargetClass(), equalTo((Object) MockHadoopDataSource.class));
        assertThat(profile.getAttributes(), is(map()));
    }

    /**
     * Loads a profile with path.
     */
    @Test
    public void loadProfiles_path() {
        Configuration conf = new Configuration();
        conf.set(key("root"), MockHadoopDataSource.class.getName());
        conf.set(key("root", "path"), "example/path");

        List<DirectDataSourceProfile> profiles = HadoopDataSourceUtil.loadProfiles(conf);
        assertThat(profiles.size(), is(1));

        DirectDataSourceProfile profile = find(profiles, "example/path");
        assertThat(profile.getTargetClass(), equalTo((Object) MockHadoopDataSource.class));
        assertThat(profile.getAttributes(), is(map()));
    }

    /**
     * Loads a profile with attributes.
     */
    @Test
    public void loadProfiles_attribute() {
        Configuration conf = new Configuration();
        conf.set(key("root"), MockHadoopDataSource.class.getName());
        conf.set(key("root", "path"), "/");
        conf.set(key("root", "hello1"), "world1");
        conf.set(key("root", "hello2"), "world2");
        conf.set(key("root", "hello3"), "world3");

        List<DirectDataSourceProfile> profiles = HadoopDataSourceUtil.loadProfiles(conf);
        assertThat(profiles.size(), is(1));

        DirectDataSourceProfile profile = find(profiles, "");
        assertThat(profile.getTargetClass(), equalTo((Object) MockHadoopDataSource.class));
        assertThat(profile.getAttributes(), is(map("hello1", "world1", "hello2", "world2", "hello3", "world3")));
    }

    /**
     * Loads multiple profiles.
     */
    @Test
    public void loadProfiles_multiple() {
        Configuration conf = new Configuration();

        conf.set(key("a"), MockHadoopDataSource.class.getName());
        conf.set(key("a", "path"), "aaa");

        conf.set(key("b"), MockHadoopDataSource.class.getName());
        conf.set(key("b", "path"), "bbb");

        conf.set(key("c"), MockHadoopDataSource.class.getName());
        conf.set(key("c", "path"), "ccc");

        List<DirectDataSourceProfile> profiles = HadoopDataSourceUtil.loadProfiles(conf);
        assertThat(profiles.size(), is(3));

        DirectDataSourceProfile a = find(profiles, "aaa");
        assertThat(a.getTargetClass(), equalTo((Object) MockHadoopDataSource.class));
        assertThat(a.getAttributes(), is(map()));

        DirectDataSourceProfile b = find(profiles, "bbb");
        assertThat(b.getTargetClass(), equalTo((Object) MockHadoopDataSource.class));
        assertThat(b.getAttributes(), is(map()));

        DirectDataSourceProfile c = find(profiles, "ccc");
        assertThat(c.getTargetClass(), equalTo((Object) MockHadoopDataSource.class));
        assertThat(c.getAttributes(), is(map()));
    }

    private Map<String, String> map(String... kvs) {
        assertThat(kvs.length % 2, is(0));
        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            results.put(kvs[i], kvs[i + 1]);
        }
        return results;
    }

    private DirectDataSourceProfile find(List<DirectDataSourceProfile> profiles, String path) {
        for (DirectDataSourceProfile p : profiles) {
            if (p.getPath().equals(path)) {
                return p;
            }
        }
        throw new AssertionError(path);
    }

    private String key(String first, String... rest) {
        StringBuilder buf = new StringBuilder();
        buf.append(HadoopDataSourceUtil.PREFIX);
        buf.append(first);
        for (String s : rest) {
            buf.append(".");
            buf.append(s);
        }
        return buf.toString();
    }

    /**
     * create simple repository.
     * @throws Exception if failed
     */
    @Test
    public void loadRepository() throws Exception {
        Configuration conf = new Configuration();
        conf.set(key("testing"), MockHadoopDataSource.class.getName());
        conf.set(key("testing", "path"), "testing");
        conf.set(key("testing", "hello"), "world");
        DirectDataSourceRepository repo = HadoopDataSourceUtil.loadRepository(conf);
        DirectDataSource ds = repo.getRelatedDataSource("testing");
        assertThat(ds, instanceOf(MockHadoopDataSource.class));
        MockHadoopDataSource mock = (MockHadoopDataSource) ds;
        assertThat(mock.conf, is(notNullValue()));
        assertThat(mock.profile.getPath(), is("testing"));
    }

    /**
     * Test for transaction info.
     * @throws Exception if failed
     */
    @Test
    public void transactionInfo() throws Exception {
        Configuration conf = new Configuration();
        conf.set(HadoopDataSourceUtil.KEY_SYSTEM_DIR, folder.getRoot().getAbsoluteFile().toURI().toString());

        assertThat("empty system dir", folder.getRoot().listFiles(), is(new File[0]));
        assertThat(HadoopDataSourceUtil.findAllTransactionInfoFiles(conf).size(), is(0));

        Path t1 = HadoopDataSourceUtil.getTransactionInfoPath(conf, "ex1");
        assertThat(HadoopDataSourceUtil.getTransactionInfoExecutionId(t1), is("ex1"));
        t1.getFileSystem(conf).create(t1).close();

        assertThat(folder.getRoot().listFiles().length, is(greaterThan(0)));

        Path t2 = HadoopDataSourceUtil.getTransactionInfoPath(conf, "ex2");
        assertThat(t2, is(not(t1)));
        assertThat(HadoopDataSourceUtil.getTransactionInfoExecutionId(t2), is("ex2"));
        t2.getFileSystem(conf).create(t2).close();

        Path c2 = HadoopDataSourceUtil.getCommitMarkPath(conf, "ex2");
        assertThat(c2, is(not(t2)));
        c2.getFileSystem(conf).create(c2).close();

        List<Path> paths = new ArrayList<>();
        for (FileStatus stat : HadoopDataSourceUtil.findAllTransactionInfoFiles(conf)) {
            paths.add(stat.getPath());
        }
        assertThat(paths.size(), is(2));
        assertThat(paths, hasItem(t1));
        assertThat(paths, hasItem(t2));
    }

    /**
     * search by token.
     * @throws Exception if failed
     */
    @Test
    public void search_direct() throws Exception {
        touch("a.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("a.csv"));
        assertThat(normalize(results), is(path("a.csv")));
    }

    /**
     * search by token.
     * @throws Exception if failed
     */
    @Test
    public void search_direct_deep() throws Exception {
        touch("a.csv");
        touch("a/a.csv");
        touch("a/a/a.csv");
        touch("a/a/a/a.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("a/a/a.csv"));
        assertThat(normalize(results), is(path("a/a/a.csv")));
    }

    /**
     * search by wildcard.
     * @throws Exception if failed
     */
    @Test
    public void search_wildcard() throws Exception {
        touch("a.csv");
        touch("b.tsv");
        touch("c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("*.csv"));
        assertThat(normalize(results), is(path("a.csv", "c.csv")));
    }

    /**
     * search by wildcard for directories.
     * @throws Exception if failed
     */
    @Test
    public void search_wildcard_dir() throws Exception {
        touch("a/a.csv");
        touch("b/b/b.csv");
        touch("c/c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("*/*.csv"));
        assertThat(normalize(results), is(path("a/a.csv", "c/c.csv")));
    }

    /**
     * search using selection.
     * @throws Exception if failed
     */
    @Test
    public void search_selection() throws Exception {
        touch("a.csv");
        touch("b.csv");
        touch("c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("{a|b}.csv"));
        assertThat(normalize(results), is(path("a.csv", "b.csv")));
    }

    /**
     * search using multiple selection.
     * @throws Exception if failed
     */
    @Test
    public void search_selection_multiple() throws Exception {
        touch("a/a.csv");
        touch("a/b.csv");
        touch("a/c.csv");
        touch("b/a.csv");
        touch("b/b.csv");
        touch("b/c.csv");
        touch("c/a.csv");
        touch("c/b.csv");
        touch("c/c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(),
                FilePattern.compile("{a|b}/{b|c}.csv"));
        assertThat(normalize(results), is(path("a/b.csv", "a/c.csv", "b/b.csv", "b/c.csv")));
    }

    /**
     * search using complex selection.
     * @throws Exception if failed
     */
    @Test
    public void search_selection_complex() throws Exception {
        for (int year = 2001; year <= 2010; year++) {
            for (int month = 1; month <= 12; month++) {
                touch(String.format("data/%04d/%02d%s", year, month, ".csv"));
            }
        }
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(),
                FilePattern.compile("data/{2005/12|2003/11}.csv"));
        assertThat(normalize(results), is(path("data/2005/12.csv", "data/2003/11.csv")));
    }

    /**
     * search by traverse.
     * @throws Exception if failed
     */
    @Test
    public void search_traverse() throws Exception {
        touch("a/a.csv");
        touch("b/b.csv");
        touch("c/c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("**"));
        assertThat(normalize(results), is(path("", "a", "b", "c", "a/a.csv", "b/b.csv", "c/c.csv")));
    }

    /**
     * search by traverse only file.
     * @throws Exception if failed
     */
    @Test
    public void search_traverse_file() throws Exception {
        touch("a/a.csv");
        touch("b/b.csv");
        touch("c/c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> results = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("**/*.csv"));
        assertThat(normalize(results), is(path("a/a.csv", "b/b.csv", "c/c.csv")));
    }

    /**
     * single file does not cover anything.
     * @throws Exception if failed
     */
    @Test
    public void minimalCovered_trivial() throws Exception {
        touch("a.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> raw = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("**/*.csv"));
        assertThat(raw.size(), is(1));
        List<FileStatus> results = HadoopDataSourceUtil.onlyMinimalCovered(raw);
        assertThat(normalize(results), is(path("a.csv")));
    }

    /**
     * single file does not cover anything.
     * @throws Exception if failed
     */
    @Test
    public void minimalCovered_siblings() throws Exception {
        touch("dir/a.csv");
        touch("dir/b.csv");
        touch("dir/c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> raw = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("**/*.csv"));
        assertThat(raw.size(), is(3));
        List<FileStatus> results = HadoopDataSourceUtil.onlyMinimalCovered(raw);
        assertThat(normalize(results), is(path("dir/a.csv", "dir/b.csv", "dir/c.csv")));
    }

    /**
     * check covered.
     * @throws Exception if failed
     */
    @Test
    public void minimalCovered_parent() throws Exception {
        touch("dir/a.csv");
        touch("dir/b.csv");
        touch("dir/c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> raw = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("*/**"));
        assertThat(raw.size(), is(4));
        List<FileStatus> results = HadoopDataSourceUtil.onlyMinimalCovered(raw);
        assertThat(normalize(results), is(path("dir")));
    }

    /**
     * check covered.
     * @throws Exception if failed
     */
    @Test
    public void minimalCovered_deep() throws Exception {
        touch("dir/a.csv");
        touch("dir/a/b.csv");
        touch("dir/a/b/c.csv");
        FileSystem fs = getTempFileSystem();
        List<FileStatus> raw = HadoopDataSourceUtil.search(fs, getBase(), FilePattern.compile("dir/**"));
        for (Iterator<FileStatus> iterator = raw.iterator(); iterator.hasNext();) {
            FileStatus fileStatus = iterator.next();
            if (fileStatus.getPath().getName().equals("dir")) {
                iterator.remove();
            }
        }
        assertThat(raw.size(), is(5));
        List<FileStatus> results = HadoopDataSourceUtil.onlyMinimalCovered(raw);
        assertThat(normalize(results), is(path("dir/a.csv", "dir/a")));
    }

    /**
     * move files simply.
     * @throws Exception if failed
     */
    @Test
    public void move_simple() throws Exception {
        touch("src/a.csv");
        FileSystem fs = getTempFileSystem();
        HadoopDataSourceUtil.move(new Counter(), fs, getPath("src"), getPath("dst"));
        assertThat(collect(), is(path("dst/a.csv")));
    }

    /**
     * move multiple files.
     * @throws Exception if failed
     */
    @Test
    public void move_multiple() throws Exception {
        touch("src/a.csv");
        touch("src/b.csv");
        touch("src/c.csv");
        FileSystem fs = getTempFileSystem();
        HadoopDataSourceUtil.move(new Counter(), fs, getPath("src"), getPath("dst"));
        assertThat(collect(), is(path("dst/a.csv", "dst/b.csv", "dst/c.csv")));
    }

    /**
     * move multiple files.
     * @throws Exception if failed
     */
    @Test
    public void move_threads() throws Exception {
        List<String> paths = new ArrayList<>();
        List<String> expects = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            paths.add(String.format("src/%04d.csv", i));
            expects.add(String.format("dst/%04d.csv", i));
        }
        for (String s : paths) {
            touch(s);
        }
        FileSystem fs = getTempFileSystem();
        HadoopDataSourceUtil.move(new Counter(), fs, getPath("src"), getPath("dst"), 4);
        assertThat(collect(), is(path(expects.toArray(new String[expects.size()]))));
    }

    /**
     * move deep files.
     * @throws Exception if failed
     */
    @Test
    public void move_deep() throws Exception {
        touch("src/a.csv");
        touch("src/a/b.csv");
        touch("src/a/b/c.csv");
        FileSystem fs = getTempFileSystem();
        HadoopDataSourceUtil.move(new Counter(), fs, getPath("src"), getPath("dst"));
        assertThat(collect(), is(path("dst/a.csv", "dst/a/b.csv", "dst/a/b/c.csv")));
    }

    /**
     * move multiple files.
     * @throws Exception if failed
     */
    @Test
    public void move_merge() throws Exception {
        touch("src/a.csv");
        touch("src/b.csv");
        touch("dst/c.csv");
        FileSystem fs = getTempFileSystem();
        HadoopDataSourceUtil.move(new Counter(), fs, getPath("src"), getPath("dst"));
        assertThat(collect(), is(path("dst/a.csv", "dst/b.csv", "dst/c.csv")));
    }

    private List<String> collect() throws IOException {
        List<FileStatus> all = HadoopDataSourceUtil.search(getTempFileSystem(), getBase(), FilePattern.compile("**"));
        List<FileStatus> files = new ArrayList<>();
        for (FileStatus stat : all) {
            if (stat.isDirectory() == false) {
                files.add(stat);
            }
        }
        return normalize(files);
    }

    private List<String> normalize(List<FileStatus> stats) throws IOException {
        File base = folder.getRoot().getCanonicalFile();
        List<String> normalized = new ArrayList<>();
        for (FileStatus stat : stats) {
            URI uri = stat.getPath().toUri();
            try {
                File file = new File(uri).getCanonicalFile();
                String f = file.getAbsolutePath();
                String b = base.getAbsolutePath();
                assertThat(f, startsWith(b));
                String r = f.substring(b.length());
                while (r.startsWith(File.separator)) {
                    r = r.substring(1);
                }
                if (File.separatorChar != '/') {
                    r = r.replace(File.separatorChar, '/');
                }
                normalized.add(r);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        Collections.sort(normalized);
        return normalized;
    }

    private Matcher<List<String>> path(String... paths) {
        return new BaseMatcher<List<String>>() {
            @Override
            public boolean matches(Object obj) {
                @SuppressWarnings("unchecked")
                List<String> actuals = (List<String>) obj;
                List<String> normalized = new ArrayList<>(actuals);
                List<String> expected = new ArrayList<>();
                Collections.addAll(expected, paths);
                Collections.sort(expected);
                Collections.sort(normalized);
                return expected.equals(normalized);
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(Arrays.toString(paths));
            }
        };
    }

    private FileSystem getTempFileSystem() throws IOException {
        Configuration conf = new Configuration();
        LocalFileSystem local = FileSystem.getLocal(conf);
        return local;
    }

    private Path getBase() {
        return new Path(folder.getRoot().toURI());
    }

    private Path getPath(String path) {
        return new Path(getBase(), path);
    }

    private void touch(String path) throws IOException {
        File file = new File(folder.getRoot(), path);
        file.getParentFile().mkdirs();
        file.createNewFile();
        assertThat(file.isFile(), is(true));
    }
}
