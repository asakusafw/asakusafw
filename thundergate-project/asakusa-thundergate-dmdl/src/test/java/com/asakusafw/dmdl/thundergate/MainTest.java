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
package com.asakusafw.dmdl.thundergate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.utils.collections.Lists;

/**
 * Test for {@link Main}.
 */
public class MainTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * create configuration.
     * @throws Exception if test was failed
     */
    @Test
    public void simple() throws Exception {
        List<String> arguments = Lists.create();
        File jdbc = jdbc();
        File output = folder.newFolder("output").getCanonicalFile().getAbsoluteFile();

        Collections.addAll(arguments, "-jdbc", jdbc.getAbsolutePath());
        Collections.addAll(arguments, "-output", output.getAbsolutePath());
        Collections.addAll(arguments, "-encoding", "ASCII");
        Collections.addAll(arguments, "-includes", "ACCEPT|DENIED");
        Collections.addAll(arguments, "-excludes", "DENIED");

        Configuration conf = Main.loadConfigurationFromArguments(
                arguments.toArray(new String[arguments.size()]));

        assertThat(conf.getJdbcDriver(), is("com.asakusafw.Driver"));
        assertThat(conf.getJdbcUrl(), is("asakusa:thundergate"));
        assertThat(conf.getJdbcUser(), is("asakusa"));
        assertThat(conf.getJdbcPassword(), is("asakusapw"));
        assertThat(conf.getDatabaseName(), is("asakusadb"));
        assertThat(conf.getEncoding(), is(Charset.forName("ASCII")));
        assertThat(conf.getOutput(), is(output));
        assertThat(conf.getMatcher().acceptModel("ACCEPT"), is(true));
        assertThat(conf.getMatcher().acceptModel("NOT_ACCEPT"), is(false));
        assertThat(conf.getMatcher().acceptModel("DENIED"), is(false));
        assertThat(conf.getMatcher().acceptModel("__TG_CACHE_INFO"), is(false));
        assertThat(conf.getRecordLockDdlOutput(), is(nullValue()));
    }

    /**
     * configuration with empty encoding.
     * @throws Exception if test was failed
     */
    @Test
    public void encoding_empty() throws Exception {
        List<String> arguments = Lists.create();
        File jdbc = jdbc();
        File output = folder.newFolder("output").getCanonicalFile().getAbsoluteFile();

        Collections.addAll(arguments, "-jdbc", jdbc.getAbsolutePath());
        Collections.addAll(arguments, "-output", output.getAbsolutePath());

        Configuration conf = Main.loadConfigurationFromArguments(
                arguments.toArray(new String[arguments.size()]));

        assertThat(conf.getEncoding(), is(Charset.forName("UTF-8")));
    }

    /**
     * configuration with empty includes/excludes.
     * @throws Exception if test was failed
     */
    @Test
    public void matcher_empty() throws Exception {
        List<String> arguments = Lists.create();

        File jdbc = jdbc();
        File output = folder.newFolder("output").getCanonicalFile().getAbsoluteFile();

        Collections.addAll(arguments, "-jdbc", jdbc.getAbsolutePath());
        Collections.addAll(arguments, "-output", output.getAbsolutePath());
        Collections.addAll(arguments, "-encoding", "ASCII");
        Configuration conf = Main.loadConfigurationFromArguments(arguments.toArray(new String[arguments.size()]));

        assertThat(conf.getMatcher().acceptModel("ACCEPT"), is(true));
        assertThat(conf.getMatcher().acceptModel("NOT_ACCEPT"), is(true));
        assertThat(conf.getMatcher().acceptModel("__TG_CACHE_INFO"), is(false));
    }

    /**
     * configuration with empty includes/excludes.
     * @throws Exception if test was failed
     */
    @Test
    public void matcher_only_includes() throws Exception {
        List<String> arguments = Lists.create();

        File jdbc = jdbc();
        File output = folder.newFolder("output").getCanonicalFile().getAbsoluteFile();

        Collections.addAll(arguments, "-jdbc", jdbc.getAbsolutePath());
        Collections.addAll(arguments, "-output", output.getAbsolutePath());
        Collections.addAll(arguments, "-encoding", "ASCII");
        Collections.addAll(arguments, "-includes", "ACCEPT|DENIED");
        Configuration conf = Main.loadConfigurationFromArguments(arguments.toArray(new String[arguments.size()]));

        assertThat(conf.getMatcher().acceptModel("ACCEPT"), is(true));
        assertThat(conf.getMatcher().acceptModel("NOT_ACCEPT"), is(false));
        assertThat(conf.getMatcher().acceptModel("DENIED"), is(true));
        assertThat(conf.getMatcher().acceptModel("__TG_CACHE_INFO"), is(false));
    }

    /**
     * configuration with empty includes/excludes.
     * @throws Exception if test was failed
     */
    @Test
    public void matcher_only_excludes() throws Exception {
        List<String> arguments = Lists.create();

        File jdbc = jdbc();
        File output = folder.newFolder("output").getCanonicalFile().getAbsoluteFile();

        Collections.addAll(arguments, "-jdbc", jdbc.getAbsolutePath());
        Collections.addAll(arguments, "-output", output.getAbsolutePath());
        Collections.addAll(arguments, "-encoding", "ASCII");
        Collections.addAll(arguments, "-excludes", "DENIED");
        Configuration conf = Main.loadConfigurationFromArguments(arguments.toArray(new String[arguments.size()]));

        assertThat(conf.getMatcher().acceptModel("ACCEPT"), is(true));
        assertThat(conf.getMatcher().acceptModel("NOT_ACCEPT"), is(true));
        assertThat(conf.getMatcher().acceptModel("DENIED"), is(false));
        assertThat(conf.getMatcher().acceptModel("__TG_CACHE_INFO"), is(false));
    }

    /**
     * create configuration with cache.
     * @throws Exception if test was failed
     */
    @Test
    public void with_cache() throws Exception {
        List<String> arguments = Lists.create();

        File jdbc = jdbc();
        File output = folder.newFolder("output").getCanonicalFile().getAbsoluteFile();

        Collections.addAll(arguments, "-jdbc", jdbc.getAbsolutePath());
        Collections.addAll(arguments, "-output", output.getAbsolutePath());
        Collections.addAll(arguments, "-encoding", "ASCII");
        Collections.addAll(arguments, "-includes", "ACCEPT|DENIED");
        Collections.addAll(arguments, "-excludes", "DENIED");
        Collections.addAll(arguments, "-sid_column", "SID");
        Collections.addAll(arguments, "-timestamp_column", "LAST_UPDT_DATETIME");
        Collections.addAll(arguments, "-delete_flag_column", "LOGICAL_DEL_FLG");
        Collections.addAll(arguments, "-delete_flag_value", "\"1\"");

        Configuration conf = Main.loadConfigurationFromArguments(
                arguments.toArray(new String[arguments.size()]));

        assertThat(conf.getJdbcDriver(), is("com.asakusafw.Driver"));
        assertThat(conf.getJdbcUrl(), is("asakusa:thundergate"));
        assertThat(conf.getJdbcUser(), is("asakusa"));
        assertThat(conf.getJdbcPassword(), is("asakusapw"));
        assertThat(conf.getDatabaseName(), is("asakusadb"));
        assertThat(conf.getEncoding(), is(Charset.forName("ASCII")));
        assertThat(conf.getOutput(), is(output));
        assertThat(conf.getMatcher().acceptModel("ACCEPT"), is(true));
        assertThat(conf.getMatcher().acceptModel("DENIED"), is(false));
        assertThat(conf.getSidColumn(), is("SID"));
        assertThat(conf.getTimestampColumn(), is("LAST_UPDT_DATETIME"));
        assertThat(conf.getDeleteFlagColumn(), is("LOGICAL_DEL_FLG"));
        assertThat(conf.getDeleteFlagValue().toStringValue(), is("1"));
    }

    /**
     * configuration with record lock ddl output.
     * @throws Exception if test was failed
     */
    @Test
    public void with_rlddl() throws Exception {
        List<String> arguments = Lists.create();

        File jdbc = jdbc();
        File output = folder.newFolder("output").getCanonicalFile().getAbsoluteFile();
        File rl = folder.newFolder("rl").getCanonicalFile().getAbsoluteFile();

        Collections.addAll(arguments, "-jdbc", jdbc.getAbsolutePath());
        Collections.addAll(arguments, "-output", output.getAbsolutePath());
        Collections.addAll(arguments, "-encoding", "ASCII");
        Collections.addAll(arguments, "-record_lock_ddl_output", rl.getAbsolutePath());
        Configuration conf = Main.loadConfigurationFromArguments(arguments.toArray(new String[arguments.size()]));

        assertThat(conf.getRecordLockDdlOutput(), is(rl));
    }

    private File jdbc() throws IOException {
        Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty(Constants.K_JDBC_DRIVER, "com.asakusafw.Driver");
        jdbcProperties.setProperty(Constants.K_JDBC_URL, "asakusa:thundergate");
        jdbcProperties.setProperty(Constants.K_JDBC_USER, "asakusa");
        jdbcProperties.setProperty(Constants.K_JDBC_PASSWORD, "asakusapw");
        jdbcProperties.setProperty(Constants.K_DATABASE_NAME, "asakusadb");
        File jdbc = jdbc(jdbcProperties);
        return jdbc;
    }

    private File jdbc(Properties jdbcProperties) throws IOException {
        File jdbc = folder.newFile("jdbc.properties");
        FileOutputStream out = new FileOutputStream(jdbc);
        try {
            jdbcProperties.store(out, "testing");
        } finally {
            out.close();
        }
        return jdbc;
    }
}
