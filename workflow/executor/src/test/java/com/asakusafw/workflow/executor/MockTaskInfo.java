/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.workflow.executor;

import com.asakusafw.workflow.model.TaskInfo;
import com.asakusafw.workflow.model.basic.BasicTaskInfo;

/**
 * Mock implementation of {@link TaskInfo}.
 */
public class MockTaskInfo extends BasicTaskInfo {

    private final String moduleName;

    private final String value;

    /**
     * Creates a new instance.
     * @param value the value
     */
    public MockTaskInfo(String value) {
        this("mock", value);
    }

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param value the value
     */
    public MockTaskInfo(String moduleName, String value) {
        this.moduleName = moduleName;
        this.value = value;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
