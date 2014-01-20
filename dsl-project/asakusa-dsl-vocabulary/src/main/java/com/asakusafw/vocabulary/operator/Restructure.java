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
package com.asakusafw.vocabulary.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;

/**
 * 再構築演算子を表す演算子注釈。
 * <p>
 * この演算子を表す演算子メソッドは作成できない。
 * 代わりに {@link CoreOperatorFactory#restructure(Source, Class)}を利用すること。
 * </p>
 * @see CoreOperatorFactory#restructure(Source, Class)
 * @since 0.2.1
 */
@Target({ })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Restructure {

    /**
     * 入力ポートの番号。
     */
    int ID_INPUT = 0;

    /**
     * 出力ポートの番号。
     */
    int ID_OUTPUT = 0;
}
