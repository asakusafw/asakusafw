/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.directio;

import com.asakusafw.runtime.io.util.WritableRawComparable;
import com.asakusafw.runtime.io.util.WritableRawComparableTuple;

/**
 * A class for ordering in direct output.
 * @since 0.2.5
 */
public abstract class DirectOutputOrder extends WritableRawComparableTuple {

    /**
     * An empty order.
     */
    public static final DirectOutputOrder EMPTY = new DirectOutputOrder() {
        @Override
        public void set(Object object) {
            return;
        }
    };

    /**
     * Creates a new instance.
     * @param objects element objects
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected DirectOutputOrder(WritableRawComparable... objects) {
        super(objects);
    }

    /**
     * Sets object.
     * @param object the object
     */
    public abstract void set(Object object);
}
