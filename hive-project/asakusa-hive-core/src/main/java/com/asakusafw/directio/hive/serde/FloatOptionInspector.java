/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import org.apache.hadoop.hive.serde2.objectinspector.primitive.FloatObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.FloatWritable;

import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link FloatOption} object.
 * @since 0.7.0
 */
public class FloatOptionInspector extends AbstractValueInspector implements FloatObjectInspector {

    /**
     * Creates a new instance.
     */
    public FloatOptionInspector() {
        super(TypeInfoFactory.floatTypeInfo);
    }

    @Override
    protected ValueOption<?> newObject() {
        return new FloatOption();
    }

    @Override
    public Object getPrimitiveJavaObject(Object o) {
        FloatOption object = (FloatOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.get();
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        FloatOption object = (FloatOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new FloatWritable(object.get());
    }

    @Override
    public float get(Object o) {
        return ((FloatOption) o).get();
    }
}
