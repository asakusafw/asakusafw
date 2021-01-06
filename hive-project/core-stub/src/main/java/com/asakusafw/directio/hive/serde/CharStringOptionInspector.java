/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import org.apache.hadoop.hive.common.type.HiveChar;
import org.apache.hadoop.hive.serde2.io.HiveCharWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveCharObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.CharTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link StringOption} object.
 * @since 0.7.0
 */
public class CharStringOptionInspector extends AbstractValueInspector implements HiveCharObjectInspector {

    private final int length;

    /**
     * Creates a new instance.
     */
    public CharStringOptionInspector() {
        this((CharTypeInfo) TypeInfoFactory.charTypeInfo);
    }

    /**
     * Creates a new instance.
     * @param length character string length
     */
    public CharStringOptionInspector(int length) {
        this(TypeInfoFactory.getCharTypeInfo(length));
    }

    private CharStringOptionInspector(CharTypeInfo info) {
        super(info);
        this.length = info.getLength();
    }

    @Override
    protected ValueOption<?> newObject() {
        return new StringOption();
    }

    @Override
    public HiveChar getPrimitiveJavaObject(Object o) {
        StringOption object = (StringOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new HiveChar(object.getAsString(), length);
    }

    @Override
    public HiveCharWritable getPrimitiveWritableObject(Object o) {
        StringOption object = (StringOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        HiveCharWritable writable = new HiveCharWritable();
        writable.getTextValue().set(object.get());
        return writable;
    }

    @Override
    public boolean preferWritable() {
        return true;
    }
}
