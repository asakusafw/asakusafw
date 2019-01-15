/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.BooleanWritable;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link BooleanOption} object.
 * @since 0.7.0
 */
public class BooleanOptionInspector extends AbstractValueInspector implements BooleanObjectInspector {

    /**
     * Creates a new instance.
     */
    public BooleanOptionInspector() {
        super(TypeInfoFactory.booleanTypeInfo);
    }

    @Override
    protected ValueOption<?> newObject() {
        return new BooleanOption();
    }

    @Override
    public Object getPrimitiveJavaObject(Object o) {
        BooleanOption object = (BooleanOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.get();
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        BooleanOption object = (BooleanOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new BooleanWritable(object.get());
    }

    @Override
    public boolean get(Object o) {
        return ((BooleanOption) o).get();
    }
}
