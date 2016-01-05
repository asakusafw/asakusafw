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
package com.asakusafw.dmdl.model;

/**
 * Literal kind.
 * @since 0.2.0
 */
public enum LiteralKind {

    /**
     * Represents string literals.
     */
    STRING(Messages.getString("LiteralKind.string")), //$NON-NLS-1$

    /**
     * Represents integral literals.
     */
    INTEGER(Messages.getString("LiteralKind.integer")), //$NON-NLS-1$

    /**
     * Represents decimal literals.
     */
    DECIMAL(Messages.getString("LiteralKind.decimal")), //$NON-NLS-1$

    /**
     * Represents boolean literals.
     */
    BOOLEAN(Messages.getString("LiteralKind.boolean")), //$NON-NLS-1$
    ;

    private String description;

    private LiteralKind(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
