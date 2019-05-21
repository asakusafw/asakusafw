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
package com.asakusafw.directio.hive.serde;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;

import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Drives a value into an Asakusa data model.
 * @since 0.7.0
 */
public class DataModelDriver {

    static final Log LOG = LogFactory.getLog(DataModelDriver.class);

    private final StructObjectInspector sourceInspector;

    private final StructField[] sourceFields;

    private final PropertyDescriptor[] targetProperties;

    private final ValueDriver[] propertyDrivers;

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param sourceInspector the object inspector for the drive data
     */
    public DataModelDriver(DataModelDescriptor descriptor, StructObjectInspector sourceInspector) {
        this(descriptor, sourceInspector, new DataModelMapping());
    }

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param sourceInspector the object inspector for the drive data
     * @param configuration the driver configuration
     */
    public DataModelDriver(
            DataModelDescriptor descriptor, StructObjectInspector sourceInspector, DataModelMapping configuration) {
        this.sourceInspector = sourceInspector;
        List<Mapping> mappings;
        switch (configuration.getFieldMappingStrategy()) {
        case NAME:
            mappings = computeMappingByName(descriptor, sourceInspector);
            break;
        case POSITION:
            mappings = computeMappingByPosition(descriptor, sourceInspector);
            break;
        default:
            throw new AssertionError(configuration.getFieldMappingStrategy());
        }
        List<StructField> sources = new ArrayList<>();
        List<PropertyDescriptor> targets = new ArrayList<>();
        for (Mapping mapping : mappings) {
            if (checkMapping(descriptor, mapping, configuration)) {
                assert mapping.source != null;
                assert mapping.target != null;
                sources.add(mapping.source);
                targets.add(mapping.target);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Map column: {0}:{1} -> {2}:{3}", //$NON-NLS-1$
                            mapping.source.getFieldName(),
                            mapping.source.getFieldObjectInspector().getTypeName(),
                            mapping.target.getFieldName(),
                            mapping.target.getTypeInfo()));
                }
            }
        }
        assert sources.size() == targets.size();
        this.sourceFields = sources.toArray(new StructField[sources.size()]);
        this.targetProperties = targets.toArray(new PropertyDescriptor[targets.size()]);
        this.propertyDrivers = new ValueDriver[sourceFields.length];
        for (int i = 0; i < sourceFields.length; i++) {
            propertyDrivers[i] = targetProperties[i].getDriver(sourceFields[i].getFieldObjectInspector());
        }
    }

    private static List<Mapping> computeMappingByName(
            DataModelDescriptor target, StructObjectInspector source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Mapping columns by their name: model={0}", //$NON-NLS-1$
                    target.getDataModelClass().getName()));
        }
        Set<PropertyDescriptor> rest = new LinkedHashSet<>(target.getPropertyDescriptors());
        List<Mapping> mappings = new ArrayList<>();
        for (StructField s : source.getAllStructFieldRefs()) {
            String name = s.getFieldName();
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
            DataModelDescriptor target, StructObjectInspector source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Mapping columns by their position: model={0}", //$NON-NLS-1$
                    target.getDataModelClass().getName()));
        }
        List<? extends StructField> sources = source.getAllStructFieldRefs();
        List<? extends PropertyDescriptor> targets = target.getPropertyDescriptors();
        List<Mapping> mappings = new ArrayList<>();
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

    private boolean checkMapping(DataModelDescriptor descriptor, Mapping mapping, DataModelMapping configuration) {
        assert mapping.source != null || mapping.target != null;
        if (mapping.source == null) {
            handleException(configuration.getOnMissingSource(), MessageFormat.format(
                    Messages.getString("DataModelDriver.errorMissingSource"), //$NON-NLS-1$
                    descriptor.getDataModelClass().getName(),
                    mapping.target.getFieldName(),
                    mapping.target.getFieldObjectInspector().getTypeName()));
            return false;
        } else if (mapping.target == null) {
            handleException(configuration.getOnMissingTarget(), MessageFormat.format(
                    Messages.getString("DataModelDriver.errorMissingTarget"), //$NON-NLS-1$
                    descriptor.getDataModelClass().getName(),
                    mapping.source.getFieldName(),
                    mapping.source.getFieldObjectInspector().getTypeName()));
            return false;
        } else if (isCompatible(mapping.source, mapping.target) == false) {
            handleException(configuration.getOnIncompatibleType(), MessageFormat.format(
                    Messages.getString("DataModelDriver.errorIncompatibleType"), //$NON-NLS-1$
                    descriptor.getDataModelClass().getName(),
                    mapping.source.getFieldName(),
                    mapping.source.getFieldObjectInspector().getTypeName(),
                    mapping.target.getFieldName(),
                    mapping.target.getFieldObjectInspector().getTypeName()));
            return false;
        } else {
            return true;
        }
    }

    private boolean isCompatible(StructField source, StructField target) {
        PrimitiveCategory sourceCategory = getCategory(source.getFieldObjectInspector());
        if (sourceCategory == null) {
            return false;
        }
        PrimitiveCategory targetCategory = getCategory(target.getFieldObjectInspector());
        return sourceCategory == targetCategory;
    }

    private PrimitiveCategory getCategory(ObjectInspector inspector) {
        if (inspector instanceof PrimitiveObjectInspector) {
            return ((PrimitiveObjectInspector) inspector).getPrimitiveCategory();
        }
        return null;
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

    /**
     * Returns the source object inspector.
     * @return the source object inspector
     */
    public StructObjectInspector getSourceInspector() {
        return sourceInspector;
    }

    /**
     * Returns source field references which will be actually mapped into the data model.
     * @return source field references
     */
    public List<StructField> getSourceFields() {
        List<StructField> results = new ArrayList<>();
        Collections.addAll(results, this.sourceFields);
        return results;
    }

    /**
     * Sets data model properties from the source object.
     * @param dataModel the data model object
     * @param source the source object
     */
    public void set(Object dataModel, Object source) {
        StructObjectInspector inspector = this.sourceInspector;
        StructField[] sources = this.sourceFields;
        PropertyDescriptor[] targets = this.targetProperties;
        ValueDriver[] drivers = propertyDrivers;
        for (int i = 0; i < sources.length; i++) {
            Object value = inspector.getStructFieldData(source, sources[i]);
            ValueOption<?> option = targets[i].extract(dataModel);
            drivers[i].set(option, value);
        }
    }

    private static final class Mapping {

        final StructField source;

        final PropertyDescriptor target;

        Mapping(StructField source, PropertyDescriptor target) {
            this.source = source;
            this.target = target;
        }
    }
}
