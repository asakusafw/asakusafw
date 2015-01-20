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
package com.asakusafw.runtime.compatibility;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.JobContext;

/**
 * Compatibility for core framework layer.
 * @since 0.7.0
 */
public final class CoreCompatibility {

    static final Log LOG = LogFactory.getLog(CoreCompatibility.class);

    /**
     * Represents the target version.
     */
    private static final FrameworkVersion TARGET = FrameworkVersion.DONT_CARE;

    private CoreCompatibility() {
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
     */
    public enum FrameworkVersion {

        /**
         * Don't care for temporary.
         */
        DONT_CARE("<don't-care>") {
            @Override
            public boolean isCompatibleTo(FrameworkVersion running) {
                // don't care
                return true;
            }
        },

        /**
         * Represents Hadoop {@code 1.x}.
         */
        HADOOP_V1("hadoop-1.x"),

        /**
         * Represents Hadoop {@code 2.x} with YARN.
         */
        HADOOP_V2("hadoop-2.x") {
            @Override
            public boolean isCompatibleTo(FrameworkVersion running) {
                return this == running || running == HADOOP_V2_MR1;
            }
        },

        /**
         * Represents Hadoop {@code 2.x} with {@code MRv1}.
         */
        HADOOP_V2_MR1("hadoop-2.x-MRv1"),

        /**
         * Represents unknown framework.
         */
        UNKNOWN("hadoop-<UNKNOWN_VERSION>"),
        ;

        private final String label;

        private FrameworkVersion(String label) {
            this.label = label;
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

                // Detect MRConfig
                if (existsClass("org.apache.hadoop.mapreduce.MRConfig")) {
                    detected = HADOOP_V2;
                } else if (JobContext.class.isInterface()) {
                    detected = HADOOP_V2_MR1;
                } else {
                    detected = HADOOP_V1;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Detected installed framework: {0}",
                            detected));
                }
                VERSION = detected;
            }

            private static boolean existsClass(String className) {
                ClassLoader loader = CoreCompatibility.class.getClassLoader();
                try {
                    Class.forName(className, false, loader);
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
        }
    }
}
