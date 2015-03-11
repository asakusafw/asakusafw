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

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SwitchDefaultLabel;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link SwitchDefaultLabel}の実装。
 */
public final class SwitchDefaultLabelImpl extends ModelRoot implements SwitchDefaultLabel {

    /**
     * この要素の種類を表す{@link ModelKind#SWITCH_DEFAULT_LABEL}を返す。
     * @return {@link ModelKind#SWITCH_DEFAULT_LABEL}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SWITCH_DEFAULT_LABEL;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSwitchDefaultLabel(this, context);
    }
}
