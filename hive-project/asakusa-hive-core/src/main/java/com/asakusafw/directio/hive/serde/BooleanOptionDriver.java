/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.serde;

import org.apache.hadoop.hive.serde2.objectinspector.primitive.BooleanObjectInspector;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An implementation of {@link ValueDriver} for {@link BooleanOption}.
 * @since 0.7.0
 */
public class BooleanOptionDriver extends AbstractValueDriver {

    private final BooleanObjectInspector inspector;

    /**
     * Creates a new instance.
     * @param inspector the object inspector
     */
    public BooleanOptionDriver(BooleanObjectInspector inspector) {
        this.inspector = inspector;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void set(ValueOption<?> target, Object value) {
        if (value == null) {
            target.setNull();
        } else {
            ((BooleanOption) target).modify(inspector.get(value));
        }
    }

}
