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
package com.asakusafw.utils.java.internal.model.syntax;

import com.asakusafw.utils.java.model.syntax.EmptyStatement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link EmptyStatement}の実装。
 */
public final class EmptyStatementImpl extends ModelRoot implements EmptyStatement {

    /**
     * この要素の種類を表す{@link ModelKind#EMPTY_STATEMENT}を返す。
     * @return {@link ModelKind#EMPTY_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.EMPTY_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitEmptyStatement(this, context);
    }
}
