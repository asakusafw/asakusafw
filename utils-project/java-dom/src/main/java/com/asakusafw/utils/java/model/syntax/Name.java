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
package com.asakusafw.utils.java.model.syntax;

import java.util.List;


/**
 * 名前を表現する基底インターフェース。
 * <p> フィールド参照式が名前のみによって表現可能な場合、名前式を利用しなければならない。 </p>
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:6.2] Names and Identifiers} </li>
 *     <li> {@code [JLS3:6.5.6] Meaning of Expression Names} </li>
 *   </ul> </li>
 * </ul>
 * @see FieldAccessExpression
 */
public interface Name
        extends Expression, DocElement {

    /**
     * この名前の末尾の単純名を返す。
     * <p>
     * この名前が単純名である場合は、名前そのものが返される。
     * </p>
     * @return この名前の末尾の単純名
     */
    SimpleName getLastSegment();

    /**
     * この名前を単純名のリストに変換して返す。
     * <p>
     * 返されるリストは、表記と同様の順序に整列される。
     * </p>
     * @return 変換後の名前
     */
    List<SimpleName> toNameList();

    /**
     * この名前に対する正規化された文字列を返す。
     * @return 名前に対する正規化された文字列
     */
    String toNameString();
}
