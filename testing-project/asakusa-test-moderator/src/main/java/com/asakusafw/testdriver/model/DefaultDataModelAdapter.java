/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.model;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.testdriver.core.DataModelAdapter;
import com.asakusafw.testdriver.core.DataModelDefinition;

/**
 * Default implementation of {@link DataModelAdapter}
 * which can process {@link DataModel} classes from DMDL.
 * @since 0.2.0
 */
public class DefaultDataModelAdapter implements DataModelAdapter {

    private static final String KIND_NAME = "DMDL"; //$NON-NLS-1$

    @Override
    public <T> DataModelDefinition<T> get(Class<T> modelClass) {
        if (DataModel.class.isAssignableFrom(modelClass) == false) {
            return null;
        }
        DataModelKind kind = modelClass.getAnnotation(DataModelKind.class);
        if (kind == null || kind.value().equals(KIND_NAME) == false) {
            return null;
        }
        return new DefaultDataModelDefinition<>(modelClass);
    }
}
