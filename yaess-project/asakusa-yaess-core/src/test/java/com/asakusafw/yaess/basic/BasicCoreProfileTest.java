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
package com.asakusafw.yaess.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.yaess.core.CoreProfile;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Test for {@link BasicCoreProfile}.
 */
public class BasicCoreProfileTest {

    /**
     * Get version.
     * @throws Exception if failed
     */
    @Test
    public void version() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(CoreProfile.KEY_VERSION, "TESTING");

        ServiceProfile<CoreProfile> profile = new ServiceProfile<CoreProfile>(
                "testing", BasicCoreProfile.class, conf, ProfileContext.system(getClass().getClassLoader()));
        CoreProfile instance = profile.newInstance();

        assertThat(instance.getVersion(), is("TESTING"));
    }

    /**
     * Version not specified.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void version_missing() throws Exception {
        Map<String, String> conf = new HashMap<>();
        ServiceProfile<CoreProfile> profile = new ServiceProfile<CoreProfile>(
                "testing", BasicCoreProfile.class, conf, ProfileContext.system(getClass().getClassLoader()));
        profile.newInstance();
    }
}
