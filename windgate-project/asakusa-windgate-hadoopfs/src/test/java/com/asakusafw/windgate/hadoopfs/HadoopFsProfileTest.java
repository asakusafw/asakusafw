/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.windgate.hadoopfs;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.junit.Test;

import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.resource.ResourceProfile;

/**
 * Test for {@link HadoopFsProfile}.
 */
public class HadoopFsProfileTest {

    /**
     * Converts a simple resource profile.
     */
    @Test
    public void convert() {
        Map<String, String> conf = new HashMap<String, String>();
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile profile = HadoopFsProfile.convert(new Configuration(), resourceProfile);
        assertThat(profile.getResourceName(), is("testing"));
        assertThat(profile.getCompressionCodec(), is(nullValue()));
    }

    /**
     * Converts a resource profile with compression codec.
     */
    @Test
    public void convert_compression() {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(HadoopFsProfile.KEY_COMPRESSION, DefaultCodec.class.getName());
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile profile = HadoopFsProfile.convert(new Configuration(), resourceProfile);
        assertThat(profile.getResourceName(), is("testing"));
        assertThat(profile.getCompressionCodec(), instanceOf(DefaultCodec.class));
    }

    /**
     * Attempts to convert a resource profile with invalid compression codec.
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_invalid_compression() {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(HadoopFsProfile.KEY_COMPRESSION, "INVALID");
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile.convert(new Configuration(), resourceProfile);
    }
}
