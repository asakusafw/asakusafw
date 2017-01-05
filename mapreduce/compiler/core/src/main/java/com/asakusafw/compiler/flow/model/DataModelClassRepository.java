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
package com.asakusafw.compiler.flow.model;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClassRepository;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;

/**
 * An implementation of {@link DataClassRepository} for basic data model classes.
 */
public class DataModelClassRepository
        extends FlowCompilingEnvironment.Initialized
        implements DataClassRepository {

    private static final String KIND = "DMDL"; //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(DataModelClassRepository.class);

    private Reference<Map<Type, DataClass>> cache;

    @Override
    protected void doInitialize() {
        cache = new SoftReference<>(new HashMap<>());
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
        if (isSuitable(aClass) == false) {
            return null;
        }
        DataModelClass created = DataModelClass.create(getEnvironment(), aClass);
        if (cacheMap == null) {
            cacheMap = new HashMap<>();
            cache = new SoftReference<>(cacheMap);
        }
        cacheMap.put(type, created);

        return created;
    }

    private boolean isSuitable(Class<?> aClass) {
        assert aClass != null;
        if (DataModel.class.isAssignableFrom(aClass) == false) {
            LOG.debug("{} is not a data-model class: should be subtype of {}", //$NON-NLS-1$
                    aClass.getName(),
                    DataModel.class.getName());
            return false;
        }
        DataModelKind kind = aClass.getAnnotation(DataModelKind.class);
        if (kind == null) {
            LOG.debug("{} is not a data-model class: shoud be annotated with {}", //$NON-NLS-1$
                    aClass.getName(),
                    DataModelKind.class.getName());
            return false;
        }
        if (kind.value().equals(KIND) == false) {
            LOG.debug("{}is not a data-model class: should be with @{}(\"{}\")", new Object[] { //$NON-NLS-1$
                    aClass.getName(),
                    DataModelKind.class.getName(),
                    KIND,
            });
            return false;
        }
        return true;
    }
}
