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
package com.asakusafw.dmdl.directio.hive.common;

import java.math.BigInteger;
import java.util.Map;

import org.apache.hadoop.hive.common.type.HiveVarchar;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.hive.varchar</code> attributes.
<h2>'&#64;directio.hive.varchar' attribute</h2>
The attributed declaration must be:
<ul>
<li> with length=[integer-literal]</li>
</ul>
 * @since 0.7.0
 */
public class HiveVarcharDriver extends PropertyAttributeDriver {

    // TODO refactor with HiveCharDriver

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.hive.varchar"; //$NON-NLS-1$

    /**
     * The element name.
     */
    public static final String ELEMENT_NAME = "length"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        if (Util.checkProperty(environment, declaration, attribute, HiveFieldTrait.TypeKind.VARCHAR) == false) {
            return;
        }
        HiveFieldTrait trait = HiveFieldTrait.get(declaration);
        trait.setOriginalAst(attribute, false);
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        analyzeElements(environment, attribute, elements, trait);
    }

    private void analyzeElements(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements,
            HiveFieldTrait trait) {
        AstLiteral length = AttributeUtil.takeLiteral(
                environment, attribute, elements, ELEMENT_NAME, LiteralKind.INTEGER, true);
        if (length != null) {
            BigInteger value = length.toIntegerValue();
            if (AttributeUtil.checkRange(
                    environment, length, label(ELEMENT_NAME),
                    value, 1L, (long) HiveVarchar.MAX_VARCHAR_LENGTH)) {
                trait.setVarcharTypeInfo(value.intValue());
            }
        }
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
    }

    private static String label(String key) {
        return String.format("@%s(%s)", TARGET_NAME, key); //$NON-NLS-1$
    }
}
