/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import javax.lang.model.type.TypeMirror;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.DataModelMirror;
import com.asakusafw.compiler.operator.DataModelMirrorRepository;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.utils.collections.Lists;

/**
 * Aggregates repositories of {@link DataModelMirror} using Service Provider Interface.
 * @since 0.2.0
 */
public class SpiDataModelMirrorRepository implements DataModelMirrorRepository {

    static final Logger LOG = LoggerFactory.getLogger(SpiDataModelMirrorRepository.class);

    private final List<DataModelMirrorRepository> repositories;

    /**
     * Creates a new instance.
     * @param serviceLoader the service class loader
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SpiDataModelMirrorRepository(ClassLoader serviceLoader) {
        Precondition.checkMustNotBeNull(serviceLoader, "serviceLoader"); //$NON-NLS-1$
        this.repositories = loadRepositories(serviceLoader);
    }

    @Override
    public DataModelMirror load(OperatorCompilingEnvironment environment, TypeMirror type) {
        for (DataModelMirrorRepository repo : repositories) {
            DataModelMirror result = repo.load(environment, type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private List<DataModelMirrorRepository> loadRepositories(ClassLoader serviceLoader) {
        assert serviceLoader != null;
        LOG.debug("Loading data model mirror repositories");
        ServiceLoader<DataModelMirrorRepository> services = ServiceLoader.load(
                DataModelMirrorRepository.class,
                serviceLoader);
        List<DataModelMirrorRepository> results = Lists.create();
        for (DataModelMirrorRepository repo : services) {
            results.add(repo);
        }
        Collections.sort(results, new Comparator<DataModelMirrorRepository>() {
            @Override
            public int compare(DataModelMirrorRepository o1, DataModelMirrorRepository o2) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
        });
        return results;
    }
}
