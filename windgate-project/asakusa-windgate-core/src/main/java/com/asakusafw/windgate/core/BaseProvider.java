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
package com.asakusafw.windgate.core;

import java.io.IOException;

/**
 * Abstract superclass of any providers.
 * @param <T> type of acceptable profile
 * @since 0.2.2
 */
public abstract class BaseProvider<T> {

    /**
     * Configures this provider.
     * This method will be invoked just after this object is created.
     * @param profile the profile
     * @throws IOException if failed to configure this
     * @throws IllegalArgumentException if the specified profile is not valid
     */
    protected abstract void configure(T profile) throws IOException;
}
