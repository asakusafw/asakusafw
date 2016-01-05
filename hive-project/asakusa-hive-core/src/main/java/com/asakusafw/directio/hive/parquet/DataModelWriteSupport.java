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
package com.asakusafw.directio.hive.parquet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;

import parquet.hadoop.api.WriteSupport;
import parquet.io.api.RecordConsumer;
import parquet.schema.MessageType;
import parquet.schema.Type;

import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.PropertyDescriptor;
import com.asakusafw.directio.hive.serde.PropertyExtractor;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Consumes a data model into {@link RecordConsumer}.
 * @since 0.7.0
 */
public class DataModelWriteSupport extends WriteSupport<Object> {

    private final MessageType schema;

    private final Map<String, String> metadata;

    private final PropertyDescriptor[] properties;

    private final String[] names;

    private final ValueWriter[] drivers;

    private RecordConsumer recordConsumer;

    /**
     * Creates a new instance with empty extra-metadata.
     * @param descriptor the target data model descriptor
     */
    public DataModelWriteSupport(DataModelDescriptor descriptor) {
        this(descriptor, Collections.<String, String>emptyMap());
    }

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param metadata the extra metadata
     */
    public DataModelWriteSupport(DataModelDescriptor descriptor, Map<String, String> metadata) {
        if (descriptor.getPropertyDescriptors().isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("DataModelWriteSupport.errorEmptyDataModel"), //$NON-NLS-1$
                    descriptor.getDataModelClass().getName()));
        }
        this.schema = computeSchema(descriptor);
        this.metadata = metadata == null ? Collections.<String, String>emptyMap() : metadata;
        List<? extends PropertyDescriptor> props = descriptor.getPropertyDescriptors();
        this.properties = props.toArray(new PropertyDescriptor[props.size()]);
        this.names = new String[props.size()];
        this.drivers = new ValueWriter[props.size()];
        for (int i = 0, n = props.size(); i < n; i++) {
            PropertyDescriptor property = props.get(i);
            names[i] = property.getFieldName();
            drivers[i] = ParquetValueDrivers.of(property.getTypeInfo(), property.getValueClass()).getWriter();
        }
    }

    private MessageType computeSchema(DataModelDescriptor descriptor) {
        List<Type> fields = new ArrayList<>();
        for (PropertyDescriptor property : descriptor.getPropertyDescriptors()) {
            Type field = computeParquetType(property);
            fields.add(field);
        }
        return new MessageType(
                descriptor.getDataModelClass().getName(),
                fields);
    }

    private Type computeParquetType(PropertyDescriptor property) {
        ParquetValueDriver driver = ParquetValueDrivers.of(
                property.getTypeInfo(),
                property.getValueClass());
        return driver.getType(property.getFieldName());
    }

    @Override
    public WriteContext init(Configuration configuration) {
        return new WriteContext(schema, metadata);
    }

    @Override
    public void prepareForWrite(RecordConsumer consumer) {
        this.recordConsumer = consumer;
    }

    @Override
    public void write(Object value) {
        RecordConsumer consumer = recordConsumer;
        String[] ns = names;
        PropertyExtractor[] ps = properties;
        ValueWriter[] vs = drivers;
        consumer.startMessage();
        for (int index = 0, n = ns.length; index < n; index++) {
            ValueOption<?> property = ps[index].extract(value);
            if (property.isNull() == false) {
                String name = ns[index];
                consumer.startField(name, index);
                vs[index].write(property, consumer);
                consumer.endField(name, index);
            }
        }
        consumer.endMessage();
    }
}
