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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.model.DataModel;

/**
 * {@link ModelGenDataClass}を生成する{@code DataClass.Repository}の実装。
 */
public class ModelGenDataClassRepository
        extends FlowCompilingEnvironment.Initialized
        implements DataClass.Repository {

    static final Logger LOG = LoggerFactory.getLogger(ModelGenDataClassRepository.class);

    private Reference<Map<Type, DataClass>> cache;

    @Override
    protected void doInitialize() {
        cache = new SoftReference<Map<Type, DataClass>>(new HashMap<Type, DataClass>());
    }

    @Override
    public DataClass load(Type type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Map<Type, DataClass> cacheMap = cache.get();
        if (cacheMap != null) {
            if (cacheMap.containsKey(type)) {
                return cacheMap.get(type);
            }
        }

        if ((type instanceof Class<?>) == false) {
            return null;
        }
        Class<?> aClass = (Class<?>) type;
        if (DataModel.class.isAssignableFrom(aClass) == false) {
            LOG.debug("{}は{}のサブタイプでないため、スキップされます",
                    aClass.getName(),
                    DataModel.class.getName());
            return null;
        }

        ModelGenDataClass created = ModelGenDataClass.create(getEnvironment(), aClass);
        if (created == null) {
            return null;
        }

        if (cacheMap == null) {
            cacheMap = new HashMap<Type, DataClass>();
            cache = new SoftReference<Map<Type, DataClass>>(cacheMap);
        }
        cacheMap.put(type, created);

        return created;
    }

}
