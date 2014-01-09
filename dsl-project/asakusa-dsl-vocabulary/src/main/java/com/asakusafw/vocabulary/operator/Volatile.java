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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多重化に関する最適化を抑止すべきメソッドに付与する注釈。
 * <p>
 * 同じ入力に対して異なる結果を返すような演算子は、
 * 予期せずに複数回実行すると予期せぬ振る舞いを行うことがある。
 * この注釈を付与することで、そのようなデータフローでの最適化を抑制することができる。
 * </p>
 * <p>
 * 具体的には、以下のような特性を持つ演算子メソッドには、この注釈が必要である。
 * </p>
 * <ul>
 * <li> ユニークな値を採番する </li>
 * <li> 実行時の時刻などを利用する </li>
 * <li> ランダムな値を利用する </li>
 * <li> 入力の個数を計測する </li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Volatile {

    // no members
}
