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

import com.asakusafw.runtime.io.util.ShuffleKey;
import com.asakusafw.runtime.io.util.WritableRawComparableUnion;

/**
 * An abstract implementation of key object in direct output stages.
 * @since 0.2.5
 */
public abstract class AbstractDirectOutputKey
        extends ShuffleKey<WritableRawComparableUnion, WritableRawComparableUnion> {

    /**
     * Creates a new instance.
     * @param specs spec objects
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected AbstractDirectOutputKey(DirectOutputSpec... specs) {
        super(DirectOutputSpec.createGroupUnion(specs), DirectOutputSpec.createOrderUnion(specs));
    }

    /**
     * Sets new position.
     * @param position the new position
     */
    public void setPosition(int position) {
        getGroupObject().switchObject(position);
        getOrderObject().switchObject(position);
    }

    /**
     * Sets an object for the current position.
     * @param value the object
     */
    public void setObject(Object value) {
        DirectOutputGroup groupValue = (DirectOutputGroup) getGroupObject().getObject();
        groupValue.set(value);
        DirectOutputOrder orderValue = (DirectOutputOrder) getOrderObject().getObject();
        orderValue.set(value);
    }
}
