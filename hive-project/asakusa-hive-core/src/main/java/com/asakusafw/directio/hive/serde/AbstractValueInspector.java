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

import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;

import com.asakusafw.runtime.value.ValueOption;

/**
 * An abstract implementation of {@link AbstractPrimitiveObjectInspector}.
 * @since 0.7.0
 */
public abstract class AbstractValueInspector extends AbstractPrimitiveObjectInspector {

    /**
     * Creates a new instance.
     * @param typeInfo the primitive type info
     */
    protected AbstractValueInspector(PrimitiveTypeInfo typeInfo) {
        super(typeInfo);
    }

    @Override
    public boolean preferWritable() {
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
    @Override
    public final Object copyObject(Object o) {
        ValueOption<?> object = (ValueOption<?>) o;
        if (object == null || object.isNull()) {
            return null;
        }
        ValueOption copy = newObject();
        copy.copyFrom(object);
        return copy;
    }

    /**
     * Creates a new instance for supported {@link ValueOption} type.
     * @return the created instance
     */
    protected abstract ValueOption<?> newObject();
}
