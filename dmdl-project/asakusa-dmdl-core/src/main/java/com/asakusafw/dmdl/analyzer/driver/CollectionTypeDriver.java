/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.dmdl.analyzer.driver;

import com.asakusafw.dmdl.model.AstCollectionType;
import com.asakusafw.dmdl.model.AstType;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.CollectionType;
import com.asakusafw.dmdl.spi.TypeDriver;

/**
 * Resolves {@link AstCollectionType} elements.
 * @since 0.9.2
 */
public class CollectionTypeDriver implements TypeDriver {

    @Override
    public CollectionType resolve(Context context, AstType node) {
        if (node instanceof AstCollectionType) {
            AstCollectionType collection = (AstCollectionType) node;
            CollectionType.CollectionKind kind = resolveKind(collection.kind);
            Type elementType = context.resolve(collection.elementType);
            if (elementType == null) {
                return null;
            }
            return new CollectionType(collection, kind, elementType);
        }
        return null;
    }

    private static CollectionType.CollectionKind resolveKind(AstCollectionType.CollectionKind kind) {
        switch (kind) {
        case LIST:
            return CollectionType.CollectionKind.LIST;
        case MAP:
            return CollectionType.CollectionKind.MAP;
        default:
            throw new AssertionError(kind);
        }
    }
}
