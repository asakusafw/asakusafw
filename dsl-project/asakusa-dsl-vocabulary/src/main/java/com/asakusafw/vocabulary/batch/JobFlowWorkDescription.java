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
package com.asakusafw.vocabulary.batch;

import java.text.MessageFormat;

import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.JobFlow;

/**
 * ジョブフローを実行する記述。
 */
public class JobFlowWorkDescription extends WorkDescription {

    private String name;

    private Class<? extends FlowDescription> flowClass;

    /**
     * インスタンスを生成する。
     * @param flowClass ジョブフローの内容を記述したクラス
     * @throws IllegalArgumentException 引数がジョブフローの内容を記述したクラスでない場合
     */
    public JobFlowWorkDescription(Class<? extends FlowDescription> flowClass) {
        if (flowClass == null) {
            throw new IllegalArgumentException("flowClass must not be null"); //$NON-NLS-1$
        }
        if (FlowDescription.isJobFlow(flowClass) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0}はジョブフローではありません (@{1}をクラスに付与して下さい)",
                    flowClass.getName(),
                    JobFlow.class.getSimpleName()));
        }
        this.name = FlowDescription.getJobFlowName(flowClass);
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0}はジョブフローの名前として正しくありません ({0})",
                    name,
                    flowClass.getName()));
        }
        this.flowClass = flowClass;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * ジョブフローの内容を記述したクラスを返す。
     * @return ジョブフローの内容を記述したクラス
     */
    public Class<? extends FlowDescription> getFlowClass() {
        return flowClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + flowClass.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobFlowWorkDescription other = (JobFlowWorkDescription) obj;
        if (flowClass.equals(other.flowClass) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "JobFlow({0})", //$NON-NLS-1$
                getFlowClass().getName());
    }
}
