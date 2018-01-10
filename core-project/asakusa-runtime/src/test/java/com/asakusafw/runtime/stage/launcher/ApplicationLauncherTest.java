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
package com.asakusafw.runtime.stage.launcher;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;

/**
 * Test for {@link ApplicationLauncher}.
 */
public class ApplicationLauncherTest extends LauncherTestRoot {

    private final Configuration conf = new ConfigurationProvider().newInstance();

    /**
     * setup.
     */
    @Before
    @After
    public void before() {
        Bootstrap.register(null);
    }

    /**
     * simple case.
     */
    @Test
    public void simple() {
        AtomicBoolean ok = new AtomicBoolean(false);
        Bootstrap.register(new Callback() {
            @Override
            public int run(String[] args) throws Exception {
                ok.set(true);
                return 0;
            }
        });
        int status = ApplicationLauncher.exec(conf, new String[] {
                Bootstrap.class.getName(),
        });
        assertThat(status, is(0));
        assertThat(conf.getBoolean(ApplicationLauncher.KEY_LAUNCHER_USED, false), is(true));
        assertThat(ok.get(), is(true));
    }

    /**
     * w/ generic options.
     */
    @Test
    public void w_generic_options() {
        Bootstrap.register(new Callback() {
            @Override
            public int run(String[] args) throws Exception {
                assertThat(getConf().get("TESTING"), is("OK"));
                return 0;
            }
        });
        int status = ApplicationLauncher.exec(conf, new String[] {
                Bootstrap.class.getName(),
                "-D",
                "TESTING=OK",
        });
        assertThat(status, is(0));
    }

    /**
     * w/ libjars.
     * @throws Exception if failed
     */
    @Test
    public void w_libjars() throws Exception {
        File lib = putFile("dummy.jar");
        Bootstrap.register(new Callback() {
            @Override
            public int run(String[] args) throws Exception {
                assertThat(lib, inClasspath(Thread.currentThread().getContextClassLoader()));
                return 0;
            }
        });
        int status = ApplicationLauncher.exec(conf, new String[] {
                Bootstrap.class.getName(),
                "-libjars",
                lib.getPath(),
        });
        assertThat(status, is(0));
        assertThat(lib, not(inClasspath(Thread.currentThread().getContextClassLoader())));
    }

    /**
     * w/o application class arguments.
     */
    @Test
    public void invalid_wo_application_class() {
        int status = ApplicationLauncher.exec(conf, new String[0]);
        assertThat(status, is(ApplicationLauncher.LAUNCH_ERROR));
    }

    /**
     * class w/ erroneous constructor.
     */
    @Test
    public void invalid_w_err_ctor() {
        int status = ApplicationLauncher.exec(conf, new String[] {
                Invalid.class.getName(),
        });
        assertThat(status, is(ApplicationLauncher.LAUNCH_ERROR));
    }

    /**
     * body returns bad status.
     */
    @Test
    public void body_w_failure() {
        Bootstrap.register(new Callback() {
            @Override
            public int run(String[] args) throws Exception {
                return 1;
            }
        });
        int status = ApplicationLauncher.exec(conf, new String[] {
                Bootstrap.class.getName(),
        });
        assertThat(status, is(1));
    }

    /**
     * class w/ erroneous body.
     */
    @Test
    public void body_w_error() {
        Bootstrap.register(new Callback() {
            @Override
            public int run(String[] args) throws Exception {
                throw new IOException();
            }
        });
        int status = ApplicationLauncher.exec(conf, new String[] {
                Bootstrap.class.getName(),
        });
        assertThat(status, is(ApplicationLauncher.CLIENT_ERROR));
    }

    private abstract static class Callback extends Configured implements Tool {
        public Callback() {
            return;
        }
    }

    /**
     * Bootstrap application for {@link ApplicationLauncherTest}.
     */
    public static final class Bootstrap extends Configured implements Tool {

        private static Tool callback;

        static void register(Tool tool) {
            callback = tool;
        }


        @Override
        public int run(String[] args) throws Exception {
            callback.setConf(getConf());
            return callback.run(args);
        }
    }

    /**
     * Invalid bootstrap application.
     */
    public static final class Invalid extends Configured implements Tool {
        /**
         * @throws UnsupportedOperationException always
         */
        public Invalid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int run(String[] args) throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
