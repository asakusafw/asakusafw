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
package com.asakusafw.runtime.stage.launcher;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Base test class for this package.
 */
public abstract class LauncherTestRoot {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * assert the target resource is in any jar files.
     * @param urls jar file URLs
     * @param resourceName the target resource name
     */
    public void assertClasspath(final URL[] urls, final String resourceName) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                ClassLoader loader = new URLClassLoader(urls);
                try {
                    assertThat(loader.getResource(resourceName), is(notNullValue()));
                } finally {
                    if (loader instanceof Closeable) {
                        try {
                            ((Closeable) loader).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        });
    }

    /**
     * Returns a file matcher which file is in the URLs.
     * assert the target resource is in any jar files.
     * @param classLoader URL class loader
     * @return the matcher
     */
    public Matcher<File> inClasspath(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            return inClasspath(((URLClassLoader) classLoader).getURLs());
        }
        throw new AssertionError(classLoader);
    }

    /**
     * Returns a file matcher which file is in the URLs.
     * assert the target resource is in any jar files.
     * @param urls URLs
     * @return the matcher
     */
    public Matcher<File> inClasspath(final URL[] urls) {
        return new BaseMatcher<File>() {
            @Override
            public boolean matches(Object item) {
                try {
                    File actual = ((File) item).getCanonicalFile();
                    for (URL url : urls) {
                        if (url.getProtocol().equalsIgnoreCase("file") == false) {
                            continue;
                        }
                        File expected = new File(url.toURI()).getCanonicalFile();
                        if (expected.equals(actual)) {
                            return true;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("in classpath:").appendValue(Arrays.toString(urls));
            }
        };
    }

    /**
     * Copy a resource file to temporary folder.
     * @param path the resource file (obtain using {@link Class#getResource(String)})
     * @return the copied file
     * @throws IOException if failed to copy the target file
     */
    public File putFile(String path) throws IOException {
        return putFile(path, folder.newFile(new File(path).getName()));
    }

    private File putFile(String path, File target) throws FileNotFoundException, IOException {
        assert target != null;
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(path, input, is(notNullValue()));
            prepareParent(target);
            try (OutputStream output = new FileOutputStream(target)) {
                copyStream(input, output);
            }
        }
        return target;
    }

    private void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[512];
        while (true) {
            int read = input.read(buf);
            if (read < 0) {
                break;
            }
            output.write(buf, 0, read);
        }
    }

    private void prepareParent(File target) throws IOException {
        assert target != null;
        if (target.getParentFile().isDirectory() == false && target.getParentFile().mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to copy into {0} (cannot create target directory)",
                    target));
        }
    }
}