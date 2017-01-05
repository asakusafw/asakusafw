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
package com.asakusafw.compiler.operator.model;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.DataModelMirror;
import com.asakusafw.compiler.operator.DataModelMirrorRepository;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;

/**
 * Implementation of the default {@link DataModelMirror} repository.
 * @since 0.2.0
 */
public class DefaultDataModelMirrorRepository implements DataModelMirrorRepository {

    @Override
    public DataModelMirror load(OperatorCompilingEnvironment environment, TypeMirror type) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        if (Util.isConcrete(environment, type)) {
            return new ConcreteDataModelMirror(environment, (DeclaredType) type);
        } else if (Util.isPartial(environment, type)) {
            return new PartialDataModelMirror(environment, (TypeVariable) type);
        }
        return null;
    }
}
