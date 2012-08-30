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
package com.asakusafw.vocabulary.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 複製演算子を表す演算子注釈。
 * <p>
 * この演算子を表す演算子メソッドは作成できない。
 * 他の演算子からの出力や、フローへの入力を複数回利用することで、
 * 同等の機能を実現可能である。
 * </p>
 */
@Target({ })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Duplicate {
    // no members
}
