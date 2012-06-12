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
package com.asakusafw.utils.java.model.syntax;


/**
 * インスタンス初期化子、およびクラス初期化子を表現するインターフェース。
 * <p> この要素が{@code static}の修飾子を伴って宣言される場合、この要素はクラス初期化子を表現する。そうでない場合、この要素はインスタンス初期化子を表現する。 </p>
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.6] Instance Initializers} </li>
 *     <li> {@code [JLS3:8.7] Static Initializers} </li>
 *   </ul> </li>
 * </ul>
 */
public interface InitializerDeclaration
        extends TypeBodyDeclaration {

    // properties

    /**
     * ドキュメンテーションコメントを返す。
     * <p> ドキュメンテーションコメントが存在しない場合は{@code null}が返される。 </p>
     * <p> インスタンス初期化子およびクラス初期化子のドキュメンテーションコメントは、通常無視される。そのため、この要素は通常の実装では{@code null}を返す。 </p>
     * @return
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     */
    @Override
    Javadoc getJavadoc();

    /**
     * 初期化子の本体を返す。
     * @return
     *     初期化子の本体
     */
    Block getBody();
}
