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

import org.apache.hadoop.hive.common.type.HiveVarchar;
import org.apache.hadoop.hive.serde2.io.HiveVarcharWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveVarcharObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.hive.serde2.typeinfo.VarcharTypeInfo;

import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link StringOption} object.
 * @since 0.7.0
 */
public class VarcharStringOptionInspector extends AbstractValueInspector implements HiveVarcharObjectInspector {

    private final int length;

    /**
     * Creates a new instance.
     */
    public VarcharStringOptionInspector() {
        this((VarcharTypeInfo) TypeInfoFactory.varcharTypeInfo);
    }

    /**
     * Creates a new instance.
     * @param length character string length
     */
    public VarcharStringOptionInspector(int length) {
        this(TypeInfoFactory.getVarcharTypeInfo(length));
    }

    private VarcharStringOptionInspector(VarcharTypeInfo info) {
        super(info);
        this.length = info.getLength();
    }

    @Override
    protected ValueOption<?> newObject() {
        return new StringOption();
    }

    @Override
    public HiveVarchar getPrimitiveJavaObject(Object o) {
        StringOption object = (StringOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return new HiveVarchar(object.getAsString(), length);
    }

    @Override
    public HiveVarcharWritable getPrimitiveWritableObject(Object o) {
        StringOption object = (StringOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        HiveVarcharWritable writable = new HiveVarcharWritable();
        writable.getTextValue().set(object.get());
        return writable;
    }

    @Override
    public boolean preferWritable() {
        return true;
    }
}
