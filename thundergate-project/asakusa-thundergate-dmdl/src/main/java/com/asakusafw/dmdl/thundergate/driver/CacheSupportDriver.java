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

import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.spi.ModelAttributeDriver;
import com.asakusafw.dmdl.util.AttributeUtil;

/**
 * Processes <code>&#64;thundergate.cache_support</code> attributes.
<h2>'&#64;thundergate.cache_support' attribute</h2>
The attributed declaration must be:
<ul>
<li> a <em>record model</em> attribute </li>
<li> {@code sid = <system ID property (must be LONG)>} </li>
<li> {@code timestamp = <last modified timestamp property (must be DATETIME)>} </li>
<li> {@code delete_flag = <logical delete flag property (optional)>} </li>
<li> {@code delete_flag_value = <logical delete flag value (optional, must be compatible with delete_flag type)>} </li>
</ul>
 */
public class CacheSupportDriver extends ModelAttributeDriver {

    /**
     * The attribute name.
     */
    public static final String TARGET_NAME = "thundergate.cache_support";

    /**
     * The element name of system ID property.
     */
    public static final String SID_ELEMENT_NAME = "sid";

    /**
     * The element name of last modified time stamp property.
     */
    public static final String TIMESTAMP_ELEMENT_NAME = "timestamp";

    /**
     * The element name of logical delete flag property.
     */
    public static final String DELETE_FLAG_ELEMENT_NAME = "delete_flag";

    /**
     * The element name of logical delete flag value.
     */
    public static final String DELETE_FLAG_VALUE_ELEMENT_NAME = "delete_flag_value";

    @Override
    public String getTargetName() {
        return TARGET_NAME;
    }

