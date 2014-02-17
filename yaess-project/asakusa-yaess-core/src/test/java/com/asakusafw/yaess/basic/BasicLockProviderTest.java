/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.yaess.core.ExecutionLock;
import com.asakusafw.yaess.core.ExecutionLockProvider;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;

/**
 * Test for {@link BasicLockProvider}.
 */
public class BasicLockProviderTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File lockDir = folder.getRoot();
        int start = lockDir.list().length;

        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, lockDir.getAbsolutePath());

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance = profile.newInstance();
        ExecutionLock lock = instance.newInstance("batch");
        try {
            lock.beginFlow("flow", "exec");
            assertThat(lockDir.list().length, is(greaterThan(start)));
        } finally {
            lock.close();
        }
        assertThat(lockDir.list().length, is(start));
    }

    /**
     * Configure using variable.
     * @throws Exception if failed
     */
    @Test
    public void with_variable() throws Exception {
        File lockDir = folder.getRoot();
        int start = lockDir.list().length;
        VariableResolver var = new VariableResolver(Collections.singletonMap(
                "LOCATION", lockDir.getAbsolutePath()));
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, "${LOCATION}");

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, new ProfileContext(getClass().getClassLoader(), var));
        ExecutionLockProvider instance = profile.newInstance();
        ExecutionLock lock = instance.newInstance("batch");
        try {
            lock.beginFlow("flow", "exec");
            assertThat(lockDir.list().length, is(greaterThan(start)));
        } finally {
            lock.close();
        }
        assertThat(lockDir.list().length, is(start));
    }

    /**
     * Configure using variable.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_variable() throws Exception {
        File lockDir = folder.getRoot();
        VariableResolver var = new VariableResolver(Collections.singletonMap(
                "LOCATION", lockDir.getAbsolutePath()));
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, "${__INVALID__}");

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, new ProfileContext(getClass().getClassLoader(), var));
        profile.newInstance();
    }

    /**
     * Lock provider for world.
     * @throws Exception if failed
     */
    @Test
    public void world() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, folder.getRoot().getAbsolutePath());
        conf.put(ExecutionLockProvider.KEY_SCOPE, ExecutionLock.Scope.WORLD.getSymbol());

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance1 = profile.newInstance();
        ExecutionLockProvider instance2 = profile.newInstance();

        ExecutionLock lock = instance1.newInstance("batch1");
        try {
            try {
                instance2.newInstance("batch1");
                fail("cannot run same batch");
            } catch (IOException e) {
                // ok.
            }
            try {
                instance2.newInstance("batch2");
                fail("cannot run any batch");
            } catch (IOException e) {
                // ok.
            }
            // can acquire any flow/exec lock in same
            lock.beginFlow("flow1", "exec1");
            lock.beginFlow("flow2", "exec1");
            try {
                lock.beginFlow("flow1", "exec1");
                fail("cannot reentrant same flow");
            } catch (IOException e) {
                // ok.
            }
        } finally {
            lock.close();
        }
    }

    /**
     * Lock provider for batch.
     * @throws Exception if failed
     */
    @Test
    public void batch() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, folder.getRoot().getAbsolutePath());
        conf.put(ExecutionLockProvider.KEY_SCOPE, ExecutionLock.Scope.BATCH.getSymbol());

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance1 = profile.newInstance();
        ExecutionLockProvider instance2 = profile.newInstance();

        ExecutionLock lock = instance1.newInstance("batch1");
        try {
            lock.beginFlow("flow1", "exec1");
            try {
                instance2.newInstance("batch1");
                fail("cannot run same batch");
            } catch (IOException e) {
                // ok.
            }
            ExecutionLock other = instance2.newInstance("batch2");
            try {
                // can acquire any flow/exec lock
                other.beginFlow("flow2", "exec1");
                other.endFlow("flow2", "exec1");
                other.beginFlow("flow1", "exec2");
                other.endFlow("flow1", "exec2");
                other.beginFlow("flow2", "exec2");
                other.endFlow("flow2", "exec2");
            } finally {
                other.close();
            }
        } finally {
            lock.close();
        }
    }

    /**
     * Lock provider for flow.
     * @throws Exception if failed
     */
    @Test
    public void flow() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, folder.getRoot().getAbsolutePath());
        conf.put(ExecutionLockProvider.KEY_SCOPE, ExecutionLock.Scope.FLOW.getSymbol());

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance1 = profile.newInstance();
        ExecutionLockProvider instance2 = profile.newInstance();

        ExecutionLock lock = instance1.newInstance("batch1");
        try {
            lock.beginFlow("flow1", "exec1");
            ExecutionLock other = instance2.newInstance("batch1");
            try {
                // can acquire other flow lock
                other.beginFlow("flow2", "exec1");
                other.endFlow("flow2", "exec1");
                other.beginFlow("flow2", "exec2");
                other.endFlow("flow2", "exec2");

                try {
                    other.beginFlow("flow1", "exec2");
                    fail("cannot run same flow");
                } catch (IOException e) {
                    // ok.
                }
            } finally {
                other.close();
            }

            other = instance2.newInstance("batch2");
            try {
                // can acquire any flow lock if batch is different
                other.beginFlow("flow1", "exec1");
            } finally {
                other.close();
            }
        } finally {
            lock.close();
        }
    }

    /**
     * Lock provider for execution.
     * @throws Exception if failed
     */
    @Test
    public void execution() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, folder.getRoot().getAbsolutePath());
        conf.put(ExecutionLockProvider.KEY_SCOPE, ExecutionLock.Scope.EXECUTION.getSymbol());

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance1 = profile.newInstance();
        ExecutionLockProvider instance2 = profile.newInstance();

        ExecutionLock lock = instance1.newInstance("batch1");
        try {
            lock.beginFlow("flow1", "exec1");
            ExecutionLock other = instance2.newInstance("batch1");
            try {
                // can acquire other flow lock
                other.beginFlow("flow2", "exec1");
                other.endFlow("flow2", "exec1");
                other.beginFlow("flow2", "exec2");
                other.endFlow("flow2", "exec2");

                // can acquire other execution lock
                other.beginFlow("flow1", "exec2");
                other.endFlow("flow1", "exec2");

                try {
                    other.beginFlow("flow1", "exec1");
                    fail("cannot run same execution");
                } catch (IOException e) {
                    // ok.
                }
            } finally {
                other.close();
            }

            other = instance2.newInstance("batch2");
            try {
                // can acquire any flow lock if batch is different
                other.beginFlow("flow1", "exec1");
            } finally {
                other.close();
            }
        } finally {
            lock.close();
        }
    }

    /**
     * Missing directory config.
     * @throws Exception if failed
     */
    @Test
    public void missing_directory() throws Exception {
        File lockDir = folder.newFolder("missing");
        Assume.assumeThat(lockDir.delete(), is(true));

        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, lockDir.getAbsolutePath());

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance = profile.newInstance();
        ExecutionLock lock = instance.newInstance("batch");
        try {
            lock.beginFlow("a", "b");
            lock.endFlow("a", "b");
        } finally {
            lock.close();
        }
    }

    /**
     * Missing directory config.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void missing_directory_config() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance = profile.newInstance();
        instance.newInstance("batch");
    }

    /**
     * Missing directory config.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_directory() throws Exception {
        File lockDir = folder.newFile("INVALID");

        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicLockProvider.KEY_DIRECTORY, lockDir.getAbsolutePath());

        ServiceProfile<ExecutionLockProvider> profile = new ServiceProfile<ExecutionLockProvider>(
                "testing", BasicLockProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));
        ExecutionLockProvider instance = profile.newInstance();
        instance.newInstance("batch");
    }
}
