/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.spi;

import com.asakusafw.dmdl.model.AstType;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.Type;

/**
 * Analyzes and resolve types in form of {@link AstType AST}.
 * <p>
 * To enhance DMDL compiler, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.dmdl.spi.TypeDriver}.
 * </p>
 */
public abstract class TypeDriver {

    /**
     * Processes and modifies the attributed declaration.
     * @param environment the processing environment
     * @param syntax the syntactic form of the type
     * @return the corresponded semantic form, or {@code null} if can not resolve
     */
    public abstract Type resolve(DmdlSemantics environment, AstType syntax);
}
