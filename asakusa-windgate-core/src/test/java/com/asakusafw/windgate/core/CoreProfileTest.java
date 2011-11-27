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
package com.asakusafw.windgate.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

/**
 * Test for {@link CoreProfile}.
 */
public class CoreProfileTest {

    /**
     * Test method for {@link CoreProfile#loadFrom(java.util.Properties, java.lang.ClassLoader)}.
     */
    @Test
    public void loadFrom() {
        Properties p = new Properties();

        CoreProfile profile = CoreProfile.loadFrom(p, getClass().getClassLoader());
        assertThat(profile.getMaxProcesses(), is(CoreProfile.DEFAULT_MAX_PROCESSES));
    }

    /**
     * Loads properties.
     */
    @Test
    public void loadFrom_configured() {
        Properties p = new Properties();
        p.setProperty(CoreProfile.KEY_PREFIX + CoreProfile.KEY_MAX_PROCESSES, "10");

        CoreProfile profile = CoreProfile.loadFrom(p, getClass().getClassLoader());
        assertThat(profile.getMaxProcesses(), is(10));
    }

    /**
     * Attempts to load properties with invalid maxThreads.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_maxThreads() {
        Properties p = new Properties();
        p.setProperty(CoreProfile.KEY_PREFIX + CoreProfile.KEY_MAX_PROCESSES, "0");

        CoreProfile.loadFrom(p, getClass().getClassLoader());
    }

    /**
     * Attempts to load properties with invalid token.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_token() {
        Properties p = new Properties();
        p.setProperty(CoreProfile.KEY_PREFIX + CoreProfile.KEY_MAX_PROCESSES, "INVALID");

        CoreProfile.loadFrom(p, getClass().getClassLoader());
    }

    /**
     * Test for {@link CoreProfile#storeTo(Properties)}.
     */
    @Test
    public void storeTo() {
        Properties p = new Properties();
        p.setProperty(CoreProfile.KEY_PREFIX + CoreProfile.KEY_MAX_PROCESSES, "10");

        CoreProfile profile = CoreProfile.loadFrom(p, getClass().getClassLoader());
        Properties restored = new Properties();
        profile.storeTo(restored);
        assertThat(restored, is(p));
    }

    /**
     * Attempts to dump profile into invalid properties.
     */
    @Test(expected = IllegalArgumentException.class)
    public void storeTo_conflict() {
        Properties p = new Properties();
        p.setProperty(CoreProfile.KEY_PREFIX + CoreProfile.KEY_MAX_PROCESSES, "10");

        CoreProfile profile = CoreProfile.loadFrom(p, getClass().getClassLoader());
        Properties restored = new Properties();
        restored.setProperty(CoreProfile.KEY_PREFIX + "CONFLICT", "CONFLICT");
        profile.storeTo(restored);
    }

    /**
     * Test for {@link CoreProfile#removeCorrespondingKeys(Properties)}.
     */
    @Test
    public void removeCorrespondingKeys() {
        Properties p = new Properties();
        p.setProperty(CoreProfile.KEY_PREFIX + "aaa", "aaa");
        p.setProperty("unknown.aaa", "bbb");

        CoreProfile.removeCorrespondingKeys(p);

        Properties answer = new Properties();
        answer.setProperty("unknown.aaa", "bbb");
        assertThat(p, is(answer));
    }
}
