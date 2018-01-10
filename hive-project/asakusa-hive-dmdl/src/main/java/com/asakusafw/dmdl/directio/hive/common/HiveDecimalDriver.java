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

import java.util.Map;

import org.apache.hadoop.hive.common.type.HiveDecimal;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.hive.decimal</code> attributes.
<h2>'&#64;directio.hive.decimal' attribute</h2>
The attributed declaration must be:
<ul>
<li> with precision=[integer-literal]</li>
<li> with scale=[integer-literal]</li>
</ul>
 * @since 0.7.0
 */
public class HiveDecimalDriver extends PropertyAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.hive.decimal"; //$NON-NLS-1$

    /**
     * The precision element name.
     */
    public static final String ELEMENT_PRECISION_NAME = "precision"; //$NON-NLS-1$

    /**
     * The scale element name.
     */
    public static final String ELEMENT_SCALE_NAME = "scale"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        if (Util.checkProperty(environment, declaration, attribute, HiveFieldTrait.TypeKind.DECIMAL) == false) {
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
        AstLiteral precision = AttributeUtil.takeLiteral(
                environment, attribute, elements, ELEMENT_PRECISION_NAME, LiteralKind.INTEGER, true);
        int precisionValue = -1;
        if (precision != null) {
            if (AttributeUtil.checkRange(
                    environment, precision, label(ELEMENT_PRECISION_NAME),
                    precision.toIntegerValue(), 1L, (long) HiveDecimal.MAX_PRECISION)) {
                precisionValue = precision.toIntegerValue().intValue();
            }
        }
        AstLiteral scale = AttributeUtil.takeLiteral(
                environment, attribute, elements, ELEMENT_SCALE_NAME, LiteralKind.INTEGER, true);
        int scaleValue = -1;
        if (scale != null) {
            if (AttributeUtil.checkRange(
                    environment, scale, label(ELEMENT_SCALE_NAME),
                    scale.toIntegerValue(), 0L, (long) HiveDecimal.MAX_SCALE)) {
                scaleValue = scale.toIntegerValue().intValue();
            }
        }
        if (precisionValue >= 0 && scaleValue >= 0) {
            if (precisionValue < scaleValue) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        attribute,
                        Messages.getString("HiveDecimalDriver.diagnosticPrecisionLessThanScale"), //$NON-NLS-1$
                        TARGET_NAME,
                        ELEMENT_PRECISION_NAME, precisionValue,
                        ELEMENT_SCALE_NAME, scaleValue));
            } else {
                trait.setDecimalTypeInfo(precisionValue, scaleValue);
            }
        }
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
    }

    private static String label(String key) {
        return String.format("@%s(%s)", TARGET_NAME, key); //$NON-NLS-1$
    }
}
