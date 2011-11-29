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
package com.asakusafw.yaess.core;

import java.io.IOException;

/**
 * A common interface for configurable services.
 * @since 0.2.3
 * @see ServiceProfile
 */
public interface Service {

    /**
     * Configures this service.
     * This method will be invoked only once when this instance is created from the profile.
     * @param profile the profile of this service
     * @throws InterruptedException if interrupted in configuration
     * @throws IOException if failed to configure this service
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    void configure(ServiceProfile<?> profile) throws InterruptedException, IOException;
}
