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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * このパッケージ共通のユーティリティ。
 */
final class Util {

    private Util() {
        return;
    }

    static void notNull(Object reference, String name) {
        if (reference == null) {
            throw new IllegalArgumentException(name + " must not be null"); //$NON-NLS-1$
        }
    }

    static void notContainNull(Iterable<?> references, String name) {
        for (Object o : references) {
            notNull(o, name);
        }
    }

    static <T> List<T> freeze(List<? extends T> list) {
        return Collections.unmodifiableList(new ArrayList<T>(list));
    }

    static void notEmpty(Collection<?> collection, String name) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty"); //$NON-NLS-1$
        }
    }
}
