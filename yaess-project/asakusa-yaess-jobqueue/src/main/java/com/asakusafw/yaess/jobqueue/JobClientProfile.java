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
package com.asakusafw.yaess.jobqueue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.util.PropertiesUtil;
import com.asakusafw.yaess.jobqueue.client.HttpJobClient;
import com.asakusafw.yaess.jobqueue.client.JobClient;

/**
 * A structured profile for {@link QueueHadoopScriptHandler}.
 * @since 0.2.6
 */
public class JobClientProfile {

    static final Logger LOG = LoggerFactory.getLogger(JobClientProfile.class);

    static final String KEY_TIMEOUT = "timeout";

    static final String KEY_POLLING_INTERVAL = "pollingInterval";

    static final Pattern PATTERN_COMPONENT = Pattern.compile("\\d+");

    static final String KEY_URL = "url";

    static final String KEY_USER = "user";

    static final String KEY_PASSWORD = "password";

    static final long DEFAULT_TIMEOUT = 10000;

    static final long DEFAULT_POLLING_INTERVAL = 1000;

    private final String prefix;

    private final List<JobClient> clients;

    private final long timeout;

    private final long pollingInterval;

    /**
     * Creates a new instance.
     * @param prefix the profile namespace
     * @param clients clients
     * @param timeout timeout duration (ms)
     * @param pollingInterval polling interval (ms)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JobClientProfile(String prefix, List<? extends JobClient> clients, long timeout, long pollingInterval) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        if (clients == null) {
            throw new IllegalArgumentException("clients must not be null"); //$NON-NLS-1$
        }
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("clients must not be empty"); //$NON-NLS-1$
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be >= 0"); //$NON-NLS-1$
        }
        if (pollingInterval <= 0) {
            throw new IllegalArgumentException("pollingInterval must be >= 0"); //$NON-NLS-1$
        }
        this.prefix = prefix;
        this.clients = Collections.unmodifiableList(new ArrayList<JobClient>(clients));
        this.timeout = timeout;
        this.pollingInterval = pollingInterval;
    }

    /**
     * Returns the current namespace.
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the job clients.
     * @return the clients
     */
    public List<JobClient> getClients() {
        return clients;
    }

    /**
     * Returns the timeout duration.
     * @return the timeout (ms)
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Returns the polling interval.
     * @return the polling interval (ms)
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Converts general profile into the corresponded this profile.
     * @param profile general profile
     * @return this profile
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static JobClientProfile convert(ServiceProfile<?> profile) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        Map<String, String> conf = new HashMap<String, String>(profile.getConfiguration());
        conf.remove(ExecutionScriptHandler.KEY_RESOURCE);
        removeKeyPrefix(conf, ExecutionScriptHandler.KEY_PROP_PREFIX);
        long timeout = extractLong(profile, conf, KEY_TIMEOUT, DEFAULT_TIMEOUT);
        if (timeout <= 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Request time out must be > 0 ({0}.{1}={2})",
                    profile.getPrefix(),
                    KEY_TIMEOUT,
                    timeout));
        }
        long pollingInterval = extractLong(profile, conf, KEY_POLLING_INTERVAL, DEFAULT_POLLING_INTERVAL);
        if (pollingInterval <= 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Status polling interval must be > 0 ({0}.{1}={2})",
                    profile.getPrefix(),
                    KEY_TIMEOUT,
                    pollingInterval));
        }
        List<JobClient> clients = extractClients(profile, conf);
        if (clients.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "There must be one or more job clients ({0}.<n>)",
                    profile.getPrefix()));
        }
        return new JobClientProfile(profile.getPrefix(), clients, timeout, pollingInterval);
    }

    private static long extractLong(
            ServiceProfile<?> profile,
            Map<String, String> conf,
            String key,
            long defaultValue) {
        assert profile != null;
        assert conf != null;
        assert key != null;
        String value = profile.normalize(key, conf.remove(key), false, true);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0}.{1} must be an integer ({2})",
                    profile.getPrefix(),
                    key,
                    value));
        }
    }

    private static List<JobClient> extractClients(
            ServiceProfile<?> profile,
            Map<String, String> conf) {
        assert profile != null;
        Set<String> keys = PropertiesUtil.getChildKeys(conf, "", ".");
        Map<Integer, JobClient> results = new TreeMap<Integer, JobClient>();
        for (String key : keys) {
            if (isClientPrefix(key) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Unknown profile: {0}.{1}",
                        profile.getPrefix(),
                        key));
            }
            int number = Integer.parseInt(key);
            Map<String, String> subconf = PropertiesUtil.createPrefixMap(conf, key + ".");
            String prefix = profile.getPrefix() + "." + key;
            String url = resolve(profile, subconf, prefix, KEY_URL);
            if (url == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Target URL is not specified: {0}.{1}",
                        prefix,
                        KEY_URL));
            }
            String user = resolve(profile, subconf, prefix, KEY_USER);
            String password = resolve(profile, subconf, prefix, KEY_PASSWORD);
            if (user == null || user.isEmpty()) {
                results.put(number, new HttpJobClient(url));
            } else {
                password = password == null ? "" : password;
                results.put(number, new HttpJobClient(url, user, password));
            }
        }
        return new ArrayList<JobClient>(results.values());
    }

    private static String resolve(
            ServiceProfile<?> profile,
            Map<String, String> conf,
            String prefix,
            String key) {
        assert profile != null;
        assert conf != null;
        assert prefix != null;
        assert key != null;
        String value = conf.get(key);
        if (value == null) {
            return null;
        }
        return resolve(profile, prefix + "." + key, value);
    }

    private static String resolve(ServiceProfile<?> profile, String key, String value) {
        assert profile != null;
        assert key != null;
        assert value != null;
        try {
            return profile.getContext().getContextParameters().replace(value, true);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to resolve {0} ({1})",
                    key,
                    value), e);
        }
    }

    private static boolean isClientPrefix(String key) {
        assert key != null;
        return PATTERN_COMPONENT.matcher(key).matches();
    }

    private static void removeKeyPrefix(Map<?, ?> properties, String prefix) {
        assert properties != null;
        assert prefix != null;
        for (Iterator<?> iter = properties.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            if ((key instanceof String) == false) {
                continue;
            }
            String name = (String) key;
            if (name.startsWith(prefix)) {
                iter.remove();
            }
        }
    }
}
