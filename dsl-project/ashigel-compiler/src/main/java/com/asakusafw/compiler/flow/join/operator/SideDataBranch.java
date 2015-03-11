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
package com.asakusafw.compiler.flow.join.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.operator.MasterBranch;

/**
 * {@link MasterBranch}をサイドデータを利用して行う。
 * <p>
 * この演算子は演算子メソッドに直接指定しない。
 * </p>
 * @see MasterBranch
 */
@Target({ })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SideDataBranch {

    /**
     * トランザクションの入力ポート番号。
     */
    int ID_INPUT_TRANSACTION = 0;

    /**
     * マスタ表のリソース番号。
     */
    int ID_RESOURCE_MASTER = 0;
}
