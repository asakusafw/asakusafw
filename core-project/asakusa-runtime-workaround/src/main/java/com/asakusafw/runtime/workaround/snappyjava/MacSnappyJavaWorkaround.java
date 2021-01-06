/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.workaround.snappyjava;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.xerial.snappy.OSInfo;
import org.xerial.snappy.SnappyLoader;

import com.asakusafw.runtime.stage.StageConfigurator;
import com.asakusafw.runtime.workaround.RuntimeWorkaround;

/**
 * Workaround for {@code snappy-java} on Mac OSX.
 * @since 0.8.0
 */
public class MacSnappyJavaWorkaround extends StageConfigurator {

    static final Log LOG = LogFactory.getLog(MacSnappyJavaWorkaround.class);

    private static final String KEY_PREFIX = RuntimeWorkaround.KEY_PREFIX + "snappyjava."; //$NON-NLS-1$

    /**
     * The configuration key of whether this feature is enabled or not.
     */
    public static final String KEY_ENABLED = KEY_PREFIX + "enabled"; //$NON-NLS-1$

    /**
     * The configuration key of whether skip installing for unrecognized versions or not.
     */
    public static final String KEY_SKIP_ON_UNKNOWN = KEY_PREFIX + "skipOnUnknown"; //$NON-NLS-1$

    static final boolean DEFAULT_ENABLED = true;

    static final boolean DEFAULT_SKIP_ON_UNKNOWN = true;

    private static final Pattern PATTERN_VERSION =
            Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)([\\.\\-].+)?"); //$NON-NLS-1$

    /**
     * The fixed snappy-java version.
     * @see <a href="https://github.com/xerial/snappy-java/blob/develop/Milestone.md#snappy-java-111-m4-4-july-2014">
     *    snappy-java-1.1.1-M4
     * </a>
     */
    private static final int[] FIXED_VERSION = { 1, 1, 1 };

    @Override
    public void configure(Job job) throws IOException, InterruptedException {
        Configuration conf = job.getConfiguration();
        if (conf.getBoolean(KEY_ENABLED, DEFAULT_ENABLED) == false) {
            return;
        }
        install(conf.getBoolean(KEY_SKIP_ON_UNKNOWN, DEFAULT_SKIP_ON_UNKNOWN));
    }

    /**
     * Installs workaround for {@code snappy-java} if it is effective.
     */
    public static void install() {
        install(DEFAULT_SKIP_ON_UNKNOWN);
    }

    /**
     * Installs workaround for {@code snappy-java} if it is effective.
     * @param skipOnUnknown {@code true} to skip if version is unrecognized, otherwise {@code false}
     */
    public static void install(boolean skipOnUnknown) {
        String os = OSInfo.getOSName();
        if (os == null || os.equalsIgnoreCase("mac") == false) { //$NON-NLS-1$
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "not a snappy-java workaround target: os={0}", //$NON-NLS-1$
                        os));
            }
            return;
        }
        if (isFixed(skipOnUnknown)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "not a snappy-java workaround target: version={0}", //$NON-NLS-1$
                        SnappyLoader.getVersion()));
            }
            return;
        }
        // isFixed() -> SnappyLoader.<clinit> may change System.properties
        if (System.getProperty(SnappyLoader.KEY_SNAPPY_LIB_NAME) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "not a snappy-java workaround target: defined {0}={1}", //$NON-NLS-1$
                        SnappyLoader.KEY_SNAPPY_LIB_NAME,
                        System.getProperty(SnappyLoader.KEY_SNAPPY_LIB_NAME)));
            }
            return;
        }
        LOG.info(MessageFormat.format(
                "installing snappy-java workaround for Mac OSX: version={0}",
                SnappyLoader.getVersion()));
        System.setProperty(SnappyLoader.KEY_SNAPPY_LIB_NAME, "libsnappyjava.jnilib"); //$NON-NLS-1$
    }

    private static boolean isFixed(boolean skipOnUnknown) {
        String versionString = SnappyLoader.getVersion();
        Matcher matcher = PATTERN_VERSION.matcher(versionString);
        if (matcher.matches() == false) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "unrecognized snappy-java version: {0}", //$NON-NLS-1$
                        versionString));
            }
            return skipOnUnknown;
        }
        int[] version = {
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
        };
        int[] required = FIXED_VERSION;
        assert required.length == version.length;
        for (int i = 0; i < version.length; i++) {
            int v = version[i];
            int r = required[i];
            if (v > r) {
                return true;
            } else if (v < r) {
                return false;
            }
        }
        return true;
    }
}
