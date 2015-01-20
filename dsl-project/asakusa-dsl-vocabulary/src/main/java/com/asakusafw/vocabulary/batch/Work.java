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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * バッチから実行される処理 (Unit-of-Work) を表す。
 */
public final class Work {

    private BatchDescription declaring;

    private WorkDescription description;

    private List<Work> dependencies;

    /**
     * インスタンスを生成する。
     * @param declaring この処理を宣言したバッチクラス
     * @param description 処理対象のジョブフロークラス
     * @param dependencies この処理の前提となる依存処理の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Work(
            BatchDescription declaring,
            WorkDescription description,
            List<Work> dependencies) {
        if (declaring == null) {
            throw new IllegalArgumentException("declaring must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (dependencies == null) {
            throw new IllegalArgumentException("dependencies must not be null"); //$NON-NLS-1$
        }
        this.declaring = declaring;
        this.description = description;
        this.dependencies = Collections.unmodifiableList(
                new ArrayList<Work>(dependencies));
    }

    /**
     * この処理を宣言したバッチの情報を返す。
     * @return この処理を宣言したバッチの情報
     */
    public BatchDescription getDeclaring() {
        return declaring;
    }

    /**
     * 処理内容記述を返す。
     * @return 処理内容記述
     */
    public WorkDescription getDescription() {
        return description;
    }

    /**
     * 処理の前提となる依存処理の一覧を返す。
     * @return 依存処理の一覧
     */
    public List<Work> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}'{'description={1}, declaring={2}, dependencies={3}'}'",
                getClass().getSimpleName(),
                description,
                declaring.getClass().getName(),
                dependencies);
    }
}
