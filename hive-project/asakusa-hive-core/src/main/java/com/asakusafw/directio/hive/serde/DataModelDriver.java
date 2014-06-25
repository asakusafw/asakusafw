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

import java.text.MessageFormat;
import java.util.ArrayList;
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
        this(descriptor, sourceInspector, new Configuration());
    }

    /**
     * Creates a new instance.
     * @param descriptor the target data model descriptor
     * @param sourceInspector the object inspector for the drive data
     * @param configuration the driver configuration
     */
    public DataModelDriver(
            DataModelDescriptor descriptor, StructObjectInspector sourceInspector, Configuration configuration) {
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
        List<StructField> sources = new ArrayList<StructField>();
        List<PropertyDescriptor> targets = new ArrayList<PropertyDescriptor>();
        for (Mapping mapping : mappings) {
            if (checkMapping(descriptor, mapping, configuration)) {
                assert mapping.source != null;
                assert mapping.target != null;
                sources.add(mapping.source);
                targets.add(mapping.target);
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
                    "Mapping columns by their name: model={0}",
                    target.getDataModelClass().getName()));
        }
        Set<PropertyDescriptor> rest = new LinkedHashSet<PropertyDescriptor>(target.getPropertyDescriptors());
        List<Mapping> mappings = new ArrayList<Mapping>();
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
                    "Mapping columns by their position: model={0}",
                    target.getDataModelClass().getName()));
        }
        List<? extends StructField> sources = source.getAllStructFieldRefs();
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

    private boolean checkMapping(DataModelDescriptor descriptor, Mapping mapping, Configuration configuration) {
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
                    "Target field is not found: model={0}, field={1}:{2}",
                    descriptor.getDataModelClass().getName(),
                    mapping.source.getFieldName(),
                    mapping.source.getFieldObjectInspector().getTypeName()));
            return false;
        } else if (isCompatible(mapping.source, mapping.target) == false) {
            handleException(configuration.getOnIncompatibleType(), MessageFormat.format(
                    "Field type is incompatible: model={0}, source={1}:{2}, target={3}:{4}",
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

    /**
     * Mapping strategy between source field and target field.
     * @since 0.7.0
     */
    public enum FieldMappingStrategy {

        /**
         * Mapping fields by their name.
         */
        NAME,

        /**
         * Mapping fields by their position.
         */
        POSITION,
    }

    /**
     * Exception handling strategy.
     * @since 0.7.0
     */
    public enum ExceptionHandlingStrategy {

        /**
         * Ignores on exception.
         */
        IGNORE,

        /**
         * Logging on exception.
         */
        LOGGING,

        /**
         * Raise {@link IllegalArgumentException} on exception.
         */
        FAIL,
    }

    /**
     * Configuration for {@link DataModelDriver}.
     * @since 0.7.0
     */
    public static final class Configuration {

        private FieldMappingStrategy fieldMappingStrategy = FieldMappingStrategy.NAME;

        private ExceptionHandlingStrategy onMissingSource = ExceptionHandlingStrategy.LOGGING;

        private ExceptionHandlingStrategy onMissingTarget = ExceptionHandlingStrategy.LOGGING;

        private ExceptionHandlingStrategy onIncompatibleType = ExceptionHandlingStrategy.LOGGING;

        /**
         * Returns the field mapping strategy.
         * @return the field mapping strategy
         */
        public FieldMappingStrategy getFieldMappingStrategy() {
            return fieldMappingStrategy;
        }

        /**
         * Sets the field mapping strategy.
         * @param value the strategy
         */
        public void setFieldMappingStrategy(FieldMappingStrategy value) {
            this.fieldMappingStrategy = value;
        }

        /**
         * Returns the exception handling strategy for missing source fields.
         * @return the exception handling strategy
         */
        public ExceptionHandlingStrategy getOnMissingSource() {
            return onMissingSource;
        }

        /**
         * Sets the exception handling strategy for missing source fields.
         * @param value the strategy
         */
        public void setOnMissingSource(ExceptionHandlingStrategy value) {
            this.onMissingSource = value;
        }

        /**
         * Returns the exception handling strategy for missing target fields.
         * @return the exception handling strategy
         */
        public ExceptionHandlingStrategy getOnMissingTarget() {
            return onMissingTarget;
        }

        /**
         * Sets the exception handling strategy for missing target fields.
         * @param value the strategy
         */
        public void setOnMissingTarget(ExceptionHandlingStrategy value) {
            this.onMissingTarget = value;
        }

        /**
         * Returns the exception handling strategy for incompatible field type.
         * @return the exception handling strategy
         */
        public ExceptionHandlingStrategy getOnIncompatibleType() {
            return onIncompatibleType;
        }

        /**
         * Sets the exception handling strategy for incompatible field type.
         * @param value the strategy
         */
        public void setOnIncompatibleType(ExceptionHandlingStrategy value) {
            this.onIncompatibleType = value;
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
