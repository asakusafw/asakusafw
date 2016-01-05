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

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;

import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects an Asakusa Framework data model object.
 * @since 0.7.0
 */
public class DataModelInspector extends StructObjectInspector {

    private final DataModelDescriptor descriptor;

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     */
    public DataModelInspector(DataModelDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public Category getCategory() {
        return Category.STRUCT;
    }

    @Override
    public String getTypeName() {
        return descriptor.getDataModelClass().getName();
    }

    @Override
    public List<? extends StructField> getAllStructFieldRefs() {
        return descriptor.getPropertyDescriptors();
    }

    @Override
    public Object getStructFieldData(Object data, StructField field) {
        PropertyDescriptor propertyDescriptor = (PropertyDescriptor) field;
        ValueOption<?> property = propertyDescriptor.extract(data);
        return property.isNull() ? null : property;
    }

    @Override
    public StructField getStructFieldRef(String name) {
        return descriptor.findPropertyDescriptor(name);
    }

    @Override
    public List<Object> getStructFieldsDataAsList(Object data) {
        List<? extends StructField> refs = getAllStructFieldRefs();
        List<Object> results = new ArrayList<>(refs.size());
        for (StructField field : refs) {
            results.add(getStructFieldData(data, field));
        }
        return results;
    }
}
