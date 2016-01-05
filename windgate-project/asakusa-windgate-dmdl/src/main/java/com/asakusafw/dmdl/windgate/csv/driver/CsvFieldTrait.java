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
package com.asakusafw.dmdl.windgate.csv.driver;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Attributes for CSV fields.
 * @since 0.2.4
 */
public class CsvFieldTrait implements Trait<CsvFieldTrait> {

    private final AstNode originalAst;

    private final Kind kind;

    private final String name;

    /**
     * Creates a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param kind the field kind
     * @param name the explicit field name (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvFieldTrait(AstNode originalAst, Kind kind, String name) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.kind = kind;
        this.name = name;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the CSV field kind of the property.
     * If the field kind is not declared explicitly in the property, this returns the default kind.
     * @param property target property
     * @param defaultKind default kind
     * @return the field kind
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Kind getKind(PropertyDeclaration property, Kind defaultKind) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        CsvFieldTrait trait = property.getTrait(CsvFieldTrait.class);
        if (trait != null) {
            return trait.kind;
        }
        return defaultKind;
    }

    /**
     * Returns the CSV field name of the property.
     * If the field name is not declared explicitly in the property, this returns the property name.
     * @param property target property
     * @return the field name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String getFieldName(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        CsvFieldTrait trait = property.getTrait(CsvFieldTrait.class);
        if (trait != null && trait.name != null) {
            return trait.name;
        }
        return property.getName().identifier;
    }

    /**
     * The field kind.
     * @since 0.2.4
     */
    public enum Kind {

        /**
         * normal fields.
         */
        VALUE,

        /**
         * fields which keep file name.
         */
        FILE_NAME,

        /**
         * fields which keep line number.
         */
        LINE_NUMBER,

        /**
         * fields which keep record number.
         */
        RECORD_NUMBER,

        /**
         * ignored fields.
         */
        IGNORE,
    }
}
