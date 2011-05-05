/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.file;

import org.apache.hadoop.io.Text;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;

/**
 * Mock data model definition for {@link Text} type.
 * @since 0.2.0
 */
public class MockTextDefinition implements DataModelDefinition<Text> {

    static final PropertyName VALUE = PropertyName.newInstance("value");

    @Override
    public Class<Text> getModelClass() {
        return Text.class;
    }

    @Override
    public Class<?> getType(PropertyName name) {
        if (VALUE.equals(name)) {
            return String.class;
        }
        return null;
    }

    @Override
    public Builder<Text> newReflection() {
        return new Builder<Text>(this);
    }

    @Override
    public DataModelReflection toReflection(Text object) {
        return newReflection()
            .add(VALUE, object.toString())
            .build();
    }

    @Override
    public Text toObject(DataModelReflection reflection) {
        return new Text((String) reflection.getValue(VALUE));
    }
}
