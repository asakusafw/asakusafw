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

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.ConfigureUtil

import com.asakusafw.gradle.plugins.AsakusaSourceSetExtension

/**
 * An implementation of {@link AsakusaSourceSetExtension}.
 * @since 0.6.1
 */
class DefaultAsakusaSourceSetExtension implements AsakusaSourceSetExtension {

    final SourceDirectorySet libsSource

    final SourceDirectorySet dmdlSource

    final SourceDirectorySet thunderGateDdlSource

    /**
     * Creates a new instance.
     * @param project the extension target project
     * @param target the extension target source set
     * @param fileResolver the file resolver
     */
    public DefaultAsakusaSourceSetExtension(Project project, SourceSet target, FileResolver fileResolver) {
        this.libsSource = createLibs(target, fileResolver)
        this.dmdlSource = createDmdl(target, fileResolver)
        this.thunderGateDdlSource = createThunderGateDdl(target, fileResolver)
    }

    private SourceDirectorySet createLibs(SourceSet target, FileResolver fileResolver) {
        def set = new DefaultSourceDirectorySet('libs', "Application libraries", fileResolver)
        set.filter.include "*.jar"
        return set
    }

    private SourceDirectorySet createDmdl(SourceSet target, FileResolver fileResolver) {
        def set = new DefaultSourceDirectorySet('dmdl', "DMDL scripts", fileResolver)
        set.filter.include "**/*.dmdl"
        return set
    }

    private SourceDirectorySet createThunderGateDdl(SourceSet target, FileResolver fileResolver) {
        def set = new DefaultSourceDirectorySet('thudergateDdl', "ThunderGate DDL scripts", fileResolver)
        set.filter.include "**/*.sql"
        return set
    }

    @Override
    public SourceDirectorySet getLibs() {
        return libsSource;
    }

    @Override
    public SourceDirectorySet getDmdl() {
        return dmdlSource
    }

    @Override
    public SourceDirectorySet getThundergateDdl() {
        return thunderGateDdlSource;
    }

    @Override
    public AsakusaSourceSetExtension libs(Closure<?> configureClosure) {
        ConfigureUtil.configure configureClosure, getLibs()
        return this
    }

    @Override
    public AsakusaSourceSetExtension dmdl(Closure<?> configureClosure) {
        ConfigureUtil.configure configureClosure, getDmdl()
        return this
    }

    @Override
    public AsakusaSourceSetExtension thundergateDdl(Closure<?> configureClosure) {
        ConfigureUtil.configure configureClosure, getThundergateDdl()
        return this
    }
}
