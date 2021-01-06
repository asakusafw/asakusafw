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
package com.asakusafw.info.operator;

import com.asakusafw.info.value.ClassInfo;

/**
 * Details of operator inputs.
 * @since 0.9.2
 */
public interface InputInfo {

    /**
     * Returns the port name.
     * @return the port name
     */
    String getName();

    /**
     * Returns the data type.
     * @return the data type
     */
    ClassInfo getDataType();

    /**
     * Returns the input granularity.
     * @return the input granularity, or {@code null} if is not defined
     */
    InputGranularity getGranulatity();

    /**
     * Returns the input group info.
     * @return the input group, or {@code null} if it is not defined
     */
    InputGroup getGroup();
}