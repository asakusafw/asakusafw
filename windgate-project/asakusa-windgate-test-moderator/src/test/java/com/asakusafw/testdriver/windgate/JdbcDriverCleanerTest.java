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
package com.asakusafw.testdriver.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.h2.Driver;
import org.junit.Test;

/**
 * Test for {@link JdbcDriverCleaner}.
 */
public class JdbcDriverCleanerTest {

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        PluginClassLoader loader = new PluginClassLoader(getClass().getClassLoader());
        try {
            MockDriverBase driver = loader.loadDirect(MockDriver.class)
                    .asSubclass(MockDriverBase.class)
                    .newInstance();
            assertThat(driver.isRegistered(), is(false));
            driver.register();
            try {
                assertThat(driver.isRegistered(), is(true));
            } finally {
                JdbcDriverCleaner.runIn(loader);
            }
            assertThat(driver.isRegistered(), is(false));
        } finally {
            dispose(loader);
        }
    }

    /**
     * isolated between two identical class loaders.
     * @throws Exception if failed
     */
    @Test
    public void isolated() throws Exception {
        PluginClassLoader c1 = new PluginClassLoader(getClass().getClassLoader());
        try {
            PluginClassLoader c2 = new PluginClassLoader(getClass().getClassLoader());
            try {
                MockDriverBase d1 = c1.loadDirect(MockDriver.class)
                        .asSubclass(MockDriverBase.class)
                        .newInstance();
                MockDriverBase d2 = c2.loadDirect(MockDriver.class)
                        .asSubclass(MockDriverBase.class)
                        .newInstance();
                d1.register();
                d2.register();
                try {
                    assertThat(d1.isRegistered(), is(true));
                    assertThat(d2.isRegistered(), is(true));

                    JdbcDriverCleaner.runIn(c1);
                    assertThat(d1.isRegistered(), is(false));
                    assertThat(d2.isRegistered(), is(true));
                } finally {
                    JdbcDriverCleaner.runIn(c1);
                    JdbcDriverCleaner.runIn(c2);
                }
                assertThat(d1.isRegistered(), is(false));
                assertThat(d2.isRegistered(), is(false));
            } finally {
                dispose(c2);
            }
        } finally {
            dispose(c1);
        }
    }

    /**
     * should fail if {@link JdbcDriverCleaner} is not instantiated by {@link PluginClassLoader}.
     */
    @Test
    public void instantiate_by_application() {
        try {
            JdbcDriverCleaner cleaner = new JdbcDriverCleaner();
            fail("should not enable to create instance: " + cleaner);
        } catch (IllegalStateException e) {
            // ok.
        }
    }

    private void dispose(Object object) throws IOException {
        if (object instanceof Closeable) {
            ((Closeable) object).close();
        }
    }

    @SuppressWarnings("all")
    public interface DriverExtensionJdk17 {
        Logger getParentLogger() throws SQLFeatureNotSupportedException;
    }

    @SuppressWarnings("all")
    public static abstract class MockDriverBase extends Driver implements DriverExtensionJdk17 {

        public abstract boolean isRegistered();

        public abstract void register();

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
    }

    @SuppressWarnings("all")
    public static final class MockDriver extends MockDriverBase {

        public MockDriver() {
            assert getClass().getClassLoader() instanceof PluginClassLoader;
        }

        @Override
        public boolean acceptsURL(String url) {
            return url.startsWith("jdbc:testing:");
        }

        @Override
        public boolean isRegistered() {
            Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                if (drivers.nextElement() == this) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void register() {
            try {
                DriverManager.registerDriver(this);
            } catch (SQLException e) {
                throw new AssertionError(e);
            }
        }
    }
}
