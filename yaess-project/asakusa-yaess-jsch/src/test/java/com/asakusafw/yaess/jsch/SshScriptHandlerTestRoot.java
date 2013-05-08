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
package com.asakusafw.yaess.jsch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.SystemUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.ExecutionScriptHandler;

/**
 * Common features for testing this project.
 */
public class SshScriptHandlerTestRoot {

    static final Logger LOG = LoggerFactory.getLogger(SshScriptHandlerTestRoot.class);

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Private key path.
     */
    protected File privateKey;

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
     * Executes a script.
     * @param <T> script kind
     * @param script target script
     * @param handler target handler
     */
    protected <T extends ExecutionScript> void execute(T script, ExecutionScriptHandler<T> handler) {
        ExecutionContext context = new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, map());
        execute(context, script, handler);
    }

    /**
     * Executes a script.
     * @param <T> script kind
     * @param context current context
     * @param script target script
     * @param handler target handler
     */
    protected <T extends ExecutionScript>
    void execute(ExecutionContext context, T script, ExecutionScriptHandler<T> handler) {
        try {
            handler.execute(ExecutionMonitor.NULL, context, script);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } catch (IOException e) {
            e.printStackTrace();
            Assume.assumeNoException(e);
        }
    }

    /**
     * Get scripts result.
     * @param copier copier path
     * @return result lines
     * @throws IOException if failed
     */
    protected List<String> getOutput(File copier) throws IOException {
        File output = new File(copier.getParentFile(), copier.getName() + ".out");
        List<String> results = new ArrayList<String>();
        Scanner scanner = new Scanner(output);
        while (scanner.hasNextLine()) {
            results.add(scanner.nextLine());
        }
        return results;
    }

    /**
     * Returns Asakusa home.
     * @return path
     */
    protected File getAsakusaHome() {
        return folder.getRoot();
    }

    /**
     * Puts source file into the path as an executable file.
     * @param source original contents on classpath
     * @param path target path on Asakusa home
     * @return the put file
     * @throws IOException if failed
     */
    protected File putScript(String source, String path) throws IOException {
        File file = new File(getAsakusaHome(), path);
        return putScript(source, file);
    }

    /**
     * Puts source file into the path as an executable file.
     * @param source original contents on classpath
     * @param file target file path
     * @return the put file
     * @throws IOException if failed
     */
    protected File putScript(String source, File file) throws IOException {
        Assume.assumeThat("Windows does not supported", SystemUtils.IS_OS_WINDOWS, is(false));
        LOG.debug("Deploy script: {} -> {}", source, file);
        InputStream in = getClass().getResourceAsStream(source);
        assertThat(source, in, is(notNullValue()));
        try {
            copyTo(in, file);
        } finally {
            in.close();
        }
        file.setExecutable(true);
        return file;
    }

    /**
     * Returns set.
     * @param values elements
     * @return result
     */
    protected Set<String> set(String... values) {
        return new TreeSet<String>(Arrays.asList(values));
    }

    /**
     * Returns map.
     * @param keyValuePairs key value pairs
     * @return result
     */
    protected Map<String, String> map(String... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> conf = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            conf.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return conf;
    }

    private void copyTo(InputStream input, File target) throws IOException {
        assert input != null;
        assert target != null;
        File parent = target.getParentFile();
        assertThat(parent.getAbsolutePath(), parent, not(nullValue()));
        if (parent.isDirectory() == false) {
            assertThat(parent.getAbsolutePath(), parent.mkdirs(), is(true));
        }

        FileOutputStream output = new FileOutputStream(target);
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int read = input.read(buf);
                if (read < 0) {
                    break;
                }
                output.write(buf, 0, read);
            }
        } finally {
            output.close();
        }
    }

    /**
     * Returns a matcher which tests whether RHS is in LHS.
     * FIXME Matchers.hasItem() may be broken from JUnit 4.1.1.
     * @param matcher RHS
     * @return the matcher
     */
    protected static <T> Matcher<Iterable<T>> has(final Matcher<T> matcher) {
        return new BaseMatcher<Iterable<T>>() {
            @Override
            public boolean matches(Object item) {
                for (Object o : (Iterable<?>) item) {
                    if (matcher.matches(o)) {
                        return true;
                    }
                }
                return false;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("has ").appendDescriptionOf(matcher);
            }
        };
    }
}
