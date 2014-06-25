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

import java.util.ArrayList;
import java.util.List;

/**
 * A builder for {@link DataModelDescriptor}.
 * @since 0.7.0
 */
public class DataModelDescriptorBuilder {

    private final Class<?> dataModelClass;

    private String comment;

    private final List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();

    /**
     * Creates a new instance.
     * @param dataModelClass the target data model class
     */
    public DataModelDescriptorBuilder(Class<?> dataModelClass) {
        this.dataModelClass = dataModelClass;
    }

    /**
     * Creates a new instance.
     * @param dataModelClass the target data model class
     * @return the created instance
     */
    public static DataModelDescriptorBuilder of(Class<?> dataModelClass) {
        return new DataModelDescriptorBuilder(dataModelClass);
    }

    /**
     * Sets comment for the target data model.
     * @param string the comment string
     * @return this
     */
    public DataModelDescriptorBuilder comment(String string) {
        this.comment = string;
        return this;
    }

    /**
     * Adds a property descriptor for the target data model.
     * @param descriptor the property descriptor
     * @return this
     */
    public DataModelDescriptorBuilder property(PropertyDescriptor descriptor) {
        this.properties.add(descriptor);
        return this;
    }

    /**
     * Builds a {@link DataModelDescriptor}.
     * @return the built descriptor
     */
    public DataModelDescriptor build() {
        return new DataModelDescriptor(dataModelClass, comment, properties);
    }
}
