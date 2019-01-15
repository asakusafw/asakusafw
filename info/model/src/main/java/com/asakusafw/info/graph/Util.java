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
package com.asakusafw.info.graph;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

final class Util {

    private Util() {
        return;
    }

    static <T> List<T> normalize(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list;
    }

    static void require(boolean condition, Supplier<String> message) {
        if (condition == false) {
            throw new IllegalStateException(message.get());
        }
    }
}
