/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.runtime.value.ValueOption;

/**
 * Edits {@link DataModelDescriptor}.
 */
public class DataModelDescriptorEditor {

    private final DataModelDescriptor origin;

    private final Set<PropertyDescriptor> removes = new HashSet<>();

    private final Map<PropertyDescriptor, ValueSerde> edits = new HashMap<>();

    /**
     * Creates a new instance.
     * @param origin the target descriptor
     */
    public DataModelDescriptorEditor(DataModelDescriptor origin) {
        this.origin = origin;
    }

    /**
     * Removes a field.
     * @param name the field name
     * @return this
     */
    public DataModelDescriptorEditor remove(String name) {
        PropertyDescriptor property = origin.findPropertyDescriptor(name);
        if (property == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Missing property: {0}",
                    name));
        }
        removes.add(property);
        return this;
    }

    /**
     * Edits a field.
     * @param name the field name
     * @param serde the replacement
     * @return this
     */
    public DataModelDescriptorEditor edit(String name, ValueSerde serde) {
        PropertyDescriptor property = origin.findPropertyDescriptor(name);
        if (property == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Missing property: {0}",
                    name));
        }
        edits.put(property, serde);
        return this;
    }

    /**
     * Removes fields.
     * @param targets the target field names
     * @return this
     */
    public DataModelDescriptorEditor removeAll(Iterable<String> targets) {
        for (String target : targets) {
            remove(target);
        }
        return this;
    }

    /**
     * Edits fields.
     * @param editMap the pairs of field name and the its replacement
     * @return this
     */
    public DataModelDescriptorEditor editAll(Map<String, ? extends ValueSerde> editMap) {
        for (Map.Entry<String, ? extends ValueSerde> entry : editMap.entrySet()) {
            edit(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Builds an edited result.
     * @return the result
     */
    public DataModelDescriptor build() {
        List<PropertyDescriptor> properties = new ArrayList<>();
        for (PropertyDescriptor property : origin.getPropertyDescriptors()) {
            if (removes.contains(property)) {
                continue;
            }
            ValueSerde modified = edits.get(property);
            if (modified == null) {
                properties.add(new PropertyDescriptor(property.getFieldName(), property, property.getFieldComment()) {
                    @Override
                    public ValueOption<?> extract(Object dataModel) {
                        return property.extract(dataModel);
                    }
                });
            } else {
                properties.add(new PropertyDescriptor(property.getFieldName(), modified, property.getFieldComment()) {
                    @Override
                    public ValueOption<?> extract(Object dataModel) {
                        return property.extract(dataModel);
                    }
                });
            }
        }
        return new DataModelDescriptor(
                origin.getDataModelClass(),
                origin.getDataModelComment(),
                properties);
    }
}
