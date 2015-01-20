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

import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.IntWritable;

import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link IntOption} object.
 * @since 0.7.0
 */
public class IntOptionInspector extends AbstractValueInspector implements IntObjectInspector {

    /**
     * Creates a new instance.
     */
    public IntOptionInspector() {
        super(TypeInfoFactory.intTypeInfo);
    }

    @Override
    protected ValueOption<?> newObject() {
        return new IntOption();
    }

    @Override
    public Object getPrimitiveJavaObject(Object o) {
        IntOption object = (IntOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.get();
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        IntOption object = (IntOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new IntWritable(object.get());
    }

    @Override
    public int get(Object o) {
        return ((IntOption) o).get();
    }
}
