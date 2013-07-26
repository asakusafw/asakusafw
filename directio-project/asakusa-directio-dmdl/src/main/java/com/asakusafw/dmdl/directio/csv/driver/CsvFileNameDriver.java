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
package com.asakusafw.dmdl.directio.csv.driver;

import com.asakusafw.dmdl.directio.csv.driver.CsvFieldTrait.Kind;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.spi.PropertyAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;directio.csv.file_name</code> attributes.
<h2>'&#64;directio.csv.file_name' attribute</h2>
The attributed declaration must have no attributes.
 * @since 0.2.5
 */
public class CsvFileNameDriver  extends PropertyAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "directio.csv.file_name"; //$NON-NLS-1$

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(DmdlSemantics environment, PropertyDeclaration declaration, AstAttribute attribute) {
        environment.reportAll(AttributeUtil.reportInvalidElements(attribute, attribute.elements));
        CsvFieldDriver.checkFieldType(environment, declaration, attribute, BasicTypeKind.TEXT);
        if (CsvFieldDriver.checkConflict(environment, declaration, attribute)) {
            declaration.putTrait(
                    CsvFieldTrait.class,
                    new CsvFieldTrait(attribute, Kind.FILE_NAME, null));
        }
    }
}
