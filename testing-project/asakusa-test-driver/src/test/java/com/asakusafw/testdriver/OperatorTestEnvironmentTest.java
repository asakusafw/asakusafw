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
package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.runtime.core.BatchContext;
import com.asakusafw.runtime.core.Report;
import com.asakusafw.runtime.core.Report.Level;
import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.flow.RuntimeResourceManager;

/**
 * Test for {@link OperatorTestEnvironment}.
 */
public class OperatorTestEnvironmentTest {

    /**
     * 設定ファイルをロードして確認。
     * @throws Throwable エラーが発生した場合
     */
    @Test
    public void load() throws Throwable {
        OperatorTestEnvironment env = new OperatorTestEnvironment(getFilePath("simple.xml"));
        env.before();
        try {
            assertThat(Collector.lastMessage, is("setup"));

            Report.error("hello");
            assertThat(Collector.lastLevel, is(Level.ERROR));
            assertThat(Collector.lastMessage, is("hello"));
        } finally {
            env.after();
        }
        assertThat(Collector.lastMessage, is("cleanup"));
    }

    /**
     * 設定ファイルをロードして確認後、上書きしてリロード。
     * @throws Throwable エラーが発生した場合
     */
    @Test
    public void reload() throws Throwable {
        OperatorTestEnvironment env = new OperatorTestEnvironment(getFilePath("simple.xml"));
        env.before();
        try {
            assertThat(Collector.lastMessage, is("setup"));
            env.configure("testing.setup", "reload-setup");
            env.configure("testing.cleanup", "reload-cleanup");
            env.reload();
            assertThat(Collector.lastMessage, is("reload-setup"));
        } finally {
            env.after();
        }
        assertThat(Collector.lastMessage, is("reload-cleanup"));
    }

    /**
     * 設定ファイルをロードして確認後、上書きしてリロード。
     * @throws Throwable エラーが発生した場合
     */
    @Test
    public void variable() throws Throwable {
        OperatorTestEnvironment env = new OperatorTestEnvironment(getFilePath("simple.xml"));
        env.before();
        try {
            env.setBatchArg("hello", "world");
            env.reload();
            assertThat(BatchContext.get("hello"), is("world"));
        } finally {
            env.after();
        }
    }

    /**
     * Missing implicit configuration file.
     * @throws Exception if failed
     */
    @Test
    public void missing_implicit_configuraion() throws Exception {
        Assume.assumeThat(
                getClass().getResource(RuntimeResourceManager.CONFIGURATION_FILE_NAME),
                is(nullValue()));

        OperatorTestEnvironment env = new OperatorTestEnvironment();
        env.before();
        try {
            // we can use Report API even if 'asakusa-resources.xml' does not exist
            Report.info("OK");
        } finally {
            env.after();
        }
        assertThat(Collector.lastMessage, is("cleanup"));
    }

    /**
     * Missing explicit configuration file.
     * @throws Exception if failed
     */
    @Test
    public void missing_explicit_configuraion() throws Exception {
        OperatorTestEnvironment env = new OperatorTestEnvironment("__MISSING__.xml");
        try {
            env.before();
            try {
                fail("missing explicit configuration");
            } finally {
                env.after();
            }
        } catch (RuntimeException e) {
            // ok.
        }
    }

    private String getFilePath(String name) {
        String className = OperatorTestEnvironmentTest.class.getName();
        int lastDot = className.lastIndexOf('.');
        assertThat(className, lastDot, greaterThanOrEqualTo(0));
        String packageName = className.substring(0, lastDot);
        return packageName.replace('.', '/') + '/' + name;
    }

    /**
     * エラーレポートを集計する。
     */
    public static final class Collector extends Report.Delegate {

        static volatile Level lastLevel;

        static volatile String lastMessage;

        @Override
        protected void report(Level level, String message) throws IOException {
            lastLevel = level;
            lastMessage = message;
        }

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            lastLevel = Level.INFO;
            lastMessage = configuration.get("testing.setup", "setup");
        }

        @Override
        public void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            lastLevel = Level.INFO;
            lastMessage = configuration.get("testing.cleanup", "cleanup");
        }
    }
}
