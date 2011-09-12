/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.windgate.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.ResourceProfile;

/**
 * A structured profile for {@link JdbcResourceMirror}.
 * @since 0.2.2
 */
public class JdbcProfile {

    static final WindGateLogger WGLOG = new JdbcLogger(JdbcProfile.class);

    static final Logger LOG = LoggerFactory.getLogger(JdbcProfile.class);

    /**
     * The profile key of fully qualified JDBC driver class name.
     */
    public static final String KEY_DRIVER = "driver";

    /**
     * The profile key of database URL.
     */
    public static final String KEY_URL = "url";

    /**
     * The profile key of database user name.
     */
    public static final String KEY_USER = "user";

    /**
     * The profile key of database connection password.
     */
    public static final String KEY_PASSWORD = "password";

    /**
     * The profile key of {@link #getBatchPutUnit()}.
     */
    public static final String KEY_BATCH_PUT_UNIT = "batchPutUnit";

    private final String resourceName;

    private final ClassLoader classLoader;

    private final String driver;

    private final String url;

    private final String user;

    private final String password;

    private final long batchPutUnit;

    /**
     * Creates a new instance.
     * @param resourceName the target resource name
     * @param classLoader a class loader, or {@code null} to use the system class loader
     * @param driver a fully qualified class name of JDBC Driver implementation
     * @param url database URL
     * @param user database connection user (nullable)
     * @param password database connection password (nullable)
     * @param batchPutUnit the number of rows on each batch insertion
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JdbcProfile(
            String resourceName,
            ClassLoader classLoader,
            String driver,
            String url,
            String user,
            String password,
            long batchPutUnit) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        if (url == null) {
            throw new IllegalArgumentException("url must not be null"); //$NON-NLS-1$
        }
        if (batchPutUnit <= 0L) {
            throw new IllegalArgumentException("batchPutUnit must be > 0"); //$NON-NLS-1$
        }
        this.resourceName = resourceName;
        this.classLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.batchPutUnit = batchPutUnit;
    }

    /**
     * Converts {@link ResourceProfile} into {@link JdbcProfile}.
     * @param profile target profile
     * @return the converted profile
     * @throws IllegalArgumentException if profile is not valid, or any parameter is {@code null}
     */
    public static JdbcProfile convert(ResourceProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        String resourceName = profile.getName();
        ClassLoader classLoader = profile.getClassLoader();
        String driver = extract(profile, KEY_DRIVER, true);
        String url = extract(profile, KEY_URL, true);
        String user = extract(profile, KEY_USER, false);
        String password = extract(profile, KEY_PASSWORD, false);
        String batchPutUnitString = extract(profile, KEY_BATCH_PUT_UNIT, false);
        long batchPutUnit;
        try {
            if (batchPutUnitString == null) {
                batchPutUnit = Long.MAX_VALUE;
            } else {
                batchPutUnit = Long.parseLong(batchPutUnitString);
            }
        } catch (NumberFormatException e) {
            WGLOG.error("E00001",
                    profile.getName(),
                    KEY_BATCH_PUT_UNIT,
                    batchPutUnitString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid number: {2} (resource={0})",
                    profile.getName(),
                    KEY_BATCH_PUT_UNIT,
                    batchPutUnitString), e);
        }
        if (batchPutUnit <= 0) {
            WGLOG.error("E00001",
                    profile.getName(),
                    KEY_BATCH_PUT_UNIT,
                    batchPutUnitString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be > 0: {2} (resource={0})",
                    profile.getName(),
                    KEY_BATCH_PUT_UNIT,
                    batchPutUnitString));
        }

        return new JdbcProfile(resourceName, classLoader, driver, url, user, password, batchPutUnit);
    }

    private static String extract(ResourceProfile profile, String configKey, boolean mandatory) {
        assert profile != null;
        assert configKey != null;
        String value = profile.getConfiguration().get(configKey);
        if (value == null) {
            if (mandatory == false) {
                return null;
            }
            WGLOG.error("E00001",
                    profile.getName(),
                    configKey,
                    null);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Resource \"{0}\" must declare \"{1}\"",
                    profile.getName(),
                    configKey));
        }
        return value.trim();
    }

    /**
     * Returns the resource name.
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the class loader for the resources.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Creates a new connection using this configuration.
     * @return the created connection
     * @throws IOException if failed to create a new connection
     */
    public Connection openConnection() throws IOException {
        LOG.debug("Opening JDBC connection: {}",
                url);
        try {
            Class.forName(driver, true, classLoader);
            Connection conn = DriverManager.getConnection(url, user, password);
            boolean succeed = false;
            try {
                conn.setAutoCommit(false);
                succeed = true;
            } finally {
                if (succeed == false) {
                    LOG.debug("Disposing JDBC connection: {}",
                            url);
                    conn.close();
                }
            }
            return conn;
        } catch (Exception e) {
            WGLOG.error(e, "E00002",
                    getResourceName(),
                    url);
            throw new IOException(MessageFormat.format(
                    "Failed to open connection: {0}",
                    url), e);
        }
    }

    /**
     * Return the number of rows on each batch insertion.
     * @return the number of rows on each batch insertion
     */
    public long getBatchPutUnit() {
        return batchPutUnit;
    }
}
