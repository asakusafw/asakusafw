/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.hive.ignore</code> attributes.
<h2>'&#64;directio.hive.ignore' attribute</h2>
The attributed declaration must be without any properties.
 * @since 0.7.0
 */
public class HiveIgnoreDriver extends PropertyAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.hive.ignore"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(
            DmdlSemantics environment,
            PropertyDeclaration declaration,
            AstAttribute attribute) {
        HiveFieldTrait trait = HiveFieldTrait.get(declaration);
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        analyzeElements(environment, attribute, elements, trait);
    }

    private void analyzeElements(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements,
            HiveFieldTrait trait) {
        trait.setColumnPresent(false);
        trait.setOriginalAst(attribute, false);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
    }
}
