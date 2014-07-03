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
package com.asakusafw.directio.hive.parquet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parquet.column.ColumnDescriptor;
import parquet.io.api.GroupConverter;
import parquet.io.api.RecordMaterializer;
import parquet.schema.MessageType;
import parquet.schema.PrimitiveType;
import parquet.schema.Type;
import parquet.schema.Type.Repetition;

import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.PropertyDescriptor;

/**
 * An implementation of parquet record materializer for Asakusa data models.
 * @since 0.7.0
 */
public class DataModelMaterializer extends RecordMaterializer<Object> {

    static final Logger LOG = LoggerFactory.getLogger(DataModelMaterializer.class);

    private final MessageType materializeSchema;

    private final DataModelConverter root;

    /**
     * Creates a new instance.
     * @param descriptor the target descriptor
     * @param schema the file schema
     * @param configuration the mapping configuration
     */
    public DataModelMaterializer(
            DataModelDescriptor descriptor,
            MessageType schema,
            DataModelMapping configuration) {
        List<Mapping> mappings = computeMapping(descriptor, schema, configuration);
        List<Type> fields = new ArrayList<Type>();
        List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
        for (Mapping mapping : mappings) {
            if (mapping != null) {
                fields.add(new PrimitiveType(
                        Repetition.OPTIONAL,
                        mapping.source.getType(),
                        mapping.source.getPath()[0]));
                properties.add(mapping.target);
            }
        }
        this.materializeSchema = new MessageType(schema.getName(), fields);
        this.root = new DataModelConverter(properties);
    }

    /**
     * Returns the schema which this materializer is required.
     * @return the schema for materializer
     */
    public MessageType getMaterializeSchema() {
        return materializeSchema;
    }

    /**
     * Sets the next record.
     * @param object the next record object
     */
    public void setNextRecord(Object object) {
        root.prepare(object);
    }

    @Override
    public Object getCurrentRecord() {
        return root.getCurrentObject();
    }

    @Override
    public GroupConverter getRootConverter() {
        return root;
    }

    private List<Mapping> computeMapping(
            DataModelDescriptor descriptor,
            MessageType schema,
            DataModelMapping configuration) {
        List<Mapping> mappings;
        switch (configuration.getFieldMappingStrategy()) {
        case NAME:
            mappings = computeMappingByName(descriptor, schema);
            break;
        case POSITION:
            mappings = computeMappingByPosition(descriptor, schema);
            break;
        default:
            throw new AssertionError(configuration.getFieldMappingStrategy());
        }
        TreeMap<Integer, Mapping> propertyMap = new TreeMap<Integer, Mapping>();
        for (Mapping mapping : mappings) {
            if (checkMapping(descriptor, mapping, configuration)) {
                assert mapping.source != null;
                assert mapping.target != null;
                int index = schema.getFieldIndex(mapping.source.getPath()[0]);
                propertyMap.put(index, mapping);
            }
        }
        int lastIndex = -1;
        if (propertyMap.isEmpty() == false) {
            lastIndex = propertyMap.lastKey();
        }
        Mapping[] results = new Mapping[lastIndex + 1];
        for (Map.Entry<Integer, Mapping> entry : propertyMap.entrySet()) {
            results[entry.getKey()] = entry.getValue();
        }
        return Arrays.asList(results);
    }

    private static List<Mapping> computeMappingByName(
            DataModelDescriptor target, MessageType source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Mapping columns by their name: model={0}",
                    target.getDataModelClass().getName()));
        }
        Set<PropertyDescriptor> rest = new LinkedHashSet<PropertyDescriptor>(target.getPropertyDescriptors());
        List<Mapping> mappings = new ArrayList<Mapping>();
        for (ColumnDescriptor s : source.getColumns()) {
            String name = s.getPath()[0];
            PropertyDescriptor t = target.findPropertyDescriptor(name);
            if (t != null) {
                mappings.add(new Mapping(s, t));
                rest.remove(t);
            } else {
                mappings.add(new Mapping(s, null));
            }
        }
        for (PropertyDescriptor t : rest) {
            mappings.add(new Mapping(null, t));
        }
        return mappings;
    }

    private static List<Mapping> computeMappingByPosition(
            DataModelDescriptor target, MessageType source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Mapping columns by their position: model={0}",
                    target.getDataModelClass().getName()));
        }
        List<ColumnDescriptor> sources = source.getColumns();
        List<? extends PropertyDescriptor> targets = target.getPropertyDescriptors();
        List<Mapping> mappings = new ArrayList<Mapping>();
        int limit = Math.min(sources.size(), targets.size());
        for (int i = 0; i < limit; i++) {
            mappings.add(new Mapping(sources.get(i), targets.get(i)));
        }
        for (int i = limit, n = sources.size(); i < n; i++) {
            mappings.add(new Mapping(sources.get(i), null));
        }
        for (int i = limit, n = targets.size(); i < n; i++) {
            mappings.add(new Mapping(null, targets.get(i)));
        }
        return mappings;
    }

    private boolean checkMapping(
            DataModelDescriptor descriptor,
            Mapping mapping,
            DataModelMapping configuration) {
        assert mapping.source != null || mapping.target != null;
        if (mapping.source == null) {
            handleException(configuration.getOnMissingSource(), MessageFormat.format(
                    "Source field is not found: model={0}, field={1}:{2}",
                    descriptor.getDataModelClass().getName(),
                    mapping.target.getFieldName(),
                    mapping.target.getFieldObjectInspector().getTypeName()));
            return false;
        } else if (mapping.target == null) {
            handleException(configuration.getOnMissingTarget(), MessageFormat.format(
                    "Target field is not found: model={0}, field={1}:{2}(Parquet)",
                    descriptor.getDataModelClass().getName(),
                    mapping.source.getPath()[0],
                    mapping.source.getType()));
            return false;
        } else if (isCompatible(mapping.source, mapping.target) == false) {
            handleException(configuration.getOnIncompatibleType(), MessageFormat.format(
                    "Field type is incompatible: model={0}, source={1}:{2}(Parquet), target={3}:{4}",
                    descriptor.getDataModelClass().getName(),
                    mapping.source.getPath()[0],
                    mapping.source.getType(),
                    mapping.target.getFieldName(),
                    mapping.target.getFieldObjectInspector().getTypeName()));
            return false;
        } else {
            return true;
        }
    }

    private boolean isCompatible(ColumnDescriptor source, PropertyDescriptor target) {
        ParquetValueDriver driver = ParquetValueDrivers.of(
                target.getTypeInfo(),
                target.getValueClass());
        Type type = driver.getType(target.getFieldName());
        if (type.isPrimitive() == false) {
            return false;
        }
        return source.getType() == type.asPrimitiveType().getPrimitiveTypeName();
    }

    private void handleException(ExceptionHandlingStrategy strategy, String message) {
        switch (strategy) {
        case IGNORE:
            LOG.debug(message);
            break;
        case LOGGING:
            LOG.warn(message);
            break;
        case FAIL:
            throw new IllegalArgumentException(message);
        default:
            throw new AssertionError(strategy);
        }
    }

    private static final class Mapping {

        final ColumnDescriptor source;

        final PropertyDescriptor target;

        Mapping(ColumnDescriptor source, PropertyDescriptor target) {
            this.source = source;
            this.target = target;
        }
    }
}
