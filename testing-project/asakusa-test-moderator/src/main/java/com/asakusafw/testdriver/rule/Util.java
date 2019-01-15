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
package com.asakusafw.testdriver.rule;

import java.util.LinkedHashMap;
import java.util.Map;

import com.asakusafw.testdriver.core.Difference;

/**
 * Utility functions for this package.
 * @since 0.2.0
 */
final class Util {

    static String format(Object value) {
        return Difference.format(value);
    }

    static Map<Object, String> formatMap(Map<?, ?> map) {
        assert map != null;
        Map<Object, String> results = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            results.put(entry.getKey(), format(entry.getValue()));
        }
        return results;
    }

    private Util() {
        return;
    }
}
