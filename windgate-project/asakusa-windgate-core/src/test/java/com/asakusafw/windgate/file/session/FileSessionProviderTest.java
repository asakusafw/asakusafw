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
package com.asakusafw.windgate.file.session;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.session.SessionException;
import com.asakusafw.windgate.core.session.SessionException.Reason;
import com.asakusafw.windgate.core.session.SessionMirror;
import com.asakusafw.windgate.core.session.SessionProfile;

/**
 * Test for {@link FileSessionProvider}.
 */
public class FileSessionProviderTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private FileSessionProvider provider;

    /**
     * Sets up this tests.
     * @throws Throwable if failed
     */
    @Before
    public void setUp() throws Throwable {
        provider = new FileSessionProvider();
        provider.configure(new SessionProfile(
                FileSessionProvider.class,
                ProfileContext.system(FileSessionProvider.class.getClassLoader()),
                Collections.singletonMap(
                        FileSessionProvider.KEY_DIRECTORY,
                        folder.getRoot().getAbsolutePath())));
    }

    /**
     * Creates a new session.
     * @throws Exception if failed
     */
    @Test
    public void create() throws Exception {
        createAndClose("testing");
    }

    /**
     * Creates multiple sessions.
     * @throws Exception if failed
     */
    @Test
    public void create_multiple() throws Exception {

        try (SessionMirror session1 = provider.create("testing1")){
            createAndClose("testing2");
        }
    }

    /**
     * Creates a new session but the specified session already exists.
     * @throws Exception if failed
     */
    @Test
    public void create_exists() throws Exception {
        createAndClose("testing");
        try {
            createAndClose("testing");
            fail();
        } catch (SessionException e) {
            assertThat(e.getSessionId(), is("testing"));
            assertThat(e.getReason(), is(Reason.ALREADY_EXIST));
        }
    }

    /**
     * Creates a new session but is acquired.
     * @throws Exception if failed
     */
    @Test
    public void create_acquired() throws Exception {
        try (SessionMirror session = provider.create("testing")) {
            try {
                createAndClose("testing");
                fail();
            } catch (SessionException e) {
                assertThat(e.getSessionId(), is("testing"));
                assertThat(e.getReason(), is(Reason.ACQUIRED));
            }
        }
    }

    /**
     * Opens a created session.
     * @throws Exception if failed
     */
    @Test
    public void open() throws Exception {
        createAndClose("testing");
        openAndClose("testing");
    }

    /**
     * Attempts to open a missing session.
     * @throws Exception if failed
     */
    @Test
    public void open_missing() throws Exception {
        try {
            openAndClose("testing");
            fail();
        } catch (SessionException e) {
            assertThat(e.getSessionId(), is("testing"));
            assertThat(e.getReason(), is(Reason.NOT_EXIST));
            List<String> all = new ArrayList<>(provider.getCreatedIds());
            assertThat(all.isEmpty(), is(true));
        }
    }

    /**
     * Opens a session but is acquired by the creater.
     * @throws Exception if failed
     */
    @Test
    public void open_acquired() throws Exception {
        try (SessionMirror session = provider.create("testing")) {
            try {
                openAndClose("testing");
                fail();
            } catch (SessionException e) {
                assertThat(e.getSessionId(), is("testing"));
                assertThat(e.getReason(), is(Reason.ACQUIRED));
            }
        }
    }

    /**
     * Opens a session but is acquired by the other.
     * @throws Exception if failed
     */
    @Test
    public void open_conflict() throws Exception {
        createAndClose("testing");
        try (SessionMirror other = provider.open("testing")) {
            try {
                openAndClose("testing");
                fail();
            } catch (SessionException e) {
                assertThat(e.getSessionId(), is("testing"));
                assertThat(e.getReason(), is(Reason.ACQUIRED));
            }
        }
    }

    /**
     * Enumerates IDs.
     * @throws Exception if failed
     */
    @Test
    public void getIds() throws Exception {
        createAndClose("testing1");
        try (SessionMirror session2 = provider.create("testing2")) {
            session2.complete();
        }
        createAndClose("testing3");

        List<String> all = new ArrayList<>(provider.getCreatedIds());
        assertThat(all, hasItems("testing1", "testing3"));
        assertThat(all.size(), is(2));
    }

    /**
     * Deletes a created session.
     * @throws Exception if failed
     */
    @Test
    public void delete() throws Exception {
        createAndClose("testing");
        provider.delete("testing");

        List<String> all = new ArrayList<>(provider.getCreatedIds());
        assertThat(all.isEmpty(), is(true));
    }

    /**
     * Attempts to delete a missing session.
     * @throws Exception if failed
     */
    @Test
    public void delete_missing() throws Exception {
        try {
            provider.delete("testing");
            fail();
        } catch (SessionException e) {
            assertThat(e.getReason(), is(SessionException.Reason.NOT_EXIST));
        }

        List<String> all = new ArrayList<>(provider.getCreatedIds());
        assertThat(all.isEmpty(), is(true));
    }

    /**
     * Check the session ID.
     * @throws Exception if failed
     */
    @Test
    public void getId() throws Exception {
        try (SessionMirror session = provider.create("testing")) {
            assertThat(session.getId(), is("testing"));
        }
    }

    /**
     * Completes the session.
     * @throws Exception if failed
     */
    @Test
    public void complete() throws Exception {
        try (SessionMirror session = provider.create("testing")) {
            session.complete();
        }
        createAndClose("testing");
    }

    /**
     * Aborts the session.
     * @throws Exception if failed
     */
    @Test
    public void abort() throws Exception {
        try (SessionMirror session = provider.create("testing")) {
            session.abort();
        }
        createAndClose("testing");
    }

    /**
     * Attempts to configure with empty config.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void config_empty() throws Exception {
        provider = new FileSessionProvider();
        provider.configure(new SessionProfile(
                FileSessionProvider.class,
                ProfileContext.system(FileSessionProvider.class.getClassLoader()),
                Collections.emptyMap()));
    }

    /**
     * Attempts to configure with unknown environment variable.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void config_unknown_variable() throws Exception {
        Assume.assumeThat(System.getenv("__UNKNOWN_VARIABLE"), is(nullValue()));
        provider = new FileSessionProvider();
        provider.configure(new SessionProfile(
                FileSessionProvider.class,
                ProfileContext.system(FileSessionProvider.class.getClassLoader()),
                Collections.singletonMap(
                        FileSessionProvider.KEY_DIRECTORY,
                        "${__UNKNOWN_VARIABLE}/hoge")));
    }

    /**
     * Attempts to configure with conflict directory.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void config_conflict_directory() throws Exception {
        File file = folder.newFile("conflict");
        provider = new FileSessionProvider();
        provider.configure(new SessionProfile(
                FileSessionProvider.class,
                ProfileContext.system(FileSessionProvider.class.getClassLoader()),
                Collections.singletonMap(
                        FileSessionProvider.KEY_DIRECTORY,
                        file.getAbsolutePath())));
    }

    private void createAndClose(String id) throws IOException, SessionException {
        try (SessionMirror session = provider.create(id)) {
            // do nothing
        }
    }

    private void openAndClose(String id) throws IOException, SessionException {
        try (SessionMirror session = provider.open(id)) {
            // do nothing
        }
    }
}
