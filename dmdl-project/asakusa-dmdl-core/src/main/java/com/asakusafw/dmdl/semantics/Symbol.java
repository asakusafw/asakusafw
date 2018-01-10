/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.dmdl.semantics;

import com.asakusafw.dmdl.model.AstSimpleName;

/**
 * Super interface of the all reference symbols.
 * @param <D> type of referred declarations
 */
public interface Symbol<D extends Declaration> extends Element {

    /**
     * Returns the name of this symbol.
     * @return the name
     */
    AstSimpleName getName();

    /**
     * Returns the corresponding declaration.
     * @return the corresponding declaration, or {@code null} if is not exist
     */
    D findDeclaration();
}
