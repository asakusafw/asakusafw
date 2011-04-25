/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClassRepository;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;

/**
 * Aggregates repositories of {@link DataClass} using Service Provider Interface.
 * @since 0.2.0
 */
public class SpiDataClassRepository extends FlowCompilingEnvironment.Initialized
        implements DataClassRepository {

    static final Logger LOG = LoggerFactory.getLogger(SpiDataClassRepository.class);

    private List<DataClassRepository> repositories;

    @Override
    protected void doInitialize() {
        LOG.info("データモデルクラスのプラグインを読み出します");
        List<DataClassRepository> results = new ArrayList<DataClassRepository>();
        ServiceLoader<DataClassRepository> services = ServiceLoader.load(
                DataClassRepository.class,
                getEnvironment().getServiceClassLoader());
        for (DataClassRepository repo : services) {
            assert repo.getClass().equals(this.getClass()) == false;
            repo.initialize(getEnvironment());
            LOG.debug("{0}が利用可能になります", repo.getClass().getName());
            results.add(repo);
        }
        Collections.sort(results, new Comparator<DataClassRepository>() {
            @Override
            public int compare(DataClassRepository o1, DataClassRepository o2) {
                String name1 = o1.getClass().getName();
                String name2 = o2.getClass().getName();
                return name1.compareTo(name2);
            }
        });
        this.repositories = results;
    }

    @Override
    public DataClass load(Type type) {
        Precondition.checkMustNotBeNull(type, "type");
        LOG.debug("Resolving {0} as a data model");
        for (DataClassRepository repository : repositories) {
            DataClass dataClass = repository.load(type);
            if (dataClass != null) {
                return dataClass;
            }
        }
        return null;
    }
}
