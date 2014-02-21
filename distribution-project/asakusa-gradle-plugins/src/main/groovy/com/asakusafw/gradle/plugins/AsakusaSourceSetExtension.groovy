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
package com.asakusafw.gradle.plugins

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet;


/**
 * A {@link SourceSet} extension for Asakusa Framework.
 * @since 0.6.1
 */
interface AsakusaSourceSetExtension {

    /**
     * Returns the application libraries.
     * @return the application libraries
     */
    SourceDirectorySet getLibs()

    /**
     * Configures the application libraries.
     * @param configureClosure the closure that configures the application libraries
     * @return this
     */
    AsakusaSourceSetExtension libs(Closure<?> configureClosure)

    /**
     * Returns the DMDL source set.
     * @return the DMDL source set
     */
    SourceDirectorySet getDmdl()

    /**
     * Configures the DMDL source set.
     * @param configureClosure the closure that configures the DMDL source set
     * @return this
     */
    AsakusaSourceSetExtension dmdl(Closure<?> configureClosure)

    /**
     * Returns the ThunderGate DDL source set.
     * @return the ThunderGate DDL source set
     */
    SourceDirectorySet getThundergateDdl()

    /**
     * Configures the ThunderGate DDL source set.
     * @param configureClosure the closure that configures the ThunderGate DDL source set
     * @return this
     */
    AsakusaSourceSetExtension thundergateDdl(Closure<?> configureClosure)
}
