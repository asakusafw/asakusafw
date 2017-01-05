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

import java.text.MessageFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.Location;

/**
 * Direct access API for {@link TemporaryOutputDescription}.
 * @since 0.2.5
 */
public class DirectExporterDescription extends TemporaryOutputDescription {

    private final Class<?> modelType;

    private final String pathPrefix;

    /**
     * Creates a new instance.
     * @param modelType the target data model type
     * @param pathPrefix the relative path prefix of the export target file (must be ends with {@code -*})
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public DirectExporterDescription(Class<?> modelType, String pathPrefix) {
        Precondition.checkMustNotBeNull(modelType, "modelType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(pathPrefix, "pathPrefix"); //$NON-NLS-1$
        if (Location.fromPath(pathPrefix, '/').isPrefix() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "pathPrefix must be an path prefix (ends with {1}): {0}", //$NON-NLS-1$
                    pathPrefix,
                    Location.WILDCARD_SUFFIX));
        }
        this.modelType = modelType;
        this.pathPrefix = pathPrefix;
    }

    @Override
    public Class<?> getModelType() {
        return modelType;
    }

    @Override
    public String getPathPrefix() {
        return pathPrefix;
    }
}
