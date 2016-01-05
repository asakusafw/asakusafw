/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.LabeledStatement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link LabeledStatement}.
 */
public final class LabeledStatementImpl extends ModelRoot implements LabeledStatement {

    private SimpleName label;

    private Statement body;

    @Override
    public SimpleName getLabel() {
        return this.label;
    }

    /**
     * Sets the label name.
     * @param label the documentation blocks
     * @throws IllegalArgumentException if {@code label} was {@code null}
     */
    public void setLabel(SimpleName label) {
        Util.notNull(label, "label"); //$NON-NLS-1$
        this.label = label;
    }

    @Override
    public Statement getBody() {
        return this.body;
    }

    /**
     * Sets the body statement.
     * @param body the body statement
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    public void setBody(Statement body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#LABELED_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#LABELED_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.LABELED_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitLabeledStatement(this, context);
    }
}
