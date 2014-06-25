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

import org.apache.hadoop.hive.serde2.io.ShortWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.ShortObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.runtime.value.ShortOption;

/**
 * Inspects {@link ShortOption} object.
 * @since 0.7.0
 */
public class ShortOptionInspector extends AbstractValueInspector implements ShortObjectInspector {

    /**
     * Creates a new instance.
     */
    public ShortOptionInspector() {
        super(TypeInfoFactory.shortTypeInfo);
    }

    @Override
    public Object copyObject(Object o) {
        ShortOption object = (ShortOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new ShortOption(object.get());
    }

    @Override
    public Object getPrimitiveJavaObject(Object o) {
        ShortOption object = (ShortOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.get();
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        ShortOption object = (ShortOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new ShortWritable(object.get());
    }

    @Override
    public short get(Object o) {
        return ((ShortOption) o).get();
    }
}
