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
package com.asakusafw.directio.hive.tools.cli;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.directio.hive.info.FieldType.TypeName;
import com.asakusafw.directio.hive.info.TableInfo;
import com.asakusafw.directio.hive.tools.cli.GenerateCreateTableTask.Configuration;

/**
 * Test for {@link GenerateCreateTable}.
 */
public class GenerateCreateTableTest extends GenerateCeateTableTestRoot {

    /**
     * exec simple case.
     * @throws Exception if failed
     */
    @Test
    public void exec_simple() throws Exception {
        File output = new File(folder.getRoot(), "output.txt");
        Info1.schema = new TableInfo.Builder("testing")
            .withColumn("testing", TypeName.INT)
            .build();
        int status = GenerateCreateTable.execute(new String[] {
                "--classpath", source(),
                "--output", output.getPath(),
                "--include", "testing",
        });
        assertThat(status, is(0));
        List<String> stmts = collectStatements(output);
        assertThat(stmts, hasSize(1));
        verify(stmts.get(0));
    }

    /**
     * exec w/o arguments.
     * @throws Exception if failed
     */
    @Test
    public void exec_wo_arguments() throws Exception {
        int status = GenerateCreateTable.execute();
        assertThat(status, is(not(0)));
    }

    /**
     * parse minimum args.
     * @throws Exception if failed
     */
    @Test
    public void parse_simple() throws Exception {
        File source = folder.newFolder();
        File output = folder.newFile();
        Configuration conf = parse(new String[] {
                "--classpath", source.getPath(),
                "--output", output.getPath(),
        });
        assertThat(conf.sources, hasSize(1));
        assertThat(conf.sources.get(0).getCanonicalFile(), is(source.getCanonicalFile()));
        assertThat(conf.output.getCanonicalFile(), is(output.getCanonicalFile()));
        assertThat(conf.classLoader, canLoad(source));
    }

    /**
     * parse w/ arguments.
     * @throws Exception if failed
     */
    @Test
    public void parse_w_args() throws Exception {
        File plugin = folder.newFolder();
        Configuration conf = parse(new String[] {
                "--classpath", folder.newFolder().getPath(),
                "--output", folder.newFile().getPath(),
                "--location", "/home/dwh",
                "--database", "testdb",
                "--pluginpath", plugin.getPath(),
        });
        String location = conf.locationProvider.toString(new TableInfo.Builder("testing")
                .withColumn("p", TypeName.INT)
                .build());
        assertThat(location, is("/home/dwh/testing"));
        assertThat(conf.databaseName, is("testdb"));
        assertThat(conf.classLoader, canLoad(plugin));
    }

    private Matcher<ClassLoader> canLoad(final File path) {
        return new BaseMatcher<ClassLoader>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof URLClassLoader) {
                    try {
                        URL url = path.toURI().toURL();
                        URL[] urls = ((URLClassLoader) item).getURLs();
                        return Arrays.asList(urls).contains(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("can load ").appendValue(path);
            }
        };
    }

    private String source() {
        File classes = new File("target/test-classes");
        Assume.assumeTrue(classes.exists());
        return classes.getPath();
    }

    private Configuration parse(String[] args) throws ParseException, IOException {
        Configuration conf = GenerateCreateTable.parseConfiguration(args);
        if (conf.classLoader instanceof Closeable) {
            ((Closeable) conf.classLoader).close();
        }
        return conf;
    }
}
