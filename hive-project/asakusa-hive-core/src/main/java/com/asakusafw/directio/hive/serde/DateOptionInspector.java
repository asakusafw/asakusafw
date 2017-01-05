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

import org.apache.hadoop.hive.serde2.io.DateWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.DateObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link DateOption} object.
 * @since 0.7.0
 */
public class DateOptionInspector extends AbstractValueInspector implements DateObjectInspector {

    private static final int EPOCH_OFFSET = DateUtil.getDayFromDate(1970, 1, 1);

    /**
     * Creates a new instance.
     */
    public DateOptionInspector() {
        super(TypeInfoFactory.dateTypeInfo);
    }

    @Override
    protected ValueOption<?> newObject() {
        return new DateOption();
    }

    @Override
    public java.sql.Date getPrimitiveJavaObject(Object o) {
        DateOption object = (DateOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        Date value = object.get();

        // FIXME for optimization
        @SuppressWarnings("deprecation")
        java.sql.Date result = new java.sql.Date(value.getYear() - 1900, value.getMonth() - 1, value.getDay());
        return result;
    }

    @Override
    public DateWritable getPrimitiveWritableObject(Object o) {
        DateOption object = (DateOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        Date value = object.get();
        return new DateWritable(value.getElapsedDays() - EPOCH_OFFSET);
    }
}
