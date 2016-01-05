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
package com.asakusafw.dmdl.thundergate.driver;

import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Trait for holding cache support.
 * @since 0.2.3
 */
public class CacheSupportTrait implements Trait<CacheSupportTrait> {

    private final AstNode originalAst;

    private final PropertySymbol sid;

    private final PropertySymbol timestamp;

    private final PropertySymbol deleteFlag;

    private final AstLiteral deleteFlagValue;

    /**
     * Creates a new instance.
     * @param originalAst the original AST (optional)
     * @param sid system ID proeprty
     * @param timestamp last modified timestamp property
     * @param deleteFlag logical delete flag property (optional)
     * @param deleteFlagValue logical delete flag value (optional)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CacheSupportTrait(
            AstNode originalAst,
            PropertySymbol sid,
            PropertySymbol timestamp,
            PropertySymbol deleteFlag,
            AstLiteral deleteFlagValue) {
        if (sid == null) {
            throw new IllegalArgumentException("sid must not be null"); //$NON-NLS-1$
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.sid = sid;
        this.timestamp = timestamp;
        this.deleteFlag = deleteFlag;
        this.deleteFlagValue = deleteFlagValue;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the system ID property.
     * @return the system ID property
     */
    public PropertySymbol getSid() {
        return sid;
    }

    /**
     * Return the last modified timestamp property.
     * @return the last modified timestamp property
     */
    public PropertySymbol getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the logical delete flag property.
     * @return the delete flag property, or {@code null} if not defined
     */
    public PropertySymbol getDeleteFlag() {
        return deleteFlag;
    }

    /**
     * Returns the logical delete flag value.
     * @return the delete flag value, or {@code null} if not defined
     */
    public AstLiteral getDeleteFlagValue() {
        return deleteFlagValue;
    }
}
