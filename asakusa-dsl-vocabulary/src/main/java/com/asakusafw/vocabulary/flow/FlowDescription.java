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
package com.asakusafw.vocabulary.flow;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * フローを記述するための基底クラス。
 */
public abstract class FlowDescription {

    private final AtomicBoolean described = new AtomicBoolean(false);

    /**
     * フロー記述メソッドを起動する。
     */
    public final void start() {
        if (described.compareAndSet(false, true) == false) {
            return;
        }
        describe();
    }

    /**
     * フロー記述メソッド。
     */
    protected abstract void describe();

    /**
     * このフローがジョブフローを表現するものである場合に{@code true}を返す。
     * @return ジョブフローを表現するものである場合に{@code true}
     */
    public boolean isJobFlow() {
        return isJobFlow(getClass());
    }

    /**
     * このフローがフロー部品を表現するものである場合に{@code true}を返す。
     * @return フロー部品を表現するものである場合に{@code true}
     */
    public boolean isFlowPart() {
        return isFlowPart(getClass());
    }

    /**
     * 指定のフロー記述がジョブフローを表現するものである場合に{@code true}を返す。
     * @param aClass 対象のフロー記述クラス
     * @return ジョブフローを表現するものである場合に{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static boolean isJobFlow(Class<? extends FlowDescription> aClass) {
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        return aClass.isAnnotationPresent(JobFlow.class);
    }

    /**
     * 指定のフロー記述がジョブフローを表現するものである場合に{@code true}を返す。
     * @param aClass 対象のフロー記述クラス
     * @return ジョブフローを表現するものである場合に{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getJobFlowName(Class<? extends FlowDescription> aClass) {
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        JobFlow jobflow = aClass.getAnnotation(JobFlow.class);
        if (jobflow == null) {
            return null;
        }
        return jobflow.name();
    }

    /**
     * 対象のフロー記述がフロー部品を表現するものである場合に{@code true}を返す。
     * @param aClass 対象のフロー記述クラス
     * @return フロー部品を表現するものである場合に{@code true}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public boolean isFlowPart(Class<? extends FlowDescription> aClass) {
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        return aClass.isAnnotationPresent(FlowPart.class);
    }
}
