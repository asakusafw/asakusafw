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

import org.apache.hadoop.hive.serde2.objectinspector.primitive.LongObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.LongWritable;

import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link LongOption} object.
 * @since 0.7.0
 */
public class LongOptionInspector extends AbstractValueInspector implements LongObjectInspector {

    /**
     * Creates a new instance.
     */
    public LongOptionInspector() {
        super(TypeInfoFactory.longTypeInfo);
    }

    @Override
    protected ValueOption<?> newObject() {
        return new LongOption();
    }

    @Override
    public Object getPrimitiveJavaObject(Object o) {
        LongOption object = (LongOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.get();
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        LongOption object = (LongOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new LongWritable(object.get());
    }

    @Override
    public long get(Object o) {
        return ((LongOption) o).get();
    }
}
