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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Represents simple names in {@link IrDocComment}.
 */
public final class IrDocSimpleName extends IrDocName {

    private static final long serialVersionUID = 1L;

    private String identifier;

    /**
     * Creates a new instance.
     * @param identifier the identifier
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws IllegalArgumentException if the string is not suitable for Java identifiers
     */
    public IrDocSimpleName(String identifier) {
        setIdentifier0(identifier);
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.SIMPLE_NAME;
    }

    /**
     * Returns the identifier.
     * @return the identifier
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Sets the identifier.
     * @param identifier the identifier
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws IllegalArgumentException if the string is not suitable for Java identifiers
     */
    public void setIdentifier(String identifier) {
        setIdentifier0(identifier);
    }

    private void setIdentifier0(String id) {
        if (id == null) {
            throw new IllegalArgumentException("identifier"); //$NON-NLS-1$
        }
        if (id.length() == 0) {
            throw new IllegalArgumentException("identifier.length() == 0"); //$NON-NLS-1$
        }
        if (!Character.isJavaIdentifierStart(id.charAt(0))) {
            throw new IllegalArgumentException(MessageFormat.format(
                "identifier[0] = '''{0}'''", //$NON-NLS-1$
                id.charAt(0)));
        }
        for (int i = 1, n = id.length(); i < n; i++) {
            if (!Character.isJavaIdentifierPart(id.charAt(i))) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "identifier[{0}] = '''{1}'''", //$NON-NLS-1$
                    i,
                    id.charAt(0)));
            }
        }
        this.identifier = id;
    }

    @Override
    public String asString() {
        return getIdentifier();
    }

    @Override
    public List<IrDocSimpleName> asSimpleNameList() {
        return Collections.singletonList(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + identifier.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IrDocSimpleName other = (IrDocSimpleName) obj;
        if (!identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitSimpleName(this, context);
    }
}
