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
package com.asakusafw.operation.tools.directio;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

final class SimpleLoggerUtil {

    static final String NAME_RESOURCE_FILE = "simplelogger.properties";

    static final String OPT_PREFIX = "org.slf4j.simpleLogger.";

    static final String OPT_DEFAULT_LOG_LEVEL = OPT_PREFIX + "defaultLogLevel";

    static final String OPT_LOG_LEVEL_PREFIX = OPT_PREFIX + "log.";

    static final String OPT_SHOW_DATE_TIME = OPT_PREFIX + "showDateTime";

    static final String OPT_SHOW_THREAD_NAME = OPT_PREFIX + "showThreadName";

    static final String OPT_SHOW_LOG_NAME = OPT_PREFIX + "showLogName";

    static final String OPT_LEVEL_IN_BRACKETS = OPT_PREFIX + "levelInBrackets";

    static final String DEFAULT_DEFAULT_LOG_LEVEL = "warn";

    static final String DEFAULT_WORKFLOW_EXECUTOR_LOG_LEVEL = "info";

    static final String DEFAULT_SHOW_DATE_TIME = String.valueOf(false);

    static final String DEFAULT_SHOW_THREAD_NAME = String.valueOf(false);

    static final String DEFAULT_SHOW_LOG_NAME = String.valueOf(false);

    static final String DEFAULT_LEVEL_IN_BRACKETS = String.valueOf(true);

    static final Map<String, String> OPT_DEFAULTS;
    static {
        Map<String, String> map = new HashMap<>();
        map.put(OPT_DEFAULT_LOG_LEVEL, DEFAULT_DEFAULT_LOG_LEVEL);
        map.put(OPT_SHOW_DATE_TIME, DEFAULT_SHOW_DATE_TIME);
        map.put(OPT_SHOW_THREAD_NAME, DEFAULT_SHOW_THREAD_NAME);
        map.put(OPT_SHOW_LOG_NAME, DEFAULT_SHOW_LOG_NAME);
        map.put(OPT_LEVEL_IN_BRACKETS, DEFAULT_LEVEL_IN_BRACKETS);
        OPT_DEFAULTS = map;
    }

    private SimpleLoggerUtil() {
        return;
    }

    static void configure() {
        if (SimpleLoggerUtil.class.getClassLoader().getResource(NAME_RESOURCE_FILE) != null) {
            return;
        }
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Properties props = System.getProperties();
            OPT_DEFAULTS.forEach((k, v) -> props.putIfAbsent(k, v));
            System.setProperties(props);
            return null;
        });
    }
}
