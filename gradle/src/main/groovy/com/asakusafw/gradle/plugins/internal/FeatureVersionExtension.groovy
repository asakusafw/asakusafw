/*
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.gradle.plugins.internal

import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * An extension to provide a lazy {@code version} property.
 * @since 0.9.0
 */
class FeatureVersionExtension {

    private final Object value

    FeatureVersionExtension(Object value) {
        this.value = value
    }

    /**
     * Returns the version string.
     * @return the version string
     */
    String getVersion() {
        return ResolutionUtils.resolveToString(value)
    }
}
