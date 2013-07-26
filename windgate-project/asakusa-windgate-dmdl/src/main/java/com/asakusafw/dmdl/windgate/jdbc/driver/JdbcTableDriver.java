/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.dmdl.windgate.jdbc.driver;

import java.util.Map;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;windgate.jdbc.table</code> attributes.
<h2>'&#64;windgate.jdbc.column' attribute</h2>
The attributed declaration must be:
<ul>
<li> with name=[string-literal] </li>
</ul>
 * @since 0.2.4
 */
public class JdbcTableDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "windgate.jdbc.table"; //$NON-NLS-1$

    /**
     * The element name of {@code name}.
     */
    public static final String ELEMENT_NAME = "name"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(
            DmdlSemantics environment,
            ModelDeclaration declaration,
            AstAttribute attribute) {
        Map<String, AstAttributeElement> elements = AttributeUtil.getElementMap(attribute);
        String name = AttributeUtil.takeString(environment, attribute, elements, ELEMENT_NAME, true);
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
        if (name != null) {
            declaration.putTrait(
                    JdbcTableTrait.class,
                    new JdbcTableTrait(attribute, name));
        }
    }
}
