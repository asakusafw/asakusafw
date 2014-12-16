/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.util.hadoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.OutputBuffer;

/**
 * Detects and write out the current Hadoop configuration directory.
 * @since 0.5.0
 */
public final class ConfigurationDetecter {

    static final String MARKER_FILE_NAME = "core-site.xml"; //$NON-NLS-1$

    private static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    private ConfigurationDetecter() {
        return;
    }

    /**
     * Program entry.
     * @param args {@code [0]} - the path to the output file
     * @throws IOException if failed to create output
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid argument for {0}: {1}",
                    ConfigurationDetecter.class.getName(),
                    Arrays.toString(args)));
        }
        int exit = execute(new File(args[0]), new Configuration().getClassLoader());
        if (exit != 0) {
            System.exit(1);
        }
    }

    static int execute(File path, ClassLoader classLoader) {
        assert path != null;
        assert classLoader != null;
        try {
            File conf = detectConfigurationDirectory(classLoader);
            write(conf, path);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return 1;
        }
        return 0;
    }

    private static File detectConfigurationDirectory(ClassLoader classLoader) throws IOException {
        assert classLoader != null;
        URL url = classLoader.getResource(MARKER_FILE_NAME);
        if (url == null) {
            throw new FileNotFoundException(MessageFormat.format(
                    "There is no marker file on the current classpath: {0}",
                    MARKER_FILE_NAME));
        }
        File file;
        try {
            file = new File(url.toURI());
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to detect a default configuration path: {0}",
                    url), e);
        }
        return file.getParentFile();
    }

    private static void write(File conf, File path) throws IOException {
        assert conf != null;
        assert path != null;
        OutputStream output = new FileOutputStream(path);
        try {
            output.write(conf.getAbsolutePath().getBytes(ENCODING));
        } finally {
            output.close();
        }
    }

    /**
     * Read the path in the target file.
     * @param path the file which has another path
     * @return the read path
     * @throws IOException if failed to read the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static File read(File path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        String result;
        InputStream input = new FileInputStream(path);
        try {
            OutputBuffer ob = new OutputBuffer();
            byte[] buf = new byte[256];
            while (true) {
                int read = input.read(buf);
                if (read < 0) {
                    break;
                }
                ob.write(buf, 0, read);
            }
            result = new String(ob.getData(), 0, ob.getLength(), ENCODING);
            ob.close();
        } finally {
            input.close();
        }
        File file = new File(result);
        return file;
    }
}
