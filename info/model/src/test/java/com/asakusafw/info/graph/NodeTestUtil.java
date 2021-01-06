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
package com.asakusafw.info.graph;

/**
 *
 * @since 0.9.2
 */
public final class NodeTestUtil {

    private NodeTestUtil() {
        return;
    }

    /**
     * Returns whether or not the contents of both nodes are equivalent.
     * @param a the first node
     * @param b the second node
     * @return {@code true} if they are equivalent, {@code false} otherwise
     */
    public static boolean contentEquals(Node a, Node b) {
        return a.info().equals(b.info());
    }
}
