/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import com.asakusafw.vocabulary.batch.WorkDescription;


/**
 * 処理記述を処理するプロセッサ。
 * @param <T> 処理対象の{@link WorkDescription}クラス
 */
public interface WorkDescriptionProcessor<T extends WorkDescription>
        extends BatchCompilingEnvironment.Initializable {

    /**
     * このプロセッサが対象とするクラス(またはその基底クラス)を返す。
     * @return このプロセッサが対象とするクラス
     */
    Class<T> getTargetType();

    /**
     * このプロセッサを利用して記述を処理する。
     * @param description 対象の処理記述
     * @return 処理結果を表すデータ
     * @throws IOException 処理に失敗した場合
     */
    Object process(T description) throws IOException;
}
