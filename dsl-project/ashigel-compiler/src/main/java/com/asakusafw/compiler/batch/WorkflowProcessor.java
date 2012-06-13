/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.asakusafw.vocabulary.batch.WorkDescription;


/**
 * {@link Workflow}を解釈するエンジン。
 */
public interface WorkflowProcessor extends BatchCompilingEnvironment.Initializable {

    /**
     * このエンジンが利用する{@link WorkDescriptionProcessor}の一覧を返す。
     * @return 利用する{@link WorkDescriptionProcessor}の一覧
     */
    Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors();

    /**
     * このエンジンを利用して{@link Workflow}の処理を返す。
     * @param workflow 対象のワークフロー
     * @throws IOException 処理に失敗した場合
     */
    void process(Workflow workflow) throws IOException;

    /**
     * {@link WorkflowProcessor}を検出するリポジトリ。
     */
    interface Repository extends BatchCompilingEnvironment.Initializable {

        /**
         * 指定の{@link WorkDescription}に対して
         * 利用可能な{@link WorkflowProcessor}の一覧を返す。
         * @param descriptions 対象の{@link WorkDescription}一覧
         * @return 利用可能な{@link WorkflowProcessor}の一覧
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Set<WorkflowProcessor> findWorkflowProcessors(
                Set<? extends WorkDescription> descriptions);

        /**
         * 指定の{@link WorkDescription}に対して、それ自身を処理する
         * {@link WorkDescriptionProcessor}を返す。
         * @param workDescription 対象の{@link WorkDescription}
         * @return 対応する{@link WorkDescriptionProcessor}、存在しない場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        WorkDescriptionProcessor<?> findDescriptionProcessor(
                WorkDescription workDescription);
    }
}