    @Override
    public void process(
            DmdlSemantics environment,
            ModelDeclaration declaration,
            AstAttribute attribute) {
        if (declaration.getOriginalAst().kind != ModelDefinitionKind.RECORD) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    declaration.getOriginalAst(),
                    "@{0} is only for record model",
                    TARGET_NAME));
            return;
        }

        Holder holder = new Holder(environment, declaration, attribute);

        PropertySymbol sid = holder.takeProperty(SID_ELEMENT_NAME);
        PropertySymbol timestamp = holder.takeProperty(TIMESTAMP_ELEMENT_NAME);
        PropertySymbol delete = holder.takeProperty(DELETE_FLAG_ELEMENT_NAME);
        AstLiteral deleteValue = holder.takeLiteral(DELETE_FLAG_VALUE_ELEMENT_NAME);
        holder.checkEmpty();
        if (holder.sawError) {
            return;
        }

        holder.checkDefined(SID_ELEMENT_NAME, sid);
        holder.checkDefined(TIMESTAMP_ELEMENT_NAME, timestamp);
        if (delete != null || deleteValue != null) {
            holder.checkDefined(DELETE_FLAG_ELEMENT_NAME, delete);
            holder.checkDefined(DELETE_FLAG_VALUE_ELEMENT_NAME, deleteValue);
        }
        if (holder.sawError) {
            return;
        }
        if (type(sid) != BasicTypeKind.LONG) {
            holder.error(
                    sid.getName(),
                    "System ID ({0} must be a type of LONG",
                    sid.getName().identifier);
        }
        if (type(timestamp) != BasicTypeKind.DATETIME) {
            holder.error(
                    timestamp.getName(),
                    "Last modified timestamp ({0}) must be a type of DATETIME",
                    timestamp.getName().identifier);
        }
        checkDeleteFlag(holder, delete, deleteValue);
        if (holder.sawError) {
            return;
        }

        declaration.putTrait(
                CacheSupportTrait.class,
                new CacheSupportTrait(attribute, sid, timestamp, delete, deleteValue));
    }

    private void checkDeleteFlag(Holder holder, PropertySymbol delete, AstLiteral deleteValue) {
        assert holder != null;
        if (delete != null) {
            assert deleteValue != null;
            BasicTypeKind deleteType = type(delete);
            switch (deleteValue.getKind()) {
            case BOOLEAN:
                if (deleteType != BasicTypeKind.BOOLEAN) {
                    holder.error(
                            delete.getName(),
                            "Last modified timestamp ({0}) must be a type of BOOLEAN",
                            delete.getName().identifier);
                }
                break;
            case INTEGER:
                if (deleteType != BasicTypeKind.BYTE
                        && deleteType != BasicTypeKind.SHORT
                        && deleteType != BasicTypeKind.INT
                        && deleteType != BasicTypeKind.LONG) {
                    holder.error(
                            delete.getName(),
                            "Last modified timestamp ({0}) must be a type of BYTE|SHORT|INT|LONG",
                            delete.getName().identifier);
                }
                break;
            case STRING:
                if (deleteType != BasicTypeKind.TEXT) {
                    holder.error(
                            delete.getName(),
                            "Last modified timestamp ({0}) must be a type of TEXT",
                            delete.getName().identifier);
                }
                break;
            default:
                holder.error(
                        deleteValue,
                        "Last modified timestamp value ({0}) must be a one of boolean|integer|string",
                        deleteValue);
            }
        }
    }

    private BasicTypeKind type(PropertySymbol symbol) {
        assert symbol != null;
        PropertyDeclaration decl = symbol.findDeclaration();
        assert decl != null;
        Type type = decl.getType();
        if ((type instanceof BasicType) == false) {
            return null;
        }
        return ((BasicType) type).getKind();
    }

    private static class Holder {

        private final DmdlSemantics environment;

        private final ModelDeclaration model;

        private final AstAttribute attribute;

        private final Map<String, AstAttributeElement> elements;

        boolean sawError;

        public Holder(
                DmdlSemantics environment,
                ModelDeclaration model,
                AstAttribute attribute) {
            assert environment != null;
            assert model != null;
            assert attribute != null;
            this.environment = environment;
            this.model = model;
            this.attribute = attribute;
            this.elements = AttributeUtil.getElementMap(attribute);
            this.sawError = false;
        }

        public void checkEmpty() {
            if (elements.isEmpty() == false) {
                environment.reportAll(AttributeUtil.reportInvalidElements(attribute, elements.values()));
                sawError = true;
            }
        }

        public void checkDefined(String elementName, Object value) {
            assert elementName != null;
            if (value == null) {
                error(
                        attribute.name,
                        "@{0} must declare an element \"{1}=...\"",
                        TARGET_NAME,
                        elementName);
            }
        }

        PropertySymbol takeProperty(String elementName) {
            assert elementName != null;
            AstAttributeElement nameElement = elements.remove(elementName);
            if (nameElement == null) {
                return null;
            } else if ((nameElement.value instanceof AstSimpleName) == false) {
                error(
                        nameElement,
                        "@{0}.{1} must has a property name",
                        TARGET_NAME,
                        elementName);
                return null;
            }
            AstSimpleName value = (AstSimpleName) nameElement.value;
            PropertySymbol property = model.createPropertySymbol(value);
            if (property.findDeclaration() == null) {
                error(
                        value,
                        "{0} is not declared in {1}",
                        value,
                        model.getName());
                return null;
            }
            return property;
        }

        AstLiteral takeLiteral(String elementName) {
            assert elementName != null;
            AstAttributeElement nameElement = elements.remove(elementName);
            if (nameElement == null) {
                return null;
            } else if ((nameElement.value instanceof AstLiteral) == false) {
                error(
                        nameElement,
                        "@{0}.{1} must has a literal value",
                        TARGET_NAME,
                        elementName);
                return null;
            }
            return (AstLiteral) nameElement.value;
        }

        void error(AstNode elemenet, String format, Object... arguments) {
            environment.report(new Diagnostic(Level.ERROR, attribute, format, arguments));
            sawError = true;
        }
    }
}
