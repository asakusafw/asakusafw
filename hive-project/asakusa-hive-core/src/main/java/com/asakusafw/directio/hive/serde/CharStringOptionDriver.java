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
package com.asakusafw.directio.hive.serde;

import org.apache.hadoop.hive.common.type.HiveBaseChar;
import org.apache.hadoop.hive.serde2.io.HiveBaseCharWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveCharObjectInspector;

import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An implementation of {@link ValueDriver} for {@link StringOption} using {@code char}.
 * @since 0.7.0
 */
public class CharStringOptionDriver extends AbstractValueDriver {

    private final HiveCharObjectInspector inspector;

    private final boolean primitive;

    /**
     * Creates a new instance.
     * @param inspector the object inspector
     */
    public CharStringOptionDriver(HiveCharObjectInspector inspector) {
        this.inspector = inspector;
        this.primitive = inspector.preferWritable() == false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void set(ValueOption<?> target, Object value) {
        if (value == null) {
            target.setNull();
        } else if (primitive) {
            HiveBaseChar entity = inspector.getPrimitiveJavaObject(value);
            ((StringOption) target).modify(entity.getValue());
        } else {
            HiveBaseCharWritable writable = inspector.getPrimitiveWritableObject(value);
            ((StringOption) target).modify(writable.getTextValue());
        }
    }
}
