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
package com.asakusafw.testdriver.windgate;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes JDBC drivers which {@link PluginClassLoader} was loaded.
 * Clients should not use this class directly.
 * @since 0.7.2
 */
public final class JdbcDriverCleaner implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(JdbcDriverCleaner.class);

    /**
     * Creates new instance.
     * @throws IllegalStateException if the current class loader is not a {@link PluginClassLoader}
     */
    public JdbcDriverCleaner() {
        ClassLoader loader = getClass().getClassLoader();
        if ((loader instanceof PluginClassLoader) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "{0} must be loaded on plugin class loader: {1}",
                    getClass().getName(),
                    loader));
        }
    }

    /**
     * De-registers JDBC drivers which the class loader was loaded.
     * @param loader the target class loader
     */
    public static void runIn(PluginClassLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        try {
            Runnable runnable = loader.loadDirect(JdbcDriverCleaner.class)
                    .asSubclass(Runnable.class)
                    .newInstance();
            runnable.run();
        } catch (Exception e) {
            LOG.warn(MessageFormat.format(
                    "error occurred while unloading JDBC drivers: {0}",
                    loader), e);
        }
    }

    @Override
    public void run() {
        ClassLoader loader = getClass().getClassLoader();
        synchronized (JdbcDriverCleaner.class) {
            // DriverManager.getDrivers() may register a new JDBC driver instance in the current class loader
            DriverManager.getDrivers();

            // then we de-registers all JDBC drivers which the current class loader created
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == loader) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "unloading {0} ({1})",
                                driver,
                                loader));
                    }
                    try {
                        DriverManager.deregisterDriver(driver);
                    } catch (SQLException e) {
                        LOG.warn(MessageFormat.format(
                                "failed to unload {0} ({1})",
                                driver,
                                loader), e);
                    }
                }
            }
        }
    }
}
