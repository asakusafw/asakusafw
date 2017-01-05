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
package com.asakusafw.testdriver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * An abstract super class of testers.
 * @since 0.7.3
 */
public class TesterBase extends TestDriverBase {

    private static final Logger LOG = LoggerFactory.getLogger(TesterBase.class);

    private final Map<ImporterDescription, DataModelSourceFactory> externalResources = new HashMap<>();

    /**
     * Creates a new instance.
     * @param callerClass the original test class
     */
    protected TesterBase(Class<?> callerClass) {
        super(callerClass);
    }

    /**
     * Puts initial data for the external resource.
     * This may be invoked from {@link DriverToolBase other collaborators}.
     * @param importer the importer description for accessing the target resource
     * @param source the source, or {@code null} to reset it
     */
    public final void putExternalResource(ImporterDescription importer, DataModelSourceFactory source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Prepare: importer={}, source={}", new Object[] { //$NON-NLS-1$
                    importer,
                    source,
            });
        }
        if (source == null) {
            externalResources.remove(importer);
        } else {
            externalResources.put(importer, source);
        }
    }

    /**
     * Returns initial data providers for the external resources.
     * @return the resources
     */
    protected Map<ImporterDescription, DataModelSourceFactory> getExternalResources() {
        return Collections.unmodifiableMap(externalResources);
    }
}
