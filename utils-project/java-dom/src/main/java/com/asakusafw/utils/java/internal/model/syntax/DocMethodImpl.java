/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.DocMethod;
import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link DocMethod}.
 */
public final class DocMethodImpl extends ModelRoot implements DocMethod {

    private Type type;

    private SimpleName name;

    private List<? extends DocMethodParameter> formalParameters;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the owner type.
     * @param type the owner type, or {@code null} if it is not specified
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * Sets the target method or constructor name.
     * @param name the target method or constructor name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends DocMethodParameter> getFormalParameters() {
        return this.formalParameters;
    }

    /**
     * Sets the formal parameter declarations.
     * @param formalParameters the formal parameter declarations
     * @throws IllegalArgumentException if {@code formalParameters} was {@code null}
     */
    public void setFormalParameters(List<? extends DocMethodParameter> formalParameters) {
        Util.notNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notContainNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        this.formalParameters = Util.freeze(formalParameters);
    }

    /**
     * Returns {@link ModelKind#DOC_METHOD} which represents this element kind.
     * @return {@link ModelKind#DOC_METHOD}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_METHOD;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocMethod(this, context);
    }
}
