/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.yaess.jsch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.yaess.core.VariableResolver;
import com.jcraft.jsch.JSchException;

/**
 * Test for {@link JschProcessExecutor}.
 */
public class JschProcessExecutorTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private File privateKey;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        File home = new File(System.getProperty("user.home"));
        privateKey = new File(home, ".ssh/id_dsa").getCanonicalFile();
        if (privateKey.isFile() == false) {
            System.err.printf("Test is skipped because %s is not found%n", privateKey);
            Assume.assumeTrue(false);
        }
        if (new File("/bin/sh").canExecute() == false) {
            System.err.printf("Test is skipped because %s is not found%n", "/bin/sh");
            Assume.assumeTrue(false);
        }
    }

    /**
     * Extracts simple configuration.
     * @throws Exception if failed
     */
    @Test
    public void extract() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "tester");
        config.put(JschProcessExecutor.KEY_HOST, "example.com");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        assertThat(extracted.getUser(), is("tester"));
        assertThat(extracted.getHost(), is("example.com"));
        assertThat(extracted.getPort(), is(nullValue()));
        assertThat(extracted.getPrivateKey(), is(privateKey.getAbsolutePath()));
        assertThat(extracted.getPassPhrase(), is(nullValue()));
    }

    /**
     * Extracts configuration with port number.
     * @throws Exception if failed
     */
    @Test
    public void extract_with_port() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "tester");
        config.put(JschProcessExecutor.KEY_HOST, "example.com");
        config.put(JschProcessExecutor.KEY_PORT, "10022");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        assertThat(extracted.getUser(), is("tester"));
        assertThat(extracted.getHost(), is("example.com"));
        assertThat(extracted.getPort(), is(10022));
        assertThat(extracted.getPrivateKey(), is(privateKey.getAbsolutePath()));
        assertThat(extracted.getPassPhrase(), is(nullValue()));
    }

    /**
     * Extracts configuration with pass phrase.
     * @throws Exception if failed
     */
    @Test
    public void extract_with_passphrase() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "tester");
        config.put(JschProcessExecutor.KEY_HOST, "example.com");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());
        config.put(JschProcessExecutor.KEY_PASS_PHRASE, "Hello, world!");

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        assertThat(extracted.getUser(), is("tester"));
        assertThat(extracted.getHost(), is("example.com"));
        assertThat(extracted.getPort(), is(nullValue()));
        assertThat(extracted.getPrivateKey(), is(privateKey.getAbsolutePath()));
        assertThat(extracted.getPassPhrase(), is("Hello, world!"));
    }

    /**
     * Extracts configuration with variables.
     * @throws Exception if failed
     */
    @Test
    public void extract_variables() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "${user}");
        config.put(JschProcessExecutor.KEY_HOST, "${host}");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, "${id}");

        Map<String, String> variables = new HashMap<String, String>();
        variables.put("user", "variable");
        variables.put("host", "variables.example.com");
        variables.put("id", privateKey.getAbsolutePath());
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        assertThat(extracted.getUser(), is("variable"));
        assertThat(extracted.getHost(), is("variables.example.com"));
        assertThat(extracted.getPrivateKey(), is(privateKey.getAbsolutePath()));
    }

    /**
     * Attempts to extract configuration but user is not set.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void extract_without_user() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_HOST, "example.com");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor.extract("testing", config, resolver);
    }

    /**
     * Attempts to extract configuration but host is not set.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void extract_without_host() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "tester");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor.extract("testing", config, resolver);
    }

    /**
     * Attempts to extract configuration but private key is not set.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void extract_without_id() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "tester");
        config.put(JschProcessExecutor.KEY_HOST, "example.com");

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor.extract("testing", config, resolver);
    }

    /**
     * Attempts to extract configuration but some variables are not found.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void extract_invalid_variables() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "${__INVALID__}");
        config.put(JschProcessExecutor.KEY_HOST, "example.com");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor.extract("testing", config, resolver);
    }

    /**
     * Attempts to extract configuration but private key is not found.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void extract_invalid_port() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "tester");
        config.put(JschProcessExecutor.KEY_HOST, "example.com");
        config.put(JschProcessExecutor.KEY_PORT, "__INVALID__");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor.extract("testing", config, resolver);
    }

    /**
     * Attempts to extract configuration but private key is not found.
     * @throws Exception if failed
     */
    @Test(expected = JSchException.class)
    public void extract_invalid_id() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "tester");
        config.put(JschProcessExecutor.KEY_HOST, "example.com");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath() + "__INVALID__");

        Map<String, String> variables = new HashMap<String, String>();
        VariableResolver resolver = new VariableResolver(variables);

        JschProcessExecutor.extract("testing", config, resolver);
    }

    /**
     * Executes a simple command.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        File file = folder.newFile("testing");
        Assume.assumeTrue(file.delete());

        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "${USER}");
        config.put(JschProcessExecutor.KEY_HOST, "localhost");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        VariableResolver resolver = VariableResolver.system();

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        try {
            int exit = extracted.execute(
                    Arrays.asList("touch", file.getAbsolutePath()),
                    Collections.<String, String>emptyMap());
            assertThat(exit, is(0));
        } catch (IOException e) {
            System.err.printf("Test is skipped because SSH session was not available%n");
            Assume.assumeNoException(e);
        }
        assertThat(file.exists(), is(true));
    }

    /**
     * Executes a command with variables.
     * @throws Exception if failed
     */
    @Test
    public void execute_with_variables() throws Exception {
        File file1 = folder.newFile("testing1");
        Assume.assumeTrue(file1.delete());
        File file2 = folder.newFile("testing2");
        Assume.assumeTrue(file2.delete());
        File script = folder.newFile("script.sh");
        PrintWriter writer = new PrintWriter(script);
        try {
            writer.print("#!/bin/sh\n");
            writer.print("touch ${file1}\n");
            writer.print("touch ${file2}\n");
        } finally {
            writer.close();
        }
        script.setExecutable(true);

        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "${USER}");
        config.put(JschProcessExecutor.KEY_HOST, "localhost");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        VariableResolver resolver = VariableResolver.system();

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        try {
            Map<String, String> env = new HashMap<String, String>();
            env.put("file1", file1.getAbsolutePath());
            env.put("file2", file2.getAbsolutePath());
            int exit = extracted.execute(
                    Arrays.asList(script.getAbsolutePath()),
                    env);
            assertThat(exit, is(0));
        } catch (IOException e) {
            System.err.printf("Test is skipped because SSH session was not available: variables%n");
            e.printStackTrace();
            Assume.assumeNoException(e);
        }
        assertThat(file1.exists(), is(true));
        assertThat(file2.exists(), is(true));
    }

    /**
     * Attempts to execute a missing command.
     * @throws Exception if failed
     */
    @Test
    public void execute_missing() throws Exception {
        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "${USER}");
        config.put(JschProcessExecutor.KEY_HOST, "localhost");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        VariableResolver resolver = VariableResolver.system();

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        try {
            int exit = extracted.execute(
                    Arrays.asList(".__INVALID__"),
                    Collections.<String, String>emptyMap());
            assertThat(exit, is(not(0)));
        } catch (IOException e) {
            System.err.printf("Test is skipped because SSH session was not available%n");
            Assume.assumeNoException(e);
        }
    }

    /**
     * Executes a command with metacharacters.
     * @throws Exception if failed
     */
    @Test
    public void execute_metacharacter() throws Exception {
        File file = folder.newFile("$\"'`\\ file");
        Assume.assumeTrue(file.delete());

        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "${USER}");
        config.put(JschProcessExecutor.KEY_HOST, "localhost");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        VariableResolver resolver = VariableResolver.system();

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        try {
            int exit = extracted.execute(
                    Arrays.asList("touch", file.getAbsolutePath()),
                    Collections.<String, String>emptyMap());
            assertThat(exit, is(0));
        } catch (IOException e) {
            System.err.printf("Test is skipped because SSH session was not available%n");
            Assume.assumeNoException(e);
        }
        assertThat(file.exists(), is(true));
    }

    /**
     * Executes a command with variables which includes metacharacters.
     * @throws Exception if failed
     */
    @Test
    public void execute_metacharacter_variables() throws Exception {
        File file = folder.newFile("$\"'`\\= file");
        Assume.assumeTrue(file.delete());
        File script = folder.newFile("script.sh");
        PrintWriter writer = new PrintWriter(script);
        try {
            writer.print("#!/bin/sh\n");
            writer.print("touch \"${file}\"\n");
        } finally {
            writer.close();
        }
        script.setExecutable(true);

        Map<String, String> config = new HashMap<String, String>();
        config.put(JschProcessExecutor.KEY_USER, "${USER}");
        config.put(JschProcessExecutor.KEY_HOST, "localhost");
        config.put(JschProcessExecutor.KEY_PRIVATE_KEY, privateKey.getAbsolutePath());

        VariableResolver resolver = VariableResolver.system();

        JschProcessExecutor extracted = JschProcessExecutor.extract("testing", config, resolver);
        try {
            Map<String, String> env = new HashMap<String, String>();
            env.put("file", file.getAbsolutePath());
            int exit = extracted.execute(
                    Arrays.asList(script.getAbsolutePath()),
                    env);
            assertThat(exit, is(0));
        } catch (IOException e) {
            System.err.printf("Test is skipped because SSH session was not available: variables/meta%n");
            e.printStackTrace();
            Assume.assumeNoException(e);
        }
        assertThat(file.exists(), is(true));
    }
}
