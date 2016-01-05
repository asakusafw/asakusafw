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
package com.asakusafw.testdriver.bulkloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.Properties;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * Manipulate configuration and context class loader.
 */
public class ConfigurationContext extends ExternalResource {

    private final TemporaryFolder folder = new TemporaryFolder();

    private ClassLoader context;

    @Override
    protected void before() throws Throwable {
        folder.create();
        final File root = folder.getRoot();
        ClassLoader classLoader = AccessController.doPrivileged(new PrivilegedExceptionAction<ClassLoader>() {
            @Override
            public ClassLoader run() throws Exception {
                return new URLClassLoader(
                        new URL[] { root.toURI().toURL() },
                        getClass().getClassLoader());
            }
        });
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
     * Puts database configuration to use H2.
     * @param targetName target configuration name
     * @param simpleName database simple name
     */
    public void put(String targetName, String simpleName) {
        Properties p = new Properties();
        p.setProperty("jdbc.driver", org.h2.Driver.class.getName());
        p.setProperty("jdbc.url", "jdbc:h2:mem:" + simpleName);
        put(targetName, p);
    }

    /**
     * Puts database configuration.
     * @param targetName target configuration name
     * @param properties JDBC properties to be put
     */
    public void put(String targetName, Properties properties) {
        try {
            File file;
            if (targetName == null) {
                file = folder.newFile(Configuration.COMMON_FILE);
            } else {
                file = folder.newFile(MessageFormat.format(Configuration.FILE_PATTERN, targetName));
            }
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
