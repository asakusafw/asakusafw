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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 省略に関する最適化を抑止すべきメソッドに付与する注釈。
 * <p>
 * 最終的に出力にデータが辿りつかない演算子は、最適化の過程で省略されることがある。
 * しかし、演算子が出力とは別に何らかの結果を副作用として残す場合、
 * 省略によって副作用の内容が予期せずに変更される場合がある。
 * この注釈を付与することで、そのようなデータフローでの最適化を抑制することができる。
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sticky {

    // no members
}
