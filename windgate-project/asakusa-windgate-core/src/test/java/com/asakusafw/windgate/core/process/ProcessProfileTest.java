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
package com.asakusafw.windgate.core.process;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Properties;

import org.junit.Test;

import com.asakusafw.windgate.core.BaseProfile;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.session.SessionProfile;

/**
 * Test for {@link ProcessProfile}.
 */
public class ProcessProfileTest {

    /**
     * Test method for {@link ProcessProfile#loadFrom(Properties, ProfileContext)}.
     */
    @Test
    public void loadFrom() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(1));
        ProcessProfile p1 = find(profiles, "testing");
        assertThat(p1.getName(), is("testing"));
        assertThat(p1.getProviderClass(), is((Object) BasicProcessProvider.class));
        assertThat(p1.getConfiguration().size(), is(0));
    }

    /**
     * Test method for {@link ProcessProfile#loadFrom(Properties, ProfileContext)}.
     */
    @Test
    public void loadFrom_configured() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conf1", "aaa");
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conf2", "bbb");

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(1));
        ProcessProfile p1 = find(profiles, "testing");
        assertThat(p1.getName(), is("testing"));
        assertThat(p1.getProviderClass(), is((Object) BasicProcessProvider.class));
        assertThat(p1.getConfiguration().size(), is(2));
        assertThat(p1.getConfiguration().get("conf1"), is("aaa"));
        assertThat(p1.getConfiguration().get("conf2"), is("bbb"));
    }

    /**
     * Test method for {@link ProcessProfile#loadFrom(Properties, ProfileContext)}.
     */
    @Test
    public void loadFrom_multiple() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing1", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing1" + BaseProfile.QUALIFIER + "conf", "aaa");
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing2", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing2" + BaseProfile.QUALIFIER + "conf", "bbb");
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing3", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing3" + BaseProfile.QUALIFIER + "conf", "ccc");

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(3));

        ProcessProfile p1 = find(profiles, "testing1");
        assertThat(p1.getName(), is("testing1"));
        assertThat(p1.getProviderClass(), is((Object) BasicProcessProvider.class));
        assertThat(p1.getConfiguration().size(), is(1));
        assertThat(p1.getConfiguration().get("conf"), is("aaa"));

        ProcessProfile p2 = find(profiles, "testing2");
        assertThat(p2.getName(), is("testing2"));
        assertThat(p2.getProviderClass(), is((Object) BasicProcessProvider.class));
        assertThat(p2.getConfiguration().size(), is(1));
        assertThat(p2.getConfiguration().get("conf"), is("bbb"));

        ProcessProfile p3 = find(profiles, "testing3");
        assertThat(p3.getName(), is("testing3"));
        assertThat(p3.getProviderClass(), is((Object) BasicProcessProvider.class));
        assertThat(p3.getConfiguration().size(), is(1));
        assertThat(p3.getConfiguration().get("conf"), is("ccc"));
    }

    /**
     * Attempts to load properties with invalid name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_name() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "@INVALID", BasicProcessProvider.class.getName());

        ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Attempts to load properties with invalid provider.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_provider() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", String.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conf", "aaa");

        ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Attempts to load properties without provider.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_missing_provider() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conf", "aaa");

        ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Test method for {@link ProcessProfile#storeTo(java.util.Properties)}.
     */
    @Test
    public void storeTo() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        for (ProcessProfile profile : profiles) {
            profile.storeTo(restored);
        }
        assertThat(restored, is(p));
    }

    /**
     * Builds properties from a profile with configurations.
     */
    @Test
    public void storeTo_configured() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conf1", "aaa");
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conf2", "bbb");

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        for (ProcessProfile profile : profiles) {
            profile.storeTo(restored);
        }
        assertThat(restored, is(p));
    }

    /**
     * Builds properties from multiple profiles.
     */
    @Test
    public void storeTo_multiple() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing1", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing1" + BaseProfile.QUALIFIER + "conf", "aaa");
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing2", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing2" + BaseProfile.QUALIFIER + "conf", "bbb");
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing3", BasicProcessProvider.class.getName());
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing3" + BaseProfile.QUALIFIER + "conf", "ccc");

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        for (ProcessProfile profile : profiles) {
            profile.storeTo(restored);
        }
        assertThat(restored, is(p));
    }

    /**
     * Attempts to build properties but conflict a provider.
     */
    @Test(expected = IllegalArgumentException.class)
    public void storeTo_conflict_provider() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        restored.setProperty(ProcessProfile.KEY_PREFIX + "testing", "conflict");
        for (ProcessProfile profile : profiles) {
            profile.storeTo(restored);
        }
    }

    /**
     * Attempts to build properties but conflict a configuration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void storeTo_conflict_configuration() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        restored.setProperty(ProcessProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conflict", "conflict");
        for (ProcessProfile profile : profiles) {
            profile.storeTo(restored);
        }
    }

    /**
     * Build properties with orthogonal keys.
     */
    @Test
    public void storeTo_orthogonal() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        restored.setProperty(ProcessProfile.KEY_PREFIX + "orthogonal" + BaseProfile.QUALIFIER + "conf", "conf");
        for (ProcessProfile profile : profiles) {
            profile.storeTo(restored);
        }

        restored.remove(ProcessProfile.KEY_PREFIX + "orthogonal" + BaseProfile.QUALIFIER + "conf");
        assertThat(restored, is(p));
    }

    /**
     * Test method for {@link ProcessProfile#removeCorrespondingKeys(java.util.Properties)}.
     */
    @Test
    public void removeCorrespondingKeys() {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "abc", "process");
        p.setProperty(SessionProfile.KEY_PREFIX + "abc", "session");

        ProcessProfile.removeCorrespondingKeys(p);
        Properties answer = new Properties();
        answer.setProperty(SessionProfile.KEY_PREFIX + "abc", "session");
        assertThat(p, is(answer));
    }

    /**
     * Test method for {@link ProcessProfile#createProvider()}.
     * @throws Exception if failed
     */
    @Test
    public void createProvider() throws Exception {
        Properties p = new Properties();
        p.setProperty(ProcessProfile.KEY_PREFIX + "testing", BasicProcessProvider.class.getName());

        Collection<? extends ProcessProfile> profiles = ProcessProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(1));
        ProcessProfile r1 = find(profiles, "testing");

        ProcessProvider provider = r1.createProvider();
        assertThat(provider, is(instanceOf(BasicProcessProvider.class)));
    }

    private ProcessProfile find(Collection<? extends ProcessProfile> profiles, String name) {
        for (ProcessProfile profile : profiles) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        throw new AssertionError(name);
    }
}
