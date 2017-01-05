/**
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
package com.asakusafw.compiler.testing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;

/**
 * Direct access API for {@link TemporaryInputDescription}.
 * @since 0.2.5
 */
public class DirectImporterDescription extends TemporaryInputDescription {

    private final Class<?> modelType;

    private final Set<String> paths;

    private DataSize dataSize;

    /**
     * Creates a new instance.
     * @param modelType the target data model type
     * @param paths the import target locations (relative from the base working area)
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public DirectImporterDescription(Class<?> modelType, Set<String> paths) {
        Precondition.checkMustNotBeNull(modelType, "modelType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(paths, "paths"); //$NON-NLS-1$
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("paths must not be empty"); //$NON-NLS-1$
        }
        this.modelType = modelType;
        this.paths = Sets.freeze(paths);
    }

    /**
     * Creates a new instance.
     * @param modelType the target data model type
     * @param path the import target location (relative from the base working area)
     * @param pathRest the rest of locations (relative from the base working area)
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public DirectImporterDescription(Class<?> modelType, String path, String... pathRest) {
        Precondition.checkMustNotBeNull(modelType, "modelType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(path, "path"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(pathRest, "pathRest"); //$NON-NLS-1$
        this.modelType = modelType;
        Set<String> pathSet = new HashSet<>();
        pathSet.add(path);
        Collections.addAll(pathSet, pathRest);
        this.paths = Collections.unmodifiableSet(pathSet);
    }

    @Override
    public Class<?> getModelType() {
        return modelType;
    }

    @Override
    public Set<String> getPaths() {
        return paths;
    }

    /**
     * Configures a data size hint for the target input.
     * @param dataSize the data size hint (nullable)
     */
    public void setDataSize(DataSize dataSize) {
        this.dataSize = dataSize;
    }

    @Override
    public DataSize getDataSize() {
        if (dataSize == null) {
            return DataSize.UNKNOWN;
        }
        return dataSize;
    }
}
