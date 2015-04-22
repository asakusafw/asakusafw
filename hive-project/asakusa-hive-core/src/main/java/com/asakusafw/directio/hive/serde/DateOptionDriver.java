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

import org.apache.hadoop.hive.serde2.io.DateWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.DateObjectInspector;

import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An implementation of {@link ValueDriver} for {@link DateOption}.
 * @since 0.7.0
 */
public class DateOptionDriver extends AbstractValueDriver {

    private final DateObjectInspector inspector;

    private final boolean primitive;

    /**
     * Creates a new instance.
     * @param inspector the object inspector
     */
    public DateOptionDriver(DateObjectInspector inspector) {
        this.inspector = inspector;
        this.primitive = inspector.preferWritable() == false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void set(ValueOption<?> target, Object value) {
        if (value == null) {
            target.setNull();
        } else if (primitive) {
            java.sql.Date entity = inspector.getPrimitiveJavaObject(value);
            setDate(target, entity);
        } else {
            DateWritable writable = inspector.getPrimitiveWritableObject(value);
            setDate(target, writable.get());
        }
    }

    @SuppressWarnings("deprecation")
    private void setDate(ValueOption<?> target, java.sql.Date entity) {
        ((DateOption) target).modify(DateUtil.getDayFromDate(entity));
    }
}
