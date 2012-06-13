/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
     * @since 0.2.4
     */
    public static final String KEY_BATCH_GET_UNIT = "batchGetUnit";

    /**
     * The profile key of {@link #getConnectRetryCount()}.
     * @since 0.2.4
     */
    public static final String KEY_CONNECT_RETRY_COUNT = "connect.retryCount";

    /**
     * The profile key of {@link #getConnectRetryInterval()}.
     * @since 0.2.4
     */
    public static final String KEY_CONNECT_RETRY_INTERVAL = "connect.retryInterval";

    /**
     * The profile key of {@link #getTruncateStatement(String)}.
     * @since 0.2.4
     */
    public static final String KEY_TRUNCATE_STATEMENT = "statement.truncate";

    /**
     * The profile key of {@link #getConnectionProperties()}.
     * @since 0.2.4
     */
    public static final String KEY_PREFIX_PROPERTIES = "properties.";

    /**
     * The default value of {@link #KEY_BATCH_GET_UNIT}.
     * @since 0.2.4
     */
    public static final int DEFAULT_BATCH_GET_UNIT = 0;

    /**
     * The default value of {@link #KEY_BATCH_PUT_UNIT}.
     * @since 0.2.4
     */
    public static final long DEFAULT_BATCH_PUT_UNIT = Long.MAX_VALUE;

    /**
     * The default value of {@link #KEY_CONNECT_RETRY_COUNT}.
     * @since 0.2.4
     */
    public static final int DEFAULT_CONNECT_RETRY_COUNT = 0;

    /**
     * The default value of {@link #KEY_CONNECT_RETRY_INTERVAL}.
     * @since 0.2.4
     */
    public static final int DEFAULT_CONNECT_RETRY_INTERVAL = 10;

    /**
     * The default value of {@link #KEY_TRUNCATE_STATEMENT}.
     * @since 0.2.4
     */
    public static final String DEFAULT_TRUNCATE_STATEMENT = "TRUNCATE TABLE {0}";

    private final String resourceName;

    private final ClassLoader classLoader;

    private final String driver;

    private final String url;

    private final String user;

    private final String password;

    private final Map<String, String> connectionProperties;

    private volatile int batchGetUnit = DEFAULT_BATCH_GET_UNIT;

    private volatile long batchPutUnit = DEFAULT_BATCH_PUT_UNIT;

    private volatile int connectRetryCount = DEFAULT_CONNECT_RETRY_COUNT;

    private volatile int connectRetryInterval = DEFAULT_CONNECT_RETRY_INTERVAL;

    private volatile String truncateStatement = DEFAULT_TRUNCATE_STATEMENT;

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
        this(resourceName, classLoader, driver, url, user, password, Collections.<String, String>emptyMap());
        setBatchPutUnit0(batchPutUnit);
    }

    /**
     * Creates a new instance.
     * @param resourceName the target resource name
     * @param classLoader a class loader, or {@code null} to use the system class loader
     * @param driver a fully qualified class name of JDBC Driver implementation
     * @param url database URL
     * @param user database connection user (nullable)
     * @param password database connection password (nullable)
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
        if (connectionProperties == null) {
            throw new IllegalArgumentException("connectProperties must not be null"); //$NON-NLS-1$
        }
        this.resourceName = resourceName;
        this.classLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
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
        String driver = extract(profile, KEY_DRIVER);
        String url = extract(profile, KEY_URL);
        String user = extract(profile, KEY_USER, null);
        String password = extract(profile, KEY_PASSWORD, null);
        Map<String, String> connectionProperties = PropertiesUtil.createPrefixMap(
                profile.getConfiguration(),
                KEY_PREFIX_PROPERTIES);

        JdbcProfile result = new JdbcProfile(
                resourceName, classLoader, driver, url, user, password, connectionProperties);

        int batchGetUnit = extractInt(profile, KEY_BATCH_GET_UNIT, 0, DEFAULT_BATCH_GET_UNIT);
        long batchPutUnit = extractLong(profile, KEY_BATCH_PUT_UNIT, 1, DEFAULT_BATCH_PUT_UNIT);
        int connectRetryCount = extractInt(profile, KEY_CONNECT_RETRY_COUNT, 0, DEFAULT_CONNECT_RETRY_COUNT);
        int connectRetryInterval = extractInt(profile, KEY_CONNECT_RETRY_INTERVAL, 1, DEFAULT_CONNECT_RETRY_INTERVAL);
        String truncateStatement = extract(profile, KEY_TRUNCATE_STATEMENT, DEFAULT_TRUNCATE_STATEMENT);
        try {
            MessageFormat.format(truncateStatement, "dummy");
        } catch (IllegalArgumentException e) {
            WGLOG.error("E00001",
                    profile.getName(),
                    KEY_TRUNCATE_STATEMENT,
                    truncateStatement);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid MessageFormat: {2} (resource={0})",
                    profile.getName(),
                    KEY_TRUNCATE_STATEMENT,
                    truncateStatement), e);
        }

        result.setBatchGetUnit(batchGetUnit);
        result.setBatchPutUnit(batchPutUnit);
        result.setConnectRetryCount(connectRetryCount);
        result.setConnectRetryInterval(connectRetryInterval);
        result.setTruncateStatement(truncateStatement);
        return result;
    }

    private static int extractInt(ResourceProfile profile, String key, int minimumValue, int defaultValue) {
        assert profile != null;
        assert key != null;
        String valueString = extract(profile, key, null);
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
        String valueString = extract(profile, key, null);
        long value;
        try {
            if (valueString == null || valueString.isEmpty()) {
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

    private static String extract(ResourceProfile profile, String configKey) {
        assert profile != null;
        assert configKey != null;
        String value = extract(profile, configKey, null);
        if (value == null) {
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

    private static String extract(ResourceProfile profile, String configKey, String defaultValue) {
        assert profile != null;
        assert configKey != null;
        String value = profile.getConfiguration().get(configKey);
        if (value == null) {
            return defaultValue;
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
                Driver driverObject = driverClass.getConstructor().newInstance();
                Connection connection = driverObject.connect(url, properties);
                if (connection == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Driver class {0} may not support {1}",
                            driverClass.getName(),
                            url));
                }
                return connection;
            } catch (RuntimeException inner) {
                LOG.debug(MessageFormat.format(
                        "Failed to resolve driver class (internal error): {0} (on {1})",
                        driverClass.getName(),
                        driverClass.getClassLoader()), e);
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
     * Return extra configuration properties.
     * If there is no extra configuration, this returns empty map.
     * @return extra configuration properties
     * @since 0.2.4
     */
    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * Return the number of rows on each fetch ({@code fetch-size}).
     * @return the number of rows on each fetch
     * @since 0.2.4
     */
    public int getBatchGetUnit() {
        return batchGetUnit;
    }

    /**
     * Configures {@link #KEY_BATCH_GET_UNIT}.
     * @param value to set
     * @throws IllegalArgumentException if {@code < 0}
     */
    public void setBatchGetUnit(int value) {
        if (value < 0L) {
            throw new IllegalArgumentException("batchGetUnit must be >= 0"); //$NON-NLS-1$
        }
        this.batchGetUnit = value;
    }

    /**
     * Return the number of rows on each batch insertion.
     * @return the number of rows on each batch insertion
     */
    public long getBatchPutUnit() {
        return batchPutUnit;
    }

    /**
     * Configures {@link #KEY_BATCH_PUT_UNIT}.
     * @param value to set
     * @throws IllegalArgumentException if {@code <= 0}
     */
    public void setBatchPutUnit(long value) {
        setBatchPutUnit0(value);
    }

    private void setBatchPutUnit0(long value) {
        if (value <= 0L) {
            throw new IllegalArgumentException("batchPutUnit must be > 0"); //$NON-NLS-1$
        }
        this.batchPutUnit = value;
    }

    /**
     * Returns the retry count on create connection.
     * @return the retry count, or {@code 0} for no retry
     * @since 0.2.4
     */
    public int getConnectRetryCount() {
        return connectRetryCount;
    }

    /**
     * Configures {@link #KEY_CONNECT_RETRY_COUNT}.
     * @param value to set
     * @throws IllegalArgumentException if {@code < 0}
     */
    public void setConnectRetryCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("connectionRetryCount must be >= 0"); //$NON-NLS-1$
        }
        this.connectRetryCount = value;
    }

    /**
     * Returns the retry interval (in second).
     * @return the connectionRetryInterval
     * @see #getConnectRetryCount()
     * @since 0.2.4
     */
    public int getConnectRetryInterval() {
        return connectRetryInterval;
    }

    /**
     * Configures {@link #KEY_CONNECT_RETRY_INTERVAL}.
     * @param value to set
     * @throws IllegalArgumentException if {@code < 0}
     */
    public void setConnectRetryInterval(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("connectRetryInterval must be > 0"); //$NON-NLS-1$
        }
        this.connectRetryInterval = value;
    }

    /**
     * Returns the truncate statement.
     * @param tableName target table name
     * @return the truncate statement
     * @since 0.2.4
     */
    public String getTruncateStatement(String tableName) {
        return MessageFormat.format(truncateStatement, tableName);
    }

    /**
     * Configures {@link #KEY_TRUNCATE_STATEMENT}.
     * @param pattern to set
     * @throws IllegalArgumentException if the pattern is not in form of message format
     */
    public void setTruncateStatement(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null"); //$NON-NLS-1$
        }
        MessageFormat.format(pattern, "example");
        this.truncateStatement = pattern;
    }
}
