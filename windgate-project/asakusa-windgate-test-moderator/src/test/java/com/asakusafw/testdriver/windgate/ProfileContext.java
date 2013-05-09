/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.testdriver.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Properties;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * Manipulate configuration and context class loader.
 */
public class ProfileContext extends ExternalResource {

    private final TemporaryFolder folder = new TemporaryFolder();

    private ClassLoader context;

    @Override
    protected void before() throws Throwable {
        folder.create();
        ClassLoader classLoader = new URLClassLoader(new URL[] {
                folder.getRoot().toURI().toURL(),
        }, getClass().getClassLoader());
        context = Thread.currentThread().getContextClassLoader();
        boolean green = false;
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            green = true;
        } finally {
            if (green == false) {
                after();
            }
        }
    }

    @Override
    protected void after() {
        try {
            Thread.currentThread().setContextClassLoader(context);
            System.gc();
        } finally {
            folder.delete();
        }
    }

    /**
     * Returns template of WindGate profile.
     * @return the template
     */
    public Properties getTemplate() {
        Properties p = new Properties();
        InputStream in = ProfileContext.class.getResourceAsStream("windgate-template.properties");
        assertThat(in, is(notNullValue()));
        try {
            p.load(in);
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return p;
    }

    /**
     * Puts profile.
     * @param profileName target profile name
     * @param properties profile contents
     */
    public void put(String profileName, Properties properties) {
        try {
            File file = folder.newFile(MessageFormat.format(
                    WindGateTestHelper.TESTING_PROFILE_PATH, profileName));
            FileOutputStream out = new FileOutputStream(file);
            try {
                properties.store(out, "testing");
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
