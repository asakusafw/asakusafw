/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.windgate;

import com.asakusafw.windgate.core.DriverScript;

/**
 * An abstract interface of WindGate importer/exporter process description.
 * @since 0.2.2
 */
public interface WindGateProcessDescription {

    /**
     * Returns the WindGate profile name to be used.
     * @return the WindGate profile name
     */
    String getProfileName();

    /**
     * Returns the driver script of this description.
     * @return the driver script
     * @throws IllegalStateException if failed to build a valid script
     */
    DriverScript getDriverScript();
}
