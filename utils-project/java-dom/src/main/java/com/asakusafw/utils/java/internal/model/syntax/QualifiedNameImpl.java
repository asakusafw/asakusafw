/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link QualifiedName}.
 */
public final class QualifiedNameImpl extends ModelRoot implements QualifiedName {

    private Name qualifier;

    private SimpleName simpleName;

    @Override
    public Name getQualifier() {
        return this.qualifier;
    }

    /**
     * Sets the name qualifier.
     * @param qualifier the name qualifier
     * @throws IllegalArgumentException if {@code qualifier} was {@code null}
     */
    public void setQualifier(Name qualifier) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        this.qualifier = qualifier;
    }

    @Override
    public SimpleName getSimpleName() {
        return this.simpleName;
    }

    /**
     * Sets the simple name on the tail.
     * @param simpleName the simple name on the tail
     * @throws IllegalArgumentException if {@code simpleName} was {@code null}
     */
    public void setSimpleName(SimpleName simpleName) {
        Util.notNull(simpleName, "simpleName"); //$NON-NLS-1$
        this.simpleName = simpleName;
    }

    /**
     * Returns {@link ModelKind#QUALIFIED_NAME} which represents this element kind.
     * @return {@link ModelKind#QUALIFIED_NAME}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.QUALIFIED_NAME;
    }
    @Override
    public SimpleName getLastSegment() {
        return getSimpleName();
    }

    @Override
    public String toNameString() {
        Iterator<SimpleName> iter = toNameList().iterator();
        assert iter.hasNext();
        StringBuilder buf = new StringBuilder();
        buf.append(iter.next());
        while (iter.hasNext()) {
            buf.append('.');
            buf.append(iter.next());
        }
        return buf.toString();
    }

    @Override
    public List<SimpleName> toNameList() {
        LinkedList<SimpleName> result = new LinkedList<>();
        result.addFirst(getSimpleName());
        Name current = getQualifier();
        while (current.getModelKind() == ModelKind.QUALIFIED_NAME) {
            QualifiedName qname = (QualifiedName) current;
            result.addFirst(qname.getSimpleName());
            current = qname.getQualifier();
        }
        assert current.getModelKind() == ModelKind.SIMPLE_NAME;
        result.addFirst((SimpleName) current);
        return result;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitQualifiedName(this, context);
    }
}
