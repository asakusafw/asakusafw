/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.util.hadoop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for {@link ConfigurationProvider}.
 */
public class ConfigurationProviderTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private ClassLoader contextLoader;

    private boolean contextLoaderSaved = false;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        this.contextLoader = Thread.currentThread().getContextClassLoader();
        this.contextLoaderSaved = true;
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (contextLoaderSaved) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(contextLoader);
            assertThat(cl, is(sameInstance(contextLoader)));
        }
    }

    /**
     * search for hadoop command from HADOOP_HOME.
     */
    @Test
    public void findHadoopCommand_explicit() {
        putExec("bin/hadoop");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_HOME", folder.getRoot().getAbsolutePath());

        File file = ConfigurationProvider.findHadoopCommand(envp);
        assertThat(file, is(notNullValue()));
        assertThat(file.toString(), file.canExecute(), is(true));
    }

    /**
     * search for hadoop command from PATH.
     */
    @Test
    public void findHadoopCommand_path() {
        putExec("bin/hadoop");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("PATH", new File(folder.getRoot(), "bin").getAbsolutePath());

        File file = ConfigurationProvider.findHadoopCommand(envp);
        assertThat(file, is(notNullValue()));
        assertThat(file.toString(), file.canExecute(), is(true));
    }

    /**
     * search for hadoop command from HADOOP_HOME.
     */
    @Test
    public void findHadoopCommand_both() {
        putExec("home/bin/hadoop");
        putExec("path/hadoop");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_HOME", new File(folder.getRoot(), "home").getAbsolutePath());
        envp.put("PATH", new File(folder.getRoot(), "path").getAbsolutePath());

        File file = ConfigurationProvider.findHadoopCommand(envp);
        assertThat(file, is(notNullValue()));
        assertThat(file.toString(), file.canExecute(), is(true));
        assertThat(file.toString(), file.getParentFile().getName(), is("bin"));
    }

    /**
     * search for hadoop command from PATH.
     */
    @Test
    public void findHadoopCommand_manypath() {
        putExec("path1/java");
        putExec("path2/hadoop");
        putExec("path3/ant");

        StringBuilder buf = new StringBuilder();
        buf.append(File.pathSeparator);
        buf.append(new File(folder.getRoot(), "path1").getAbsolutePath());
        buf.append(File.pathSeparator);
        buf.append(new File(folder.getRoot(), "path2").getAbsolutePath());
        buf.append(File.pathSeparator);
        buf.append(new File(folder.getRoot(), "path3").getAbsolutePath());
        buf.append(File.pathSeparator);

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("PATH", buf.toString());

        File file = ConfigurationProvider.findHadoopCommand(envp);
        assertThat(file, is(notNullValue()));
        assertThat(file.toString(), file.canExecute(), is(true));
        assertThat(file.toString(), file.getParentFile().getName(), is("path2"));
    }

    /**
     * search for confdir from HADOOP_CONF.
     */
    @Test
    public void newInstance_explicit() {
        putConf("conf/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_CONF", new File(folder.getRoot(), "conf").getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    /**
     * search for confdir from HADOOP_CMD.
     */
    @Test
    public void newInstance_cmd() {
        File cmd = putExec("bin/hadoop");
        create("conf/hadoop-env.sh");
        putConf("conf/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_CMD", cmd.getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    /**
     * search for confdir from HADOOP_CMD but no conf/hadoop-env_sh.
     */
    @Test
    public void newInstance_cmd_no_env_sh() {
        File cmd = putExec("bin/hadoop");
        putConf("conf/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_CMD", cmd.getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(false));
    }

    /**
     * search for confdir from HADOOP_CMD.
     */
    @Test
    public void newInstance_cmd_etc() {
        File cmd = putExec("bin/hadoop");
        putConf("etc/hadoop/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_CMD", cmd.getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    /**
     * search for confdir from HADOOP_HOME.
     */
    @Test
    public void newInstance_home() {
        putExec("bin/hadoop");
        create("conf/hadoop-env.sh");
        putConf("conf/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_HOME", folder.getRoot().getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    /**
     * search for confdir from HADOOP_HOME but no conf/hadoop-env_sh.
     */
    @Test
    public void newInstance_home_no_env_sh() {
        putExec("bin/hadoop");
        putConf("conf/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_HOME", folder.getRoot().getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(false));
    }

    /**
     * search for confdir from HADOOP_HOME.
     */
    @Test
    public void newInstance_home_etc() {
        putExec("bin/hadoop");
        putConf("etc/hadoop/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_HOME", folder.getRoot().getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    /**
     * search for confdir from PATH.
     */
    @Test
    public void newInstance_path() {
        putExec("bin/hadoop");
        create("conf/hadoop-env.sh");
        putConf("conf/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("PATH", new File(folder.getRoot(), "bin").getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    /**
     * search for confdir from HADOOP_HOME but no conf/hadoop-env_sh.
     */
    @Test
    public void newInstance_path_no_env_sh() {
        putExec("bin/hadoop");
        putConf("conf/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("PATH", new File(folder.getRoot(), "bin").getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(false));
    }

    /**
     * search for confdir from PATH.
     */
    @Test
    public void newInstance_path_etc() {
        putExec("bin/hadoop");
        putConf("etc/hadoop/core-site.xml");

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("PATH", new File(folder.getRoot(), "bin").getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    /**
     * reuses class loaders.
     * @throws Exception if failed
     */
    @Test
    public void newInstance_classloader_reuse() throws Exception {
        File file = putConf("conf/core-site.xml");
        URL dir = file.getParentFile().toURI().toURL();

        Configuration c1 = new ConfigurationProvider(dir).newInstance();
        assertThat(isLoaded(c1), is(true));

        Configuration c2 = new ConfigurationProvider(dir).newInstance();
        assertThat(c2.getClassLoader(), is(sameInstance(c1.getClassLoader())));
    }

    /**
     * search for confdir from PATH using symlink.
     * @throws IOException IOException
     */
    @Test
    public void symlink() throws IOException {
        Assume.assumeThat(SystemUtils.IS_OS_WINDOWS, is(false));

        File cmd = putExec("hadoop/bin/hadoop");
        putConf("hadoop/etc/hadoop/core-site.xml");

        File path = folder.newFolder("path");
        try {
            Process proc = new ProcessBuilder(
                    "ln",
                    "-s",
                    cmd.getAbsolutePath(),
                    new File(path, "hadoop").getAbsolutePath()).start();
            try {
                int exitCode = proc.waitFor();
                Assume.assumeThat(exitCode, is(0));
            } finally {
                proc.destroy();
            }
        } catch (Exception e) {
            System.out.println("Failed to create symlink");
            e.printStackTrace(System.out);
            Assume.assumeNoException(e);
        }

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("PATH", path.getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));

        File file = ConfigurationProvider.findHadoopCommand(envp);
        assertThat(file, is(notNullValue()));
        assertThat(file.toString(), file.canExecute(), is(true));
        assertThat(file.toString(), file.getParentFile().getName(), is("path"));
    }

    /**
     * search for confdir auto.
     * @throws Exception if failed
     */
    @Test
    public void newInstance_auto() throws Exception {
        File cmd = putExec("testing/testcmd");
        File site = putConf("auto/core-site.xml");

        PrintWriter writer = new PrintWriter(cmd);
        try {
            writer.printf("#/bin/sh\n");
            writer.printf("if [ $# -eq 0 ]\n");
            writer.printf("then\n");
            writer.printf("  exit 0\n");
            writer.printf("elif [ $# -ne 2 ]\n");
            writer.printf("then\n");
            writer.printf("  exit 1\n");
            writer.printf("else\n");
            writer.printf("  if [ \"$1\" != '%s' ]\n", ConfigurationDetecter.class.getName());
            writer.printf("  then\n");
            writer.printf("    exit 1\n");
            writer.printf("  fi\n");
            writer.printf("  echo -n '%s' > \"$2\"\n", site.getParentFile().getAbsolutePath());
            writer.printf("  exit 0\n");
            writer.printf("fi\n");
        } finally {
            writer.close();
        }
        try {
            Process proc = new ProcessBuilder(cmd.getAbsolutePath()).start();
            try {
                int exitCode = proc.waitFor();
                Assume.assumeThat(exitCode, is(0));
            } finally {
                proc.destroy();
            }
        } catch (Exception e) {
            System.out.println("Failed to execute bash script");
            e.printStackTrace(System.out);
            Assume.assumeNoException(e);
        }

        Map<String, String> envp = new HashMap<String, String>();
        envp.put("HADOOP_CMD", cmd.getAbsolutePath());

        Configuration conf = new ConfigurationProvider(envp).newInstance();
        assertThat(isLoaded(conf), is(true));
    }

    private File putExec(String path) {
        File file = create(path);
        file.setExecutable(true);
        return file;
    }

    private File create(String path) {
        File file = new File(folder.getRoot(), path);
        assertThat(file.getParentFile().isDirectory() || file.getParentFile().mkdirs(), is(true));
        try {
            assertThat(file.createNewFile(), is(true));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        file.setExecutable(false);
        return file;
    }

    private File putConf(String path) {
        Configuration c = new Configuration(false);
        c.set("testing.conf", "added");
        File file = create(path);
        try {
            OutputStream s = new FileOutputStream(file);
            try {
                c.writeXml(s);
            } finally {
                s.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return file;
    }

    private boolean isLoaded(Configuration c) {
        return c.get("testing.conf", "not added").equals("added");
    }
}
