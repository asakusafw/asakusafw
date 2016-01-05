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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription;

/**
 * Configurations to connect the ThunderGate database.
 * <p>
 * This locates a configuration file with following order:
 * </p>
 * <ol>
 * <li> {@code <classpath-root>/<target-name>-jdbc.properties} </li>
 * <li> {@code <classpath-root>/asakusa-test.properties} </li>
 * <li> {@code <local-config-path>/<target-name>-jdbc.properties} </li>
 * </ol>
 * <p>
 * Note that, classpath root is determined by using a context class loader,
 * or a defining class loader of this class if context class loader is not defined.
 * Additionally, if environment variable {@code ASAKUSA_HOME} is not defined,
 * each path depends on local config will be omitted.
 * </p>
 * <p>
 * Each properties file must include the following items:
 * </p>
 * <ul>
 * <li> {@code driver} - JDBC driver class name, </li>
 * <li> {@code url} - Database URL, </li>
 * <li> {@code user} - Database login user name, </li>
 * <li> {@code password} - Database login password. </li>
 * </ul>
 * <p>
 * You can also qualify each property names at its head with one of following:
 * </p>
 * <ol>
 * <li> {@code "test.jdbc."} </li>
 * <li> {@code "jdbc."} </li>
 * </ol>
 * <p>
 * If there are same properties with different qualifiers,
 * this will resolve in the order noted above.
 * For example, there are {@code test.jdbc.user = asakusa}
 * and {@code jdbc.user = shinagawa}, will be resolved as {@code asakusa}.
 * </p>
 */
public class Configuration {

    static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    /**
     * Properties file name ({@code target name} sensitive).
     */
    public static final String FILE_PATTERN = "{0}-jdbc.properties";

    /**
     * Properties file name ({@code target name} insensitive).
     */
    public static final String COMMON_FILE = "asakusa-test.properties";

    private static final String[] PREFIX = {
        "test.jdbc.",
        "jdbc.",
    };

    /**
     * Property suffix: fully qualified class name of JDBC Driver implementation.
     */
    public static final String K_DRIVER = "driver";

    /**
     * Property suffix: database URL.
     */
    public static final String K_URL = "url";

    /**
     * Property suffix: database connection user name.
     */
    public static final String K_USER = "user";

    /**
     * Property suffix: database connection password.
     */
    public static final String K_PASSWORD = "password";

    private final String driver;

    private final String url;

    private final String user;

    private final String password;

    /**
     * Creates a new instance.
     * @param driver a fully qualified class name of JDBC Driver implementation
     * @param url database URL
     * @param user database connection user (nullable)
     * @param password database connection password (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Configuration(String driver, String url, String user, String password) {
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        if (url == null) {
            throw new IllegalArgumentException("url must not be null"); //$NON-NLS-1$
        }
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Loads properties from the specified URL.
     * @param targetName connection
     *     {@link BulkLoadImporterDescription#getTargetName() target name}
     * @return the restored configurations
     * @throws IOException if failed to load configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Configuration load(String targetName) throws IOException {
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Loading JDBC configuration: {}", targetName);
        String path = MessageFormat.format(FILE_PATTERN, targetName);
        URL resource = findResource(path);
        if (resource == null) {
            throw new FileNotFoundException(path);
        }
        LOG.debug("Using JDBC configuration: {}", resource);
        return load(resource);
    }

    private static URL findResource(String path) {
        assert path != null;
        URL specifiedResource = findResourceOnClassPath(path);
        if (specifiedResource != null) {
            return specifiedResource;
        }

        URL defaultResource = findResourceOnClassPath(COMMON_FILE);
        if (defaultResource != null) {
            return defaultResource;
        }

        URL contextResource = findResourceOnHomePath(path);
        if (contextResource != null) {
            return contextResource;
        }

        return null;
    }

    private static URL findResourceOnClassPath(String path) {
        assert path != null;
        ClassLoader loader = getClassLoader();
        URL classPath = loader.getResource(path);
        return classPath;
    }

    private static URL findResourceOnHomePath(String path) {
        assert path != null;
        String home = System.getenv("ASAKUSA_HOME");
        if (home != null) {
            File file = new File(home, "bulkloader/conf/" + path);
            if (file.isFile() != false) {
                try {
                    return file.toURI().toURL();
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to convert a file path to URL: {0}",
                            file), e);
                    return null;
                }
            }
        }
        return null;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            return loader;
        }
        return Configuration.class.getClassLoader();
    }

    /**
     * Loads properties from the specified URL.
     * @param resource target URL
     * @return the restored configurations
     * @throws IOException if failed to load configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Configuration load(URL resource) throws IOException {
        if (resource == null) {
            throw new IllegalArgumentException("resource must not be null"); //$NON-NLS-1$
        }
        Properties properties = loadProperties(resource);
        String driver = extract(K_DRIVER, properties, true, resource);
        String url = extract(K_URL, properties, true, resource);
        String user = extract(K_USER, properties, false, resource);
        String password = extract(K_PASSWORD, properties, false, resource);
        Configuration configuration = new Configuration(driver, url, user, password);
        LOG.debug("JDBC configuration: {}", configuration);
        return configuration;
    }

    private static String extract(
            String name, Properties properties,
            boolean mandatory, URL source) throws IOException {
        assert name != null;
        assert properties != null;
        assert source != null;
        for (String prefix : PREFIX) {
            String key = prefix + name;
            String value = properties.getProperty(key);
            if (value != null) {
                return value;
            }
        }
        if (mandatory) {
            throw new IOException(MessageFormat.format(
                    "プロパティ\"{1}\"が見つかりません: {0}",
                    source,
                    PREFIX[0] + name));
        }
        return null;
    }

    private static Properties loadProperties(URL resource) throws IOException {
        assert resource != null;
        InputStream in = resource.openStream();
        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            in.close();
        }
    }

    /**
     * Creates a new connection using this configuration.
     * @return the created connection
     * @throws IOException if failed to create a new connection
     */
    public Connection open() throws IOException {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "{0}を開けませんでした",
                    url), e);
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "JDBC(driver={0}, url={1}, user={2}, password?={3}",
                driver,
                url,
                user,
                password != null);
    }
}
