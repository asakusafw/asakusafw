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
package com.asakusafw.directio.hive.tools.cli;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.directio.hive.tools.cli.GenerateCreateTableTask.Configuration;
import com.asakusafw.info.hive.TableInfo;
import com.asakusafw.info.hive.FieldType.TypeName;

/**
 * Test for {@link GenerateCreateTableTask}.
 */
public class GenerateCreateTableTaskTest extends GenerateCeateTableTestRoot {

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Configuration conf = new Configuration(
                getClass().getClassLoader(),
                source(),
                Pattern.compile("testing"),
                Stringnizer.NULL,
                null,
                new File(folder.getRoot(), "output.txt"));

        Info1.schema = new TableInfo.Builder("testing")
            .withColumn("simple", TypeName.INT)
            .build();
        List<String> stmts = run(conf);
        assertThat(stmts, hasSize(1));
        verify(stmts.get(0));
    }

    /**
     * w/o accept table names (any tables).
     * @throws Exception if failed
     */
    @Test
    public void wo_acceptTableNames() throws Exception {
        Configuration conf = new Configuration(
                getClass().getClassLoader(),
                source(),
                null,
                Stringnizer.NULL,
                null,
                new File(folder.getRoot(), "output.txt"));

        Info1.schema = new TableInfo.Builder("t1")
            .withColumn("simple", TypeName.INT)
            .build();
        Info2.schema = new TableInfo.Builder("t2")
            .withColumn("simple", TypeName.INT)
            .build();
        List<String> stmts = run(conf);
        assertThat(stmts, hasSize(greaterThanOrEqualTo(2)));
    }

    /**
     * w/ location.
     * @throws Exception if failed
     */
    @Test
    public void w_location() throws Exception {
        Configuration conf = new Configuration(
                getClass().getClassLoader(),
                source(),
                Pattern.compile("testing"),
                table -> "here",
                null,
                new File(folder.getRoot(), "output.txt"));

        Info1.schema = new TableInfo.Builder("testing")
            .withColumn("simple", TypeName.INT)
            .build();
        List<String> stmts = run(conf);
        assertThat(stmts, hasSize(1));
        verify(stmts.get(0));
        assertThat(stmts.get(0), is(regex("LOCATION\\s+'here'")));
    }

    /**
     * w/ database name.
     * @throws Exception if failed
     */
    @Test
    public void w_databaseName() throws Exception {
        Configuration conf = new Configuration(
                getClass().getClassLoader(),
                source(),
                Pattern.compile("testing"),
                null,
                "testdb",
                new File(folder.getRoot(), "output.txt"));

        Info1.schema = new TableInfo.Builder("testing")
            .withColumn("simple", TypeName.INT)
            .build();
        List<String> stmts = run(conf);
        assertThat(stmts, hasSize(1));
        verify(stmts.get(0));
        assertThat(stmts.get(0), is(regex("testdb\\s*\\.\\s*testing")));
    }

    private Matcher<String> regex(String pattern) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                return Pattern.compile(pattern).matcher((CharSequence) item).find();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("contains ").appendValue(pattern);
            }
        };
    }

    private List<String> run(Configuration conf) throws IOException {
        new GenerateCreateTableTask().perform(conf);
        File target = conf.output;
        assertThat(target.exists(), is(true));
        return collectStatements(target);
    }

    private List<File> source() {
        File classes = new File("target/test-classes");
        Assume.assumeTrue(classes.exists());
        return Arrays.asList(classes);
    }
}
