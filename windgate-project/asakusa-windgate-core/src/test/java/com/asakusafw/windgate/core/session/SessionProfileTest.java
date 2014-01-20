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
package com.asakusafw.windgate.core.session;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.file.session.FileSessionProvider;

/**
 * Test for {@link SessionProfile}.
 */
public class SessionProfileTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test method for {@link SessionProfile#loadFrom(java.util.Properties, java.lang.ClassLoader)}.
     */
    @Test
    public void loadFrom() {
        Properties p = new Properties();
        p.setProperty(SessionProfile.KEY_PROVIDER, FileSessionProvider.class.getName());

        SessionProfile profile = SessionProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profile.getProviderClass(), is((Object) FileSessionProvider.class));
        assertThat(profile.getConfiguration().size(), is(0));
    }

    /**
     * Test method for {@link SessionProfile#loadFrom(java.util.Properties, java.lang.ClassLoader)}.
     */
    @Test
    public void loadFrom_configure() {
        String path = new File("target/testing/" + getClass().getSimpleName()).getAbsolutePath();

        Properties p = new Properties();
        p.setProperty(SessionProfile.KEY_PROVIDER, FileSessionProvider.class.getName());
        p.setProperty(SessionProfile.KEY_PREFIX + FileSessionProvider.KEY_DIRECTORY, path);

        SessionProfile profile = SessionProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profile.getProviderClass(), is((Object) FileSessionProvider.class));
        assertThat(profile.getConfiguration().size(), is(1));
        assertThat(profile.getConfiguration().get(FileSessionProvider.KEY_DIRECTORY), is(path));
    }

    /**
     * Test method for {@link SessionProfile#storeTo(java.util.Properties)}.
     */
    @Test
    public void storeTo() {
        String path = new File("target/testing/" + getClass().getSimpleName()).getAbsolutePath();

        Properties p = new Properties();
        p.setProperty(SessionProfile.KEY_PROVIDER, FileSessionProvider.class.getName());
        p.setProperty(SessionProfile.KEY_PREFIX + FileSessionProvider.KEY_DIRECTORY, path);

        SessionProfile profile = SessionProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        profile.storeTo(restored);
        assertThat(restored, is(p));
    }

    /**
     * Test method for {@link SessionProfile#storeTo(java.util.Properties)}.
     */
    @Test
    public void storeTo_configure() {
        Properties p = new Properties();
        p.setProperty(SessionProfile.KEY_PROVIDER, FileSessionProvider.class.getName());

        SessionProfile profile = SessionProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        profile.storeTo(restored);
        assertThat(restored, is(p));
    }

    /**
     * Test method for {@link SessionProfile#removeCorrespondingKeys(java.util.Properties)}.
     */
    @Test
    public void removeCorrespondingKeys() {
        Properties p = new Properties();
        p.setProperty(SessionProfile.KEY_PROVIDER, "aaa");
        p.setProperty(SessionProfile.KEY_PROVIDER + "_", "bbb");
        p.setProperty(SessionProfile.KEY_PREFIX + "", "");
        p.setProperty(SessionProfile.KEY_PREFIX + "ccc", "ccc");

        SessionProfile.removeCorrespondingKeys(p);
        Properties answer = new Properties();
        answer.setProperty(SessionProfile.KEY_PROVIDER + "_", "bbb");
        assertThat(p, is(answer));
    }

    /**
     * Test method for {@link SessionProfile#createProvider()}.
     * @throws Exception if failed
     */
    @Test
    public void createProvider() throws Exception {
        File path = folder.newFolder("session");
        Assume.assumeTrue(path.delete());

        Properties p = new Properties();
        p.setProperty(SessionProfile.KEY_PROVIDER, FileSessionProvider.class.getName());
        p.setProperty(SessionProfile.KEY_PREFIX + FileSessionProvider.KEY_DIRECTORY, path.getPath());

        SessionProfile profile = SessionProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        SessionProvider provider = profile.createProvider();
        SessionMirror session = provider.create("hello");
        session.close();

        assertThat(path.isDirectory(), is(true));
    }
}
