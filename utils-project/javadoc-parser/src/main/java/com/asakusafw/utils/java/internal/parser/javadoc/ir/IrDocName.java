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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.util.List;

/**
 * 名前を表す要素。
 */
public abstract class IrDocName extends AbstractIrDocElement implements IrDocFragment {

    /**
     * serialVersionUID を表す。
     */
    private static final long serialVersionUID = -511710630107018908L;

    /**
     * この名前全体を表す文字列を返す。
     * @return この名前全体を表す文字列
     */
    public abstract String asString();

    /**
     * この名前を構成する単純名の一覧を返す。
     * @return この名前を構成する単純名の一覧
     */
    public abstract List<IrDocSimpleName> asSimpleNameList();
}
