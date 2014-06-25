/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import org.apache.hadoop.hive.serde2.io.ByteWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.ByteObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.runtime.value.ByteOption;

/**
 * Inspects {@link ByteOption} object.
 * @since 0.7.0
 */
public class ByteOptionInspector extends AbstractValueInspector implements ByteObjectInspector {

    /**
     * Creates a new instance.
     */
    public ByteOptionInspector() {
        super(TypeInfoFactory.byteTypeInfo);
    }

    @Override
    public Object copyObject(Object o) {
        ByteOption object = (ByteOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new ByteOption(object.get());
    }

    @Override
    public Object getPrimitiveJavaObject(Object o) {
        ByteOption object = (ByteOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.get();
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        ByteOption object = (ByteOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new ByteWritable(object.get());
    }

    @Override
    public byte get(Object o) {
        return ((ByteOption) o).get();
    }
}
