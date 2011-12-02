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
import java.sql.Driver;
import java.sql.DriverManager;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.ResourceProfile;
import com.asakusafw.windgate.core.util.PropertiesUtil;

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

    /**
     * The profile key of {@link #getBatchGetUnit()}.
     */
    public static final String KEY_BATCH_GET_UNIT = "batchGetUnit";

    /**
     * The profile key of {@link #getConnectRetryCount()}.
     */
    public static final String KEY_CONNECT_RETRY_COUNT = "connect.retryCount";

    /**
     * The profile key of {@link #getConnectRetryInterval()}.
     */
    public static final String KEY_CONNECT_RETRY_INTERVAL = "connect.retryInterval";



    /**
     * The profile key of {@link #getConnectionProperties()}.
     */
    public static final String KEY_PREFIX_PROPERTIES = "properties.";

    private final String resourceName;

    private final ClassLoader classLoader;

    private final String driver;

    private final String url;

    private final String user;

    private final String password;

    private final int batchGetUnit;

    private final long batchPutUnit;

    private final int connectRetryCount;

    private final int connectRetryInterval;

    private final Map<String, String> connectionProperties;

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
        this(resourceName, classLoader, driver, url, user, password,
                0, Long.MAX_VALUE,
                0, 1,
                Collections.<String, String>emptyMap());
    }

    /**
     * Creates a new instance.
     * @param resourceName the target resource name
     * @param classLoader a class loader, or {@code null} to use the system class loader
     * @param driver a fully qualified class name of JDBC Driver implementation
     * @param url database URL
     * @param user database connection user (nullable)
     * @param password database connection password (nullable)
     * @param batchGetUnit the number of rows on each batch fetching
     * @param batchPutUnit the number of rows on each batch insertion
     * @param connectRetryCount retry count to create connection
     * @param connectRetryInterval interval of create connection
     * @param connectionProperties extra connection properties
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.4
     */
    public JdbcProfile(
            String resourceName,
            ClassLoader classLoader,
            String driver,
            String url,
            String user,
            String password,
            int batchGetUnit,
            long batchPutUnit,
            int connectRetryCount,
            int connectRetryInterval,
            Map<String, String> connectionProperties) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        if (url == null) {
            throw new IllegalArgumentException("url must not be null"); //$NON-NLS-1$
        }
        if (batchGetUnit < 0L) {
            throw new IllegalArgumentException("batchGetUnit must be >= 0"); //$NON-NLS-1$
        }
        if (batchPutUnit <= 0L) {
            throw new IllegalArgumentException("batchPutUnit must be > 0"); //$NON-NLS-1$
        }
        if (connectRetryCount < 0) {
            throw new IllegalArgumentException("connectionRetryCount must be >= 0"); //$NON-NLS-1$
        }
        if (connectRetryInterval <= 0) {
            throw new IllegalArgumentException("connectRetryInterval must be > 0"); //$NON-NLS-1$
        }
        if (connectionProperties == null) {
            throw new IllegalArgumentException("connectProperties must not be null"); //$NON-NLS-1$
        }
        this.resourceName = resourceName;
        this.classLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.batchGetUnit = batchGetUnit;
        this.batchPutUnit = batchPutUnit;
        this.connectRetryCount = connectRetryCount;
        this.connectRetryInterval = connectRetryInterval;
        this.connectionProperties = Collections.unmodifiableMap(connectionProperties);
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
        ClassLoader classLoader = profile.getContext().getClassLoader();
        String driver = extract(profile, KEY_DRIVER, true);
        String url = extract(profile, KEY_URL, true);
        String user = extract(profile, KEY_USER, false);
        String password = extract(profile, KEY_PASSWORD, false);
        int batchGetUnit = extractInt(profile, KEY_BATCH_GET_UNIT, 0, 0);
        long batchPutUnit = extractLong(profile, KEY_BATCH_PUT_UNIT, 1, Long.MAX_VALUE);
        int connectRetryCount = extractInt(profile, KEY_CONNECT_RETRY_COUNT, 0, 0);
        int connectRetryInterval = extractInt(profile, KEY_CONNECT_RETRY_INTERVAL, 1, 10);
        Map<String, String> connectionProperties = PropertiesUtil.createPrefixMap(
                profile.getConfiguration(),
                KEY_PREFIX_PROPERTIES);

        return new JdbcProfile(resourceName, classLoader, driver, url, user, password,
                batchGetUnit, batchPutUnit,
                connectRetryCount, connectRetryInterval,
                connectionProperties);
    }

    private static int extractInt(ResourceProfile profile, String key, int minimumValue, int defaultValue) {
        assert profile != null;
        assert key != null;
        String valueString = extract(profile, key, false);
        int value;
        try {
            if (valueString == null || valueString.trim().isEmpty()) {
                value = defaultValue;
            } else {
                value = Integer.parseInt(valueString);
            }
        } catch (NumberFormatException e) {
            WGLOG.error("E00001",
                    profile.getName(),
                    key,
                    valueString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid number: {2} (resource={0})",
                    profile.getName(),
                    key,
                    valueString), e);
        }
        if (value < minimumValue) {
            WGLOG.error("E00001",
                    profile.getName(),
                    key,
                    valueString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be > 0: {2} (resource={0})",
                    profile.getName(),
                    value,
                    valueString));
        }
        return value;
    }

    private static long extractLong(ResourceProfile profile, String key, long minimumValue, long defaultValue) {
        assert profile != null;
        assert key != null;
        String valueString = extract(profile, key, false);
        long value;
        try {
            if (valueString == null || valueString.trim().isEmpty()) {
                value = defaultValue;
            } else {
                value = Integer.parseInt(valueString);
            }
        } catch (NumberFormatException e) {
            WGLOG.error("E00001",
                    profile.getName(),
                    key,
                    valueString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid number: {2} (resource={0})",
                    profile.getName(),
                    key,
                    valueString), e);
        }
        if (value < minimumValue) {
            WGLOG.error("E00001",
                    profile.getName(),
                    key,
                    valueString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be > 0: {2} (resource={0})",
                    profile.getName(),
                    value,
                    valueString));
        }
        return value;
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
            Class<? extends Driver> driverClass = Class.forName(driver, true, classLoader).asSubclass(Driver.class);
            Properties properties = new Properties();
            properties.putAll(getConnectionProperties());
            if (user != null) {
                properties.put("user", user);
            }
            if (password != null) {
                properties.put("password", password);
            }
            Connection conn = null;
            try {
                conn = openConnection(driverClass, properties);
            } catch (Exception first) {
                Exception last = first;
                for (int i = 1, n = getConnectRetryCount(); i <= n; i++) {
                    WGLOG.warn(last, "W00001",
                            getResourceName(),
                            url,
                            i,
                            getConnectRetryCount());
                    try {
                        TimeUnit.SECONDS.sleep(getConnectRetryInterval());
                        conn = openConnection(driverClass, properties);
                        break;
                    } catch (Exception retry) {
                        last = retry;
                    }
                }
                if (conn == null) {
                    throw last;
                }
            }
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

    private Connection openConnection(Class<? extends Driver> driverClass, Properties properties) throws Exception {
        assert properties != null;
        try {
            return DriverManager.getConnection(url, properties);
        } catch (Exception e) {
            // if the current class loader can not access the driver class, create driver class directly
            try {
                Driver driverObject = driverClass.newInstance();
                Connection connection = driverObject.connect(url, properties);
                if (connection == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Driver class {0} may not support {1}",
                            driverClass.getName(),
                            url));
                }
                return connection;
            } catch (Exception inner) {
                LOG.debug(MessageFormat.format(
                        "Failed to resolve driver class: {0} (on {1})",
                        driverClass.getName(),
                        driverClass.getClassLoader()), e);
            }
            throw e;
        }
    }

    /**
     * Return the number of rows on each fetch ({@code fetch-size}).
     * @return the number of rows on each fetch
     */
    public int getBatchGetUnit() {
        return batchGetUnit;
    }

    /**
     * Return the number of rows on each batch insertion.
     * @return the number of rows on each batch insertion
     */
    public long getBatchPutUnit() {
        return batchPutUnit;
    }

    /**
     * Returns the retry count on create connection.
     * @return the retry count, or {@code 0} for no retry
     */
    public int getConnectRetryCount() {
        return connectRetryCount;
    }

    /**
     * Returns the retry interval (in second).
     * @return the connectionRetryInterval
     * @see #getConnectRetryCount()
     */
    public int getConnectRetryInterval() {
        return connectRetryInterval;
    }

    /**
     * Return extra configuration properties.
     * If there is no extra configuration, this returns empty map.
     * @return extra configuration properties
     */
    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }
}
