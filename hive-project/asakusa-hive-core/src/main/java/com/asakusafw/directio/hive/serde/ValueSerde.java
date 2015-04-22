/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import com.asakusafw.runtime.value.ValueOption;

/**
 * The ser/de constants for {@link ValueOption}.
 * @since 0.7.0
 */
public interface ValueSerde {

    /**
     * Returns the type info.
     * @return the type info
     */
    TypeInfo getTypeInfo();

    /**
     * Returns the {@link ValueOption} class.
     * @return the {@link ValueOption} class
     */
    Class<? extends ValueOption<?>> getValueClass();

    /**
     * Returns an {@link ObjectInspector}.
     * @return the {@link ObjectInspector}
     */
    ObjectInspector getInspector();

    /**
     * Returns an {@link ValueDriver}.
     * @param target the target {@link ObjectInspector}
     * @return the {@link ValueDriver} for the target {@link ObjectInspector}
     */
    ValueDriver getDriver(ObjectInspector target);
}
