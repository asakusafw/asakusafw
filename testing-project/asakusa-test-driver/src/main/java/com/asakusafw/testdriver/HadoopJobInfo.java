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
package com.asakusafw.testdriver;

import java.util.Map;

/**
 * Hadoopジョブを実行するために必要な情報を格納するBean。
 */
class HadoopJobInfo {

    private final String batchId;
    private final String flowId;
    private final String executionId;
    private final String jarName;
    private final String className;
    private final Map<String, String> dPropMap;

    public HadoopJobInfo(
            String batchId,
            String flowId,
            String executionId, String jarName, String className, Map<String, String> dPropMap) {
        this.batchId = batchId;
        this.flowId = flowId;
        this.executionId = executionId;
        this.jarName = jarName;
        this.className = className;
        this.dPropMap = dPropMap;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getFlowId() {
        return flowId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getJarName() {
        return jarName;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getDPropMap() {
        return dPropMap;
    }

}
