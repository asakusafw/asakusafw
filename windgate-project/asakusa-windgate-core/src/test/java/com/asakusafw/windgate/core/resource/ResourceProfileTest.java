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
package com.asakusafw.windgate.core.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.junit.Test;

import com.asakusafw.windgate.core.BaseProfile;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.session.SessionProfile;

/**
 * Test for {@link ResourceProfile}.
 */
public class ResourceProfileTest {

    /**
     * Test method for {@link ProcessProfile#loadFrom(Properties, ProfileContext)}.
     */
    @Test
    public void loadFrom() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(1));
        ResourceProfile r1 = find(profiles, "testing");

        assertThat(r1.getName(), is("testing"));
        assertThat(r1.getProviderClass(), is((Object) MockResourceProvider.class));
        assertThat(r1.getConfiguration().size(), is(0));
    }

    /**
     * Loads a resource with configuration.
     */
    @Test
    public void loadFrom_configured() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "aaa", "aaa");
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "bbb", "bbb");

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(1));
        ResourceProfile r1 = find(profiles, "testing");

        assertThat(r1.getName(), is("testing"));
        assertThat(r1.getProviderClass(), is((Object) MockResourceProvider.class));
        assertThat(r1.getConfiguration().size(), is(2));
        assertThat(r1.getConfiguration().get("aaa"), is("aaa"));
        assertThat(r1.getConfiguration().get("bbb"), is("bbb"));
    }

    /**
     * Loads multiple resources.
     */
    @Test
    public void loadFrom_multiple() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing1", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing1" + BaseProfile.QUALIFIER + "conf", "p1");
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing2", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing2" + BaseProfile.QUALIFIER + "conf", "p2");
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing3", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing3" + BaseProfile.QUALIFIER + "conf", "p3");

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(3));

        ResourceProfile r1 = find(profiles, "testing1");
        assertThat(r1.getName(), is("testing1"));
        assertThat(r1.getProviderClass(), is((Object) MockResourceProvider.class));
        assertThat(r1.getConfiguration().size(), is(1));
        assertThat(r1.getConfiguration().get("conf"), is("p1"));

        ResourceProfile r2 = find(profiles, "testing2");
        assertThat(r2.getName(), is("testing2"));
        assertThat(r2.getProviderClass(), is((Object) MockResourceProvider.class));
        assertThat(r2.getConfiguration().size(), is(1));
        assertThat(r2.getConfiguration().get("conf"), is("p2"));

        ResourceProfile r3 = find(profiles, "testing3");
        assertThat(r3.getName(), is("testing3"));
        assertThat(r3.getProviderClass(), is((Object) MockResourceProvider.class));
        assertThat(r3.getConfiguration().size(), is(1));
        assertThat(r3.getConfiguration().get("conf"), is("p3"));
    }

    /**
     * Attempts to load properties with invalid resource name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_name() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "@INVALIDNAME", MockResourceProvider.class.getName());
        ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Attempts to load properties with invalid resource provider.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_provider() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "invalid", "INVALID-CLASS-NAME");
        ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Attempts to load properties but resource name is missing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_missing_provider() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "aaa", "aaa");
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "bbb", "bbb");
        ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Test method for {@link ResourceProfile#storeTo(java.util.Properties)}.
     */
    @Test
    public void storeTo() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        for (ResourceProfile profile : profiles) {
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
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "aaa", "aaa");
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "bbb", "bbb");

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        for (ResourceProfile profile : profiles) {
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
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing1", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing1" + BaseProfile.QUALIFIER + "conf", "p1");
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing2", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing2" + BaseProfile.QUALIFIER + "conf", "p2");
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing3", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing3" + BaseProfile.QUALIFIER + "conf", "p3");

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        for (ResourceProfile profile : profiles) {
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
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        restored.setProperty(ResourceProfile.KEY_PREFIX + "testing", "conflict");
        for (ResourceProfile profile : profiles) {
            profile.storeTo(restored);
        }
    }

    /**
     * Attempts to build properties but conflict a configuration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void storeTo_conflict_configuration() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        restored.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "conflict", "conflict");
        for (ResourceProfile profile : profiles) {
            profile.storeTo(restored);
        }
    }

    /**
     * Build properties with orthogonal keys.
     */
    @Test
    public void storeTo_orthogonal() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        Properties restored = new Properties();
        restored.setProperty(ResourceProfile.KEY_PREFIX + "orthogonal" + BaseProfile.QUALIFIER + "conf", "conf");
        for (ResourceProfile profile : profiles) {
            profile.storeTo(restored);
        }

        restored.remove(ResourceProfile.KEY_PREFIX + "orthogonal" + BaseProfile.QUALIFIER + "conf");
        assertThat(restored, is(p));
    }

    /**
     * Test method for {@link ResourceProfile#removeCorrespondingKeys(java.util.Properties)}.
     */
    @Test
    public void removeCorrespondingKeys() {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "abc", "resource");
        p.setProperty(SessionProfile.KEY_PREFIX + "abc", "session");

        ResourceProfile.removeCorrespondingKeys(p);
        Properties answer = new Properties();
        answer.setProperty(SessionProfile.KEY_PREFIX + "abc", "session");
        assertThat(p, is(answer));
    }

    /**
     * Test method for {@link ResourceProfile#createProvider()}.
     * @throws Exception if failed
     */
    @Test
    public void createProvider() throws Exception {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(1));
        ResourceProfile r1 = find(profiles, "testing");

        ResourceProvider provider = r1.createProvider();
        assertThat(provider, is(instanceOf(MockResourceProvider.class)));
        MockResourceProvider mock = (MockResourceProvider) provider;
        assertThat(mock.configuredProfile.getName(), is("testing"));
        assertThat(mock.configuredProfile.getConfiguration().size(), is(0));
    }

    /**
     * Test method for {@link ResourceProfile#createProvider()}.
     * @throws Exception if failed
     */
    @Test
    public void createProvider_configured() throws Exception {
        Properties p = new Properties();
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing", MockResourceProvider.class.getName());
        p.setProperty(ResourceProfile.KEY_PREFIX + "testing" + BaseProfile.QUALIFIER + "aaa", "aaa");

        Collection<? extends ResourceProfile> profiles = ResourceProfile.loadFrom(p, ProfileContext.system(getClass().getClassLoader()));
        assertThat(profiles.size(), is(1));
        ResourceProfile r1 = find(profiles, "testing");

        ResourceProvider provider = r1.createProvider();
        assertThat(provider, is(instanceOf(MockResourceProvider.class)));
        MockResourceProvider mock = (MockResourceProvider) provider;
        assertThat(mock.configuredProfile.getName(), is("testing"));
        assertThat(mock.configuredProfile.getConfiguration(), is(Collections.singletonMap("aaa", "aaa")));
    }

    private ResourceProfile find(Collection<? extends ResourceProfile> profiles, String name) {
        for (ResourceProfile profile : profiles) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        throw new AssertionError(name);
    }
}
