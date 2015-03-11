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
package com.asakusafw.runtime.stage.launcher;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.GenericOptionsParser;
import org.junit.After;
import org.junit.Test;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;

/**
 * Test for {@link LauncherOptionsParser}.
 */
public class LauncherOptionsParserTest extends LauncherTestRoot {

    private final Configuration conf = new ConfigurationProvider().newInstance();

    private final List<LauncherOptions> optionsManager = new ArrayList<LauncherOptions>();

    /**
     * Disposes {@link LauncherOptions}.
     * @throws Exception if failed
     */
    @After
    public void after() throws Exception {
        for (LauncherOptions options : optionsManager) {
            ClassLoader loader = options.getApplicationClassLoader();
            if (loader instanceof Closeable) {
                ((Closeable) loader).close();
            }
        }
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        LauncherOptions options = parse(new String[] {
                MockTool.class.getName(),
        });
        assertThat(options.getApplicationClass(), is((Object) MockTool.class));
        assertThat(options.getApplicationArguments(), hasSize(0));
        assertThat(options.getApplicationCacheDirectories(), hasSize(0));
    }

    /**
     * w/ application arguments.
     * @throws Exception if failed
     */
    @Test
    public void w_arguments() throws Exception {
        LauncherOptions options = parse(new String[] {
                MockTool.class.getName(),
                "hello1",
                "hello2",
                "hello3",
        });
        assertThat(options.getApplicationClass(), is((Object) MockTool.class));
        assertThat(options.getApplicationArguments(), hasItems("hello1", "hello2", "hello3"));
        assertThat(options.getApplicationCacheDirectories(), hasSize(0));
    }

    /**
     * w/ libjars.
     * @throws Exception if failed
     */
    @Test
    public void w_libjars() throws Exception {
        File lib = putFile("dummy.jar");
        LauncherOptions options = parse(new String[] {
                MockTool.class.getName(),
                LauncherOptionsParser.KEY_ARG_LIBRARIES,
                lib.getPath(),
        });
        assertClasspath(options.getApplicationClassLoader().getURLs(), "testing");
        assertThat(lib, is(inClasspath(options.getApplicationClassLoader().getURLs())));

        assertClasspath(GenericOptionsParser.getLibJars(conf), "testing");
        assertThat(lib, is(inClasspath(GenericOptionsParser.getLibJars(conf))));

        JobConf jc = new JobConf(conf);
        assertThat(jc.getJar(), is(nullValue()));
    }

    /**
     * w/ libjars and application arguments.
     * @throws Exception if failed
     */
    @Test
    public void w_libjars_arguments() throws Exception {
        File lib = putFile("dummy.jar");
        LauncherOptions options = parse(new String[] {
                MockTool.class.getName(),
                LauncherOptionsParser.KEY_ARG_LIBRARIES,
                lib.getPath(),
                "hello1",
                "hello2",
                "hello3",
        });
        assertThat(options.getApplicationClass(), is((Object) MockTool.class));
        assertThat(options.getApplicationArguments(), hasItems("hello1", "hello2", "hello3"));
        assertThat(options.getApplicationCacheDirectories(), hasSize(0));
    }

    /**
     * w/ libjars.
     * @throws Exception if failed
     */
    @Test
    public void w_libjars_w_cache() throws Exception {
        File cacheRepo = folder.newFolder();
        conf.set(LauncherOptionsParser.KEY_CACHE_REPOSITORY, cacheRepo.toURI().toString());
        File lib = putFile("dummy.jar");
        LauncherOptions options = parse(new String[] {
                MockTool.class.getName(),
                LauncherOptionsParser.KEY_ARG_LIBRARIES,
                lib.getPath(),
        });
        assertClasspath(options.getApplicationClassLoader().getURLs(), "testing");
        assertThat(lib, is(inClasspath(options.getApplicationClassLoader().getURLs())));

        assertClasspath(GenericOptionsParser.getLibJars(conf), "testing");
        assertThat(lib, is(not(inClasspath(GenericOptionsParser.getLibJars(conf)))));
    }

    /**
     * w/ libjars with disabled cache.
     * @throws Exception if failed
     */
    @Test
    public void w_libjars_disabled() throws Exception {
        File cacheRepo = folder.newFolder();
        conf.set(LauncherOptionsParser.KEY_CACHE_REPOSITORY, cacheRepo.toURI().toString());
        conf.setBoolean(LauncherOptionsParser.KEY_CACHE_ENABLED, false);
        File lib = putFile("dummy.jar");
        LauncherOptions options = parse(new String[] {
                MockTool.class.getName(),
                LauncherOptionsParser.KEY_ARG_LIBRARIES,
                lib.getPath(),
        });
        assertClasspath(options.getApplicationClassLoader().getURLs(), "testing");
        assertThat(lib, is(inClasspath(options.getApplicationClassLoader().getURLs())));

        assertClasspath(GenericOptionsParser.getLibJars(conf), "testing");
        assertThat(lib, is(inClasspath(GenericOptionsParser.getLibJars(conf))));
    }

    /**
     * w/ libjars.
     * @throws Exception if failed
     */
    @Test
    public void w_libjars_w_temporary() throws Exception {
        File cacheRepo = folder.newFolder();
        conf.set(LauncherOptionsParser.KEY_CACHE_REPOSITORY, cacheRepo.toURI().toString());

        File temporary = folder.newFolder();
        conf.set(LauncherOptionsParser.KEY_CACHE_TEMPORARY, temporary.getPath());

        File lib = putFile("dummy.jar");
        parse(new String[] {
                MockTool.class.getName(),
                LauncherOptionsParser.KEY_ARG_LIBRARIES,
                lib.getPath(),
        });
        assertThat(temporary.list(), arrayWithSize(greaterThan(0)));
    }

    /**
     * w/o arguments.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_wo_arguments() throws Exception {
        parse();
    }

    /**
     * w/ missing application class.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_w_missing_app() throws Exception {
        parse(MockTool.class.getName() + "__MISSING__");
    }

    /**
     * w/ invalid application class.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_w_invalid_app() throws Exception {
        parse(String.class.getName());
    }

    /**
     * w/ missing library.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_w_missing_library() throws Exception {
        parse(new String[] {
                MockTool.class.getName(),
                LauncherOptionsParser.KEY_ARG_LIBRARIES,
                new File(folder.getRoot(), "__MISSING__.jar").getPath(),
        });
    }

    private LauncherOptions parse(String... args) throws IOException, InterruptedException {
        LauncherOptions options = LauncherOptionsParser.parse(conf, args);
        optionsManager.add(options);
        return options;
    }
}
