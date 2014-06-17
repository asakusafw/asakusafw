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

import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.value.StringOption;

/**
 * Inspects {@link StringOption} object.
 * @since 0.7.0
 */
public class StringOptionInspector extends AbstractValueInspector implements StringObjectInspector {

    /**
     * Creates a new instance.
     */
    public StringOptionInspector() {
        super(TypeInfoFactory.stringTypeInfo);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object copyObject(Object o) {
        StringOption object = (StringOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        StringOption copy = new StringOption();
        copy.copyFrom(object);
        return copy;
    }

    @Override
    public String getPrimitiveJavaObject(Object o) {
        StringOption object = (StringOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.getAsString();
    }

    @Override
    public Text getPrimitiveWritableObject(Object o) {
        StringOption object = (StringOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return object.get();
    }

    @Override
    public boolean preferWritable() {
        return true;
    }
}
