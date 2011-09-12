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

/**
 * Int, String pair.
 */
public class Pair implements Comparable<Pair> {

    int key;

    String value;

    /**
     * Creates a new instance.
     */
    public Pair() {
        return;
    }

    Pair(int key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(Pair o) {
        if (key < o.key) {
            return -1;
        } else if (key > o.key) {
            return +1;
        } else {
            return value.compareTo(o.value);
        }
    }
}
