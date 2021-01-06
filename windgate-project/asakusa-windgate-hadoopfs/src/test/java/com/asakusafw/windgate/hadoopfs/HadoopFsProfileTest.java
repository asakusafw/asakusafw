/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.resource.ResourceProfile;

/**
 * Test for {@link HadoopFsProfile}.
 */
public class HadoopFsProfileTest {

    private Configuration hadoopConf;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        hadoopConf = new ConfigurationProvider().newInstance();
    }

    /**
     * Converts a simple resource profile.
     * @throws Exception if failed
     */
    @Test
    public void convert() throws Exception {
        hadoopConf.set("fs.default.name", "file:/");

        Map<String, String> conf = new HashMap<>();
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile profile = HadoopFsProfile.convert(hadoopConf, resourceProfile);
        assertThat(profile.getResourceName(), is("testing"));
        assertThat(profile.getBasePath().toUri().getScheme(), is("file"));
    }

    /**
     * Converts a resource profile with base path.
     * @throws Exception if failed
     */
    @Test
    public void convert_basePath() throws Exception {
        File current = new File(".").getAbsoluteFile().getCanonicalFile();

        Map<String, String> conf = new HashMap<>();
        conf.put(HadoopFsProfile.KEY_BASE_PATH, current.toURI().toString());
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile profile = HadoopFsProfile.convert(hadoopConf, resourceProfile);
        assertThat(profile.getResourceName(), is("testing"));
        assertThat(profile.getBasePath(), is(new Path(current.toURI())));
    }

    /**
     * Converts a resource profile with base path.
     * @throws Exception if failed
     */
    @Test
    public void convert_basePath_relative() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(HadoopFsProfile.KEY_BASE_PATH, "target");
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile profile = HadoopFsProfile.convert(hadoopConf, resourceProfile);
        assertThat(profile.getResourceName(), is("testing"));
        assertThat(profile.getBasePath().getName(), is("target"));
    }

    /**
     * Converts a resource profile with base path.
     * @throws Exception if failed
     */
    @Test
    public void convert_basePath_parameterize() throws Exception {
        File current = new File(".").getAbsoluteFile().getCanonicalFile();

        Map<String, String> conf = new HashMap<>();
        conf.put(HadoopFsProfile.KEY_BASE_PATH, current.toURI().toString() + "/${var}");

        Map<String, String> env = new HashMap<>();
        env.put("var", "replacement");
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList(env)),
                conf);

        HadoopFsProfile profile = HadoopFsProfile.convert(hadoopConf, resourceProfile);
        assertThat(profile.getResourceName(), is("testing"));
        assertThat(profile.getBasePath().getName(), is("replacement"));
    }

    /**
     * Converts a resource profile with base path which includes undefined variables.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_basePath_unresolved() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(HadoopFsProfile.KEY_BASE_PATH, "${__UNDEFINED__}");
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile.convert(hadoopConf, resourceProfile);
    }

    /**
     * Converts a resource profile with base path which includes undefined variables.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void convert_basePath_invalid() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(HadoopFsProfile.KEY_BASE_PATH, "INVALIDFS:UNKNOWN");
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile.convert(hadoopConf, resourceProfile);
    }

    /**
     * Converts a resource profile with compression codec.
     * @throws Exception if failed
     */
    @Test
    public void convert_compression() throws Exception {
        Map<String, String> conf = new HashMap<>();
        conf.put(HadoopFsProfile.KEY_COMPRESSION, DefaultCodec.class.getName());
        ResourceProfile resourceProfile = new ResourceProfile(
                "testing",
                HadoopFsProvider.class,
                new ProfileContext(getClass().getClassLoader(), new ParameterList()),
                conf);

        HadoopFsProfile profile = HadoopFsProfile.convert(hadoopConf, resourceProfile);
        assertThat(profile.getResourceName(), is("testing"));
        // may occur warn log
    }
}
