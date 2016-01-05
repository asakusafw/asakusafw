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

import java.util.Arrays;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;
import com.asakusafw.dmdl.windgate.csv.driver.CsvFieldTrait.Kind;

/**
 * Processes <code>&#64;windgate.csv.field</code> attributes.
<h2>'&#64;windgate.csv.field' attribute</h2>
The attributed declaration must be:
<ul>
<li> with name=[string-literal] (optional, default: property name)</li>
</ul>
 * @since 0.2.4
 */
public class CsvFieldDriver  extends PropertyAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "windgate.csv.field"; //$NON-NLS-1$

    /**
     * The element name.
     */
    public static final String ELEMENT_NAME = "name"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        String value = AttributeUtil.takeString(environment, attribute, elements, ELEMENT_NAME, false);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        checkFieldType(environment, declaration, attribute, BasicTypeKind.values());
        if (CsvFieldDriver.checkConflict(environment, declaration, attribute)) {
            declaration.putTrait(
                    CsvFieldTrait.class,
                    new CsvFieldTrait(attribute, Kind.VALUE, value));
        }
    }

    static boolean checkConflict(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        assert environment != null;
        assert declaration != null;
        assert attribute != null;
        if (declaration.getTrait(CsvFieldTrait.class) == null) {
            return true;
        }
        environment.report(new Diagnostic(
                Level.ERROR,
                attribute,
                Messages.getString("CsvFieldDriver.diagnosticDuplicateAttribute"), //$NON-NLS-1$
                declaration.getOwner().getName().identifier,
                declaration.getName().identifier));
        return false;
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
                Messages.getString("CsvFieldDriver.diagnosticInvalidTypeElement"), //$NON-NLS-1$
                declaration.getOwner().getName().identifier,
                declaration.getName().identifier,
                attribute.name.toString(),
                Arrays.asList(types)));
    }
}
