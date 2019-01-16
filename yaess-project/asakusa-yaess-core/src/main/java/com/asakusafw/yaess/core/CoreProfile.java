/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * An abstract implementation of core configuration holder.
 * @since 0.2.3
 */
public abstract class CoreProfile implements Service {

    /**
     * The configuration key prefix of {@link #getVersion()}.
     */
    public static final String KEY_VERSION = "version";

    private volatile String version;

    @Override
    public final void configure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        try {
            configureVersion(profile);
            doConfigure(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure \"{0}\" ({1})",
                    profile.getPrefix(),
                    profile.getServiceClass().getName()), e);
        }
    }

    private void configureVersion(ServiceProfile<?> profile) {
        assert profile != null;
        this.version = profile.getConfiguration(KEY_VERSION, true, false);
    }

    /**
     * Configures this service internally (extention point).
     * @param profile the profile of this service
     * @throws InterruptedException if interrupted in configuration
     * @throws IOException if failed to configure this service
     */
    protected void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        return;
    }
    /**
     * Returns the profile version.
     * @return the profile version
     */
    public String getVersion() {
        return version;
    }
}
