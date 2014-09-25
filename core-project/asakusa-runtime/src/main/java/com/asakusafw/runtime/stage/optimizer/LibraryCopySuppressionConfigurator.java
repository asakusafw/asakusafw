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
package com.asakusafw.runtime.stage.optimizer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.stage.StageConfigurator;
import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;

/**
 * Configures to suppress library copies in local mode.
 * @since 0.7.0
 */
public class LibraryCopySuppressionConfigurator extends StageConfigurator {

    static final Log LOG = LogFactory.getLog(LibraryCopySuppressionConfigurator.class);

    /**
     * The configuration key of whether this feature is enabled or not.
     */
    public static final String KEY_ENABLED = "com.asakusafw.runtime.stage.optimizer.libraryCopySuppression.enabled";

    /**
     * The default configuration value of {@link #KEY_ENABLED}.
     */
    public static final boolean DEFAULT_ENABLED = false;

    static final String KEY_CONF_LIBRARIES = "tmpjars";

    static final Method CONFIGURATION_UNSET;

    static {
        Method method;
        try {
            method = Configuration.class.getMethod("unset", String.class);
        } catch (Exception e) {
            method = null;
        }
        CONFIGURATION_UNSET = method;
    }

    @Override
    public void configure(Job job) throws IOException, InterruptedException {
        Configuration conf = job.getConfiguration();
        if (conf.getBoolean(KEY_ENABLED, DEFAULT_ENABLED) == false) {
            return;
        }
        // activates only if application launcher is used
        if (conf.getBoolean(ApplicationLauncher.KEY_LAUNCHER_USED, false) == false) {
            return;
        }
        if (JobCompatibility.isLocalMode(job) == false) {
            return;
        }
        String libraries = conf.get(KEY_CONF_LIBRARIES);
        if (libraries == null || libraries.isEmpty()) {
            return;
        }
        Set<String> loaded = new HashSet<String>();
        ClassLoader loader = conf.getClassLoader();
        if (loader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) loader).getURLs()) {
                try {
                    loaded.add(url.toURI().toString());
                } catch (URISyntaxException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to analyze classpath: {0}",
                            url));
                }
            }
        }
        if (loaded.isEmpty()) {
            return;
        }
        StringBuilder result = new StringBuilder();
        for (String library : libraries.split(",")) {
            if (loaded.contains(library)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("Keep library: {0}", library));
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("Suppress library: {0}", library));
                }
                if (result.length() != 0) {
                    result.append(',');
                }
                result.append(library);
            }
        }
        if (result.length() > 0) {
            conf.set(KEY_CONF_LIBRARIES, result.toString());
        } else {
            if (CONFIGURATION_UNSET != null) {
                try {
                    CONFIGURATION_UNSET.invoke(conf, KEY_CONF_LIBRARIES);
                    return;
                } catch (Exception e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to invoke {0}",
                            CONFIGURATION_UNSET), e);
                }
            }
            String newLibraries = selectLibraries(libraries);
            conf.set(KEY_CONF_LIBRARIES, newLibraries);
        }
    }

    static String selectLibraries(String libraries) {
        String minLibrary = null;
        long minSize = Long.MAX_VALUE;
        for (String library : libraries.split(",")) {
            Path path = new Path(library);
            String scheme = path.toUri().getScheme();
            if (scheme != null && scheme.equals("file")) {
                File file = new File(path.toUri());
                long size = file.length();
                if (size < minSize) {
                    minLibrary = library;
                    minSize = size;
                }
            }
        }
        if (minLibrary != null) {
            return minLibrary;
        }
        return libraries;
    }
}
