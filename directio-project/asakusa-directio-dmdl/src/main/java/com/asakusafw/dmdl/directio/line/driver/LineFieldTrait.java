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
package com.asakusafw.dmdl.directio.line.driver;

import java.util.Arrays;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Trait;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;

/**
 * Attributes for line based text fields.
 * @since 0.7.5
 */
public class LineFieldTrait implements Trait<LineFieldTrait> {

    private final AstNode originalAst;

    private final Kind kind;

    /**
     * Creates a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param kind the field kind
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public LineFieldTrait(AstNode originalAst, Kind kind) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.kind = kind;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the CSV field kind of the property.
     * If the field kind is not declared explicitly in the property, this returns the default kind.
     * @param property target property
     * @return the field kind
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Kind getKind(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        LineFieldTrait trait = property.getTrait(LineFieldTrait.class);
        if (trait != null) {
            return trait.kind;
        }
        return Kind.IGNORE;
    }

    static void checkFieldType(
            DmdlSemantics environment,
            PropertyDeclaration declaration,
            AstAttribute attribute,
            BasicTypeKind... types) {
        assert environment != null;
        assert declaration != null;
        assert attribute != null;
        assert types != null;
        assert types.length > 0;
        Type type = declaration.getType();
        if (type instanceof BasicType) {
            BasicTypeKind kind = ((BasicType) type).getKind();
            for (BasicTypeKind accept : types) {
                if (kind == accept) {
                    return;
                }
            }
        }
        environment.report(new Diagnostic(
                Level.ERROR,
                attribute,
                Messages.getString("LineFieldTrait.errorInconsistentType"), //$NON-NLS-1$
                declaration.getOwner().getName().identifier,
                declaration.getName().identifier,
                attribute.name.toString(),
                Arrays.asList(types)));
    }

    static boolean checkConflict(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        assert environment != null;
        assert declaration != null;
        assert attribute != null;
        if (declaration.getTrait(LineFieldTrait.class) == null) {
            return true;
        }
        environment.report(new Diagnostic(
                Level.ERROR,
                attribute,
                Messages.getString("LineFieldTrait.errorConflictAttribute"), //$NON-NLS-1$
                declaration.getOwner().getName().identifier,
                declaration.getName().identifier));
        return false;
    }

    /**
     * The field kind.
     * @since 0.7.5
     */
    public enum Kind {

        /**
         * The body field.
         */
        BODY,

        /**
         * fields which keep file name.
         */
        FILE_NAME,

        /**
         * fields which keep line number.
         */
        LINE_NUMBER,

        /**
         * Ignored fields.
         */
        IGNORE,
    }
}
