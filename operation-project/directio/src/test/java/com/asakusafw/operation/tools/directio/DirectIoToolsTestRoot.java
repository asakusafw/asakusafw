/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.directio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.hadoop.HadoopDataSource;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceProfile;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;

/**
 * Utilities for testing Direct I/O tools.
 */
public abstract class DirectIoToolsTestRoot {

    static final Logger LOG = LoggerFactory.getLogger(DirectIoToolsTestRoot.class);

    /**
     * Test name.
     */
    @Rule
    public final TestName testName = new TestName();

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * hadoop configuration.
     */
    public final Configuration conf = new Configuration(false);

    private File workingDir;

    /**
     * Adds a Direct I/O entry.
     * @param id the data source ID
     * @param basePath the base path
     * @param resources the resources
     * @return the mapped directory
     */
    public File add(String id, String basePath, String... resources) {
        try {
            File base = folder.newFolder();
            for (String s : resources) {
                touch(base, s);
            }
            addEntry(id, basePath, base.toURI().toString());
            return base;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Adds a Direct I/O entry.
     * @param id the data source ID
     * @param basePath the base path
     * @param targetPath the target file system path
     */
    public void addEntry(String id, String basePath, String targetPath) {
        conf.set(key(id), HadoopDataSource.class.getName());
        conf.set(key(id, HadoopDataSourceUtil.KEY_PATH), basePath);
        conf.set(key(id, HadoopDataSourceProfile.KEY_PATH), targetPath);
    }

    /**
     * Returns the Hadoop configuration.
     * @return the configuration
     */
    public Configuration getConf() {
        return conf;
    }

    /**
     * Returns the working directory.
     * @return the working directory
     */
    public File openWorkingDir() {
        if (workingDir == null) {
            try {
                workingDir = folder.newFolder();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return workingDir;
    }

    /**
     * Invokes the command.
     * @param args the command line
     * @return the output contents
     */
    public List<String> invoke(String... args) {
        try {
            List<String> arguments = new ArrayList<>();
            Collections.addAll(arguments, args);
            Collections.addAll(arguments, "--conf", writeConfFile().getAbsolutePath());
            if (workingDir != null) {
                Collections.addAll(arguments, "--working-directory", workingDir.getAbsolutePath());
            }
            File outFile = new File(folder.getRoot(), "__OUTPUT__");
            Collections.addAll(arguments, "--output", outFile.getAbsolutePath());
            Collections.addAll(arguments, "--encoding", StandardCharsets.UTF_8.name());

            LOG.info("{}.{} : {}", getClass().getSimpleName(), testName.getMethodName(), Arrays.toString(args));
            try {
                DirectIo.exec(arguments.toArray(new String[arguments.size()]));
            } catch (RuntimeException e) {
                collectOutput(outFile).forEach(s -> LOG.info("{}", s));
                throw e;
            }
            collectOutput(outFile).forEach(s -> LOG.info("{}", s));
            return collectOutput(outFile);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static List<String> collectOutput(File file) throws IOException {
        if (file.isFile()) {
            return Files.readAllLines(file.toPath()).stream()
                    .filter(it -> it.isEmpty() == false)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private File writeConfFile() throws IOException {
        File file = folder.newFile();
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            conf.writeXml(out);
        }
        return file;
    }

    private static String key(String id) {
        return HadoopDataSourceUtil.PREFIX + id;
    }

    private static String key(String id, String name) {
        return String.format("%s.%s", key(id), name);
    }

    /**
     * Creates a new file/directory.
     * @param base the base path
     * @param path the relative path
     * @return the created file/directory
     */
    public static File touch(File base, String path) {
        try {
            File file = new File(base, path);
            if (path.endsWith("/")) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
                write(file, path);
            }
            return file;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Read contents.
     * @param file the target file
     * @return the contents
     */
    public static String read(File file) {
        try {
            return Files.readAllLines(file.toPath()).stream()
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(file));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Write contents.
     * @param file the target file
     * @param contents the file contents
     * @return the target file
     */
    public static File write(File file, String contents) {
        try {
            Files.write(file.toPath(), Arrays.asList(contents));
            return file;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Tests whether or not it exists.
     * @return matcher
     */
    public static Matcher<File> exists() {
        return new BaseMatcher<File>() {
            @Override
            public boolean matches(Object item) {
                return ((File) item).exists();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("exists");
            }
        };
    }

    /**
     * Tests whether or not it exists.
     * @param path the sub path
     * @return matcher
     */
    public static Matcher<File> exists(String path) {
        return new BaseMatcher<File>() {
            @Override
            public boolean matches(Object item) {
                return new File((File) item, path).exists();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("exists ").appendValue(path);
            }
        };
    }

    /**
     * Tests whether or not it is file.
     * @return matcher
     */
    public static Matcher<File> file() {
        return new BaseMatcher<File>() {
            @Override
            public boolean matches(Object item) {
                return ((File) item).isFile();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("is file");
            }
        };
    }

    /**
     * Tests whether or not it is directory.
     * @return matcher
     */
    public static Matcher<File> directory() {
        return new BaseMatcher<File>() {
            @Override
            public boolean matches(Object item) {
                return ((File) item).isDirectory();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("is directory");
            }
        };
    }

    /**
     * Tests whether or not they are same path.
     * @param file the target path
     * @return matcher
     */
    public static Matcher<File> samePath(File file) {
        return new BaseMatcher<File>() {
            @Override
            public boolean matches(Object item) {
                try {
                    return ((File) item).getCanonicalPath().equals(file.getCanonicalPath());
                } catch (IOException e) {
                    LOG.debug("error", e);
                    return false;
                }
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("is same path with ").appendValue(file);
            }
        };
    }

    /**
     * Tests whether or not they are same path.
     * @param files the files
     * @return matcher
     */
    public static Matcher<Iterable<? extends File>> samePaths(File... files) {
        List<Matcher<? super File>> matcher = Arrays.stream(files)
                .map(f -> samePath(f))
                .collect(Collectors.toList());
        return Matchers.containsInAnyOrder(matcher);
    }
}
