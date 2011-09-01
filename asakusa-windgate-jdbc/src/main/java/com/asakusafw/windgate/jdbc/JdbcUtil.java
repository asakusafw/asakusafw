/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.windgate.jdbc;

import java.util.Iterator;

/**
 * Common utility classes for this package.
 * @since 0.2.3
 */
class JdbcUtil {

    static String join(Iterable<String> list) {
        assert list != null;
        Iterator<String> iterator = list.iterator();
        assert iterator.hasNext();
        StringBuilder buf = new StringBuilder();
        buf.append(iterator.next());
        while (iterator.hasNext()) {
            buf.append(", ");
            buf.append(iterator.next());
        }
        return buf.toString();
    }
}
