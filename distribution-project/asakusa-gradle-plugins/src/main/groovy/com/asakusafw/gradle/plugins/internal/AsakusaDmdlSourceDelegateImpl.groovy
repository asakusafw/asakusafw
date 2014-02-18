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
package com.asakusafw.gradle.plugins.internal

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver

import com.asakusafw.gradle.plugins.AsakusaDmdlSourceDelegate

/**
 * An implementation of {@link AsakusaDmdlSourceDelegate}.
 */
class AsakusaDmdlSourceDelegateImpl implements AsakusaDmdlSourceDelegate {

    private final SourceDirectorySet set

    /**
     * Creates a new instance.
     * @param fileResolver the file resolver
     */
    public AsakusaDmdlSourceDelegateImpl(FileResolver fileResolver) {
        this.set = new DefaultSourceDirectorySet(NAME, "DMDL scripts", fileResolver)
        this.set.filter.include "**/*.dmdl"
    }

    @Override
    public SourceDirectorySet getDmdl() {
        return set
    }

    @Override
    public AsakusaDmdlSourceDelegate dmdl(Closure<?> configureClosure) {
        ConfigureUtil.configure configureClosure, getDmdl()
        return this
    }
}
