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

import org.apache.hadoop.hive.serde2.io.TimestampWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.TimestampObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link DateTimeOption} object.
 * @since 0.7.0
 */
public class DateTimeOptionInspector extends AbstractValueInspector implements TimestampObjectInspector {

    /**
     * Creates a new instance.
     */
    public DateTimeOptionInspector() {
        super(TypeInfoFactory.timestampTypeInfo);
    }

    @Override
    protected ValueOption<?> newObject() {
        return new DateTimeOption();
    }

    @Override
    public java.sql.Timestamp getPrimitiveJavaObject(Object o) {
        DateTimeOption object = (DateTimeOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        DateTime value = object.get();

        // FIXME for optimization
        @SuppressWarnings("deprecation")
        java.sql.Timestamp result = new java.sql.Timestamp(
                value.getYear() - 1900, value.getMonth() - 1, value.getDay(),
                value.getHour(), value.getMinute(), value.getSecond(), 0);
        return result;
    }

    @Override
    public TimestampWritable getPrimitiveWritableObject(Object o) {
        java.sql.Timestamp value = getPrimitiveJavaObject(o);
        if (value == null) {
            return null;
        }
        return new TimestampWritable(value);
    }
}
