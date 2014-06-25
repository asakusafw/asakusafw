/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.hive.common;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * {@link Name} computer.
 * @since 0.7.0
 */
public interface Namer {

    /**
     * Returns the related name for the target model.
     * @param context the current context
     * @param model the target data model
     * @return the computed name
     */
    Name computeName(EmitContext context, ModelDeclaration model);
}
