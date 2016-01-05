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
package com.asakusafw.dmdl.directio.hive.common;

import java.util.Set;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Utilities for this package.
 */
final class Util {

    private Util() {
        return;
    }

    static boolean checkProperty(
            DmdlSemantics environment,
            PropertyDeclaration declaration,
            AstAttribute attribute,
            HiveFieldTrait.TypeKind target) {
        Set<BasicTypeKind> kinds = target.getSupportedKinds();
        return checkProperty(environment, declaration, attribute, kinds.toArray(new BasicTypeKind[kinds.size()]));
    }

    static boolean checkProperty(
            DmdlSemantics environment,
            PropertyDeclaration declaration,
            AstAttribute attribute,
            BasicTypeKind... typeKinds) {
        assert environment != null;
        assert declaration != null;
        assert attribute != null;
        HiveFieldTrait trait = HiveFieldTrait.get(declaration);
        if (AttributeUtil.checkFieldType(environment, declaration, attribute, typeKinds) == false) {
            return false;
        }
        if (trait.getTypeKind() != HiveFieldTrait.TypeKind.NATURAL) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    attribute,
                    Messages.getString("Util.diagnosticDuplicatePropertyAttribute"), //$NON-NLS-1$
                    declaration.getOwner().getName().identifier,
                    declaration.getName().identifier));
            return false;
        }
        return true;
    }
}
