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
package com.asakusafw.directio.hive.parquet.v2;

import java.util.List;

import org.apache.parquet.io.api.Converter;
import org.apache.parquet.io.api.GroupConverter;

import com.asakusafw.directio.hive.serde.PropertyDescriptor;
import com.asakusafw.runtime.value.ValueOption;

/**
 * An implementation of parquet file data converter for Asakusa data models.
 * @since 0.7.0
 */
public class DataModelConverter extends GroupConverter {

    private final PropertyDescriptor[] properties;

    private Object currentObject;

    private final ValueOption<?>[] values;

    private final ValueConverter[] converters;

    /**
     * Creates a new instance.
     * @param properties the properties in the target data model
     */
    public DataModelConverter(List<? extends PropertyDescriptor> properties) {
        this.properties = properties.toArray(new PropertyDescriptor[properties.size()]);
        this.converters = new ValueConverter[properties.size()];
        this.values = new ValueOption<?>[properties.size()];
        for (int i = 0, n = this.properties.length; i < n; i++) {
            PropertyDescriptor property = this.properties[i];
            this.converters[i] = ParquetValueDrivers.of(property.getTypeInfo(), property.getValueClass())
                    .getConverter();
        }
    }

    /**
     * Sets the next target data model.
     * @param nextObject the next target data model
     */
    public void prepare(Object nextObject) {
        if (currentObject != nextObject) {
            PropertyDescriptor[] ps = properties;
            ValueOption<?>[] vs = values;
            ValueConverter[] cs = converters;
            for (int i = 0; i < ps.length; i++) {
                ValueOption<?> value = ps[i].extract(nextObject);
                vs[i] = value;
                cs[i].set(value);
            }
            currentObject = nextObject;
        }
    }

    /**
     * Returns the current object.
     * @return the current object
     */
    public Object getCurrentObject() {
        return currentObject;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void start() {
        ValueOption<?>[] vs = values;
        for (int i = 0; i < vs.length; i++) {
            ValueOption<?> v = vs[i];
            if (v != null) {
                v.setNull();
            }
        }
    }

    @Override
    public void end() {
        // nothing to do
    }

    @Override
    public Converter getConverter(int index) {
        return converters[index];
    }
}
