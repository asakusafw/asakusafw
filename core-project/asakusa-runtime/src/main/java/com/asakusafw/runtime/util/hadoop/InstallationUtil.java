/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.JobContext;

/**
 * Utilities for Hadoop installations.
 * @since 0.9.0
 */
public final class InstallationUtil {

    static final Log LOG = LogFactory.getLog(InstallationUtil.class);

    private static final String PATH_HADOOP_INFO = "META-INF/asakusa-runtime/hadoop.properties"; //$NON-NLS-1$

    private static final String KEY_TARGET_VERSION = "version"; //$NON-NLS-1$

    /**
     * Represents the target version.
     */
    static final FrameworkVersion TARGET;
    static {
        FrameworkVersion detected = FrameworkVersion.DONT_CARE;
        try (InputStream input = InstallationUtil.class.getClassLoader().getResourceAsStream(PATH_HADOOP_INFO)) {
            if (input == null) {
                throw new FileNotFoundException(PATH_HADOOP_INFO);
            }
            Properties p = new Properties();
            try {
                p.load(input);
            } finally {
                input.close();
            }
            String value = p.getProperty(KEY_TARGET_VERSION);
            FrameworkVersion version = FrameworkVersion.find(value);
            if (value == null) {
                LOG.warn("target Hadoop version is not defined");
            } else if (version == null) {
                LOG.warn(MessageFormat.format(
                        "unknown target Hadoop version: {0}",
                        value));
            } else {
                detected = version;
                LOG.debug(MessageFormat.format(
                        "detect target Hadoop version: {1} ({0})", //$NON-NLS-1$
                        detected,
                        value));
            }
        } catch (IOException e) {
            LOG.warn("failed to detect current target Hadoop version", e);
            detected = FrameworkVersion.DONT_CARE;
        }
        TARGET = detected;
    }

    private InstallationUtil() {
        return;
    }

    /**
     * Verifies the running core framework version.
     */
    public static void verifyFrameworkVersion() {
        FrameworkVersion running = FrameworkVersion.get();
        if (TARGET.isCompatibleTo(running) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "Inconsistent environment version: expected-version={0}, installed-version={1}",
                    TARGET,
                    running));
        }
    }

    /**
     * Represents the core framework version.
     * @since 0.9.0
     */
    public enum FrameworkVersion {

        /**
         * Don't care for temporary.
         */
        DONT_CARE("<don't-care>", "\\$\\{.*\\}") { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean isCompatibleTo(FrameworkVersion running) {
                // don't care
                return true;
            }
        },

        /**
         * Represents Hadoop {@code 1.x}.
         */
        HADOOP_V1("hadoop-1.x", "1\\..*"), //$NON-NLS-1$ //$NON-NLS-2$

        /**
         * Represents Hadoop {@code 2.x} with YARN.
         */
        HADOOP_V2("hadoop-2.x", "2\\..*") { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean isCompatibleTo(FrameworkVersion running) {
                return this == running || running == HADOOP_V2_MR1;
            }
        },

        /**
         * Represents Hadoop {@code 2.x} with {@code MRv1}.
         */
        HADOOP_V2_MR1("hadoop-2.x-MRv1"), //$NON-NLS-1$

        /**
         * Represents unknown framework.
         */
        UNKNOWN("hadoop-<UNKNOWN_VERSION>"), //$NON-NLS-1$
        ;

        final String label;

        final Pattern pattern;

        FrameworkVersion(String label) {
            this(label, null);
        }

        FrameworkVersion(String label, String pattern) {
            this.label = label;
            this.pattern = pattern == null ? null : Pattern.compile(pattern);
        }

        /**
         * Returns whether or not this version is compatible to the specified running version.
         * @param running the running version
         * @return {@code true} if it is compatible for this version, otherwise {@code false}
         */
        public boolean isCompatibleTo(FrameworkVersion running) {
            return this == running;
        }

        /**
         * Returns a version constant from its name.
         * @param name the version name
         * @return the related version, or {@code null} if there is no such a version
         */
        public static FrameworkVersion find(String name) {
            for (FrameworkVersion version : values()) {
                if (version.isMember(name)) {
                    return version;
                }
            }
            return null;
        }

        boolean isMember(String name) {
            if (pattern != null) {
                return pattern.matcher(name).matches();
            }
            return false;
        }

        /**
         * Returns the currently linked framework version.
         * @return the currently linked version
         */
        public static FrameworkVersion get() {
            return Lazy.VERSION;
        }

        @Override
        public String toString() {
            return label;
        }

        private static final class Lazy {

            static final FrameworkVersion VERSION;
            static {
                FrameworkVersion detected = UNKNOWN;

                if (isHadoopV2()) {
                    detected = HADOOP_V2;
                } else if (isHadoopV2MR1()) {
                    detected = HADOOP_V2_MR1;
                } else {
                    detected = HADOOP_V1;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Detected installed framework: {0}", //$NON-NLS-1$
                            detected));
                }
                VERSION = detected;
            }

            private static boolean isHadoopV2() {
                // v1 - DistributedCache exists in hadoop-core
                // v2 - DistributedCache exists in hadoop-mapreduce-client-core, and it is deprecated
                try {
                    ClassLoader cl = InstallationUtil.class.getClassLoader();
                    Class<?> c = cl.loadClass("org.apache.hadoop.filecache.DistributedCache"); // //$NON-NLS-1$
                    return c.isAnnotationPresent(Deprecated.class);
                } catch (ClassNotFoundException e) {
                    return true;
                }
            }

            private static boolean isHadoopV2MR1() {
                // JobContext is interface in v2 even if the current environment is MRv1
                return JobContext.class.isInterface();
            }
        }
    }
}
