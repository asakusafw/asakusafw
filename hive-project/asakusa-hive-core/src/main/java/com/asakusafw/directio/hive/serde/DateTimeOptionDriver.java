/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.TimestampObjectInspector;

import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An implementation of {@link ValueDriver} for {@link DateTimeOption}.
 * @since 0.7.0
 */
public class DateTimeOptionDriver extends AbstractValueDriver {

    private final TimestampObjectInspector inspector;

    private final boolean primitive;

    /**
     * Creates a new instance.
     * @param inspector the object inspector
     */
    public DateTimeOptionDriver(TimestampObjectInspector inspector) {
        this.inspector = inspector;
        this.primitive = inspector.preferWritable() == false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void set(ValueOption<?> target, Object value) {
        if (value == null) {
            target.setNull();
        } else if (primitive) {
            java.sql.Timestamp entity = inspector.getPrimitiveJavaObject(value);
            setDate(target, entity);
        } else {
            TimestampWritable writable = inspector.getPrimitiveWritableObject(value);
            setDate(target, writable.getTimestamp());
        }
    }

    @SuppressWarnings("deprecation")
    private void setDate(ValueOption<?> target, java.sql.Timestamp entity) {
        ((DateTimeOption) target).modify(DateUtil.getSecondFromDate(entity));
    }
}
