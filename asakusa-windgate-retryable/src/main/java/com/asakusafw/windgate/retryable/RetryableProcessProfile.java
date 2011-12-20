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
package com.asakusafw.windgate.retryable;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.util.PropertiesUtil;

/**
 * A structured profile for {@link RetryableProcessProvider}.
 * @since 0.2.4
 */
public class RetryableProcessProfile {

    static final WindGateLogger WGLOG = new RetryableProcessLogger(RetryableProcessProfile.class);

    private static final char SEPARATOR = '.';

    /**
     * The profile key name of {@link #getRetryCount()}.
     */
    public static final String KEY_RETRY_COUNT = "retryCount";

    /**
     * The profile key name of {@link #getComponent() component process provider class name}.
     */
    public static final String KEY_COMPONENT = "component";

    /**
     * The profile key prefix of {@link #getComponent() component process provider configuration}.
     */
    public static final String PREFIX_COMPONENT = KEY_COMPONENT + SEPARATOR;

    private final ProcessProvider component;

    private final int retryCount;

    /**
     * Creates a new instance.
     * @param component the component process provider to be retried
     * @param retryCount the retry count
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public RetryableProcessProfile(ProcessProvider component, int retryCount) {
        if (component == null) {
            throw new IllegalArgumentException("component must not be null"); //$NON-NLS-1$
        }
        if (retryCount <= 0) {
            throw new IllegalArgumentException("retryCount must be >= 1"); //$NON-NLS-1$
        }
        this.component = component;
        this.retryCount = retryCount;
    }

    /**
     * Converts {@link ProcessProfile} into {@link RetryableProcessProfile}.
     * @param profile target profile
     * @return the converted profile
     * @throws IOException if failed to create a component process provider
     * @throws IllegalArgumentException if profile is not valid, or any parameter is {@code null}
     */
    public static RetryableProcessProfile convert(ProcessProfile profile) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        int retryCount = extractInt(profile, KEY_RETRY_COUNT, 1);
        String componentName = profile.getName() + '-' + KEY_COMPONENT;
        String componentClassName = extract(profile, KEY_COMPONENT);
        Class<? extends ProcessProvider> componentClass;
        try {
            Class<?> aClass = Class.forName(componentClassName, false, profile.getContext().getClassLoader());
            componentClass = aClass.asSubclass(ProcessProvider.class);
        } catch (Exception e) {
            WGLOG.error(e, "E00001",
                    profile.getName(),
                    componentClassName);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to create component process provider for \"{0}\": {1}",
                    profile.getName(),
                    componentClassName), e);
        }
        Map<String, String> conf = profile.getConfiguration();
        Map<String, String> componentConf = PropertiesUtil.createPrefixMap(conf, PREFIX_COMPONENT);
        ProcessProfile componentProfile = new ProcessProfile(
                componentName,
                componentClass,
                profile.getContext(),
                componentConf);
        ProcessProvider component;
        try {
            component = componentProfile.createProvider();
        } catch (IOException e) {
            WGLOG.error(e, "E00001",
                    profile.getName(),
                    componentClassName);
            throw e;
        }
        return new RetryableProcessProfile(component, retryCount);
    }

    private static String extract(ProcessProfile profile, String configKey) {
        assert profile != null;
        assert configKey != null;
        String value = profile.getConfiguration().get(configKey);
        if (value == null) {
            WGLOG.error("E00001",
                    profile.getName(),
                    configKey,
                    null);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Process \"{0}\" must declare \"{1}\"",
                    profile.getName(),
                    configKey));
        }
        return value.trim();
    }

    private static int extractInt(ProcessProfile profile, String key, int minimumValue) {
        assert profile != null;
        assert key != null;
        String valueString = extract(profile, key);
        int value;
        try {
            value = Integer.parseInt(valueString.trim());
        } catch (NumberFormatException e) {
            WGLOG.error("E00001",
                    profile.getName(),
                    key,
                    valueString);
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"{1}\" must be a valid number: {2} (process={0})",
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
                    "The \"{1}\" must be > 0: {2} (process={0})",
                    profile.getName(),
                    value,
                    valueString));
        }
        return value;
    }

    /**
     * Returns the target process provider.
     * @return component process provider
     */
    public ProcessProvider getComponent() {
        return component;
    }

    /**
     * Returns the max retry count.
     * @return the retry count
     */
    public int getRetryCount() {
        return retryCount;
    }
}
