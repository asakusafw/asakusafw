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
package com.asakusafw.testdriver;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestDataToolProvider;

/**
 * An abstract super class of test driver inputs.
 * @since 0.2.0
 * @version 0.6.0
 * @param <T> the data model type
 */
public abstract class DriverInputBase<T> extends DriverElementBase {

    private static final Logger LOG = LoggerFactory.getLogger(DriverInputBase.class);

    private final Class<?> callerClass;

    private final TestDataToolProvider testTools;

    private final String name;

    private final Class<T> modelType;

    private DataModelSourceFactory source;

    /**
     * Creates a new instance.
     * @param callerClass the current context class
     * @param testTools the test data tools
     * @param name the original input name
     * @param modelType the data model type
     * @since 0.6.0
     */
    protected DriverInputBase(Class<?> callerClass, TestDataToolProvider testTools, String name, Class<T> modelType) {
        if (callerClass == null) {
            throw new IllegalArgumentException("callerClass must not be null"); //$NON-NLS-1$
        }
        if (testTools == null) {
            throw new IllegalArgumentException("testTools must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (modelType == null) {
            throw new IllegalArgumentException("modelType must not be null"); //$NON-NLS-1$
        }
        this.callerClass = callerClass;
        this.testTools = testTools;
        this.name = name;
        this.modelType = modelType;
    }

    @Override
    protected final Class<?> getCallerClass() {
        return callerClass;
    }

    @Override
    protected final TestDataToolProvider getTestTools() {
        return testTools;
    }

    /**
     * Returns the name of this port.
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the data type of this port.
     * @return the data type
     */
    public final Class<T> getModelType() {
        return modelType;
    }

    /**
     * Returns the data source for this input.
     * @return the source, or {@code null} if not defined
     * @since 0.2.3
     */
    public DataModelSourceFactory getSource() {
        return source;
    }

    /**
     * Sets the data source for this input.
     * @param source the source, or {@code null} to reset it
     * @since 0.6.0
     */
    protected final void setSource(DataModelSourceFactory source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Prepare: name={}, model={}, source={}", new Object[] { //$NON-NLS-1$
                    getName(),
                    getModelType().getName(),
                    source,
            });
        }
        this.source = source;
    }

    /**
     * Converts an data model object collection into {@link DataModelSourceFactory} which provides data models.
     * This implementation immediately converts data model objects into equivalent {@link DataModelReflection}s.
     * @param sourceObjects the original data model objects
     * @return the {@link DataModelSourceFactory}
     * @since 0.6.0
     */
    protected final DataModelSourceFactory toDataModelSourceFactory(Iterable<? extends T> sourceObjects) {
        if (sourceObjects == null) {
            throw new IllegalArgumentException("sourceObjects must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<T> definition = getDataModelDefinition();
        return toDataModelSourceFactory(definition, sourceObjects);
    }

    /**
     * Returns the data model definition for this port.
     * @return the data model definition
     */
    public final DataModelDefinition<T> getDataModelDefinition() {
        try {
            TestDataToolProvider tools = getTestTools();
            return tools.toDataModelDefinition(modelType);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("DriverInputBase.errorInvalidDataModel"), //$NON-NLS-1$
                    name,
                    modelType.getName()), e);
        }
    }
}