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
package com.asakusafw.dmdl.thundergate.emitter;

import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.dmdl.analyzer.driver.AutoProjectionDriver;
import com.asakusafw.dmdl.analyzer.driver.NamespaceDriver;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstAttributeValueArray;
import com.asakusafw.dmdl.model.AstBasicType;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstName;
import com.asakusafw.dmdl.model.AstQualifiedName;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstType;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.thundergate.driver.CacheSupportDriver;
import com.asakusafw.dmdl.thundergate.driver.OriginalNameDriver;
import com.asakusafw.dmdl.thundergate.driver.PrimaryKeyDriver;
import com.asakusafw.dmdl.thundergate.model.Aggregator;
import com.asakusafw.dmdl.thundergate.model.Attribute;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.ModelReference;
import com.asakusafw.dmdl.thundergate.model.PropertyType;
import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.dmdl.thundergate.model.Source;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.utils.collections.Lists;

/**
 * DMDL AST building utility.
 * @since 0.2.0
 * @version 0.2.3
 */
public final class AstBuilder {

    /**
     * Converts the name into the corresponded DMDL name.
     * @param name target name
     * @return converted name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstSimpleName toDmdlName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        boolean requestSeparated = false;
        StringBuilder buf = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isJavaIdentifierPart(c) && c != '_') {
                if (requestSeparated) {
                    buf.append('_');
                }
                buf.append(Character.toLowerCase(c));
                requestSeparated = false;
            } else if (buf.length() > 0) {
                requestSeparated = true;
            }
        }
        if (buf.length() == 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid name for DMDL: {0}",
                    name));
        }
        return new AstSimpleName(null, buf.toString());
    }

    private static AstName toName(String name) {
        assert name != null;
        String[] segments = name.split("\\.");
        AstName current = toSimpleName(segments[0]);
        for (int i = 1; i < segments.length; i++) {
            current = new AstQualifiedName(null, current, toSimpleName(segments[i]));
        }
        return current;
    }

    private static AstSimpleName toSimpleName(String name) {
        assert name != null;
        return new AstSimpleName(null, name);
    }

    /**
     * Returns the DMDL name of the model.
     * @param model target model
     * @return converted name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstSimpleName toName(ModelReference model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        return toDmdlName(model.getSimpleName());
    }

    /**
     * Returns the DMDL name of the property.
     * @param property target property
     * @return converted name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstSimpleName toName(ModelProperty property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        return toDmdlName(property.getName());
    }

    /**
     * Returns the DMDL name of the property source.
     * @param source target property source
     * @return converted name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstSimpleName toName(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        return toDmdlName(source.getName());
    }

    /**
     * Returns the DMDL name of the aggregator.
     * @param aggregator target aggregator
     * @return converted name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstName toName(Aggregator aggregator) {
        if (aggregator == null) {
            throw new IllegalArgumentException("aggregator must not be null"); //$NON-NLS-1$
        }
        return toDmdlName(convert(aggregator).name());
    }

    private static PropertyMappingKind convert(Aggregator aggregator) {
        assert aggregator != null;
        switch (aggregator) {
        case IDENT:
            return PropertyMappingKind.ANY;
        case COUNT:
            return PropertyMappingKind.COUNT;
        case MAX:
            return PropertyMappingKind.MAX;
        case MIN:
            return PropertyMappingKind.MIN;
        case SUM:
            return PropertyMappingKind.SUM;
        default:
            throw new AssertionError(aggregator);
        }
    }

    /**
     * Converts the DMDL type.
     * @param type target type
     * @return converted type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstType toType(PropertyType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        BasicTypeKind kind = convert(type.getKind());
        return new AstBasicType(null, kind);
    }

    private static BasicTypeKind convert(PropertyTypeKind kind) {
        assert kind != null;
        switch (kind) {
        case BIG_DECIMAL:
            return BasicTypeKind.DECIMAL;
        case BOOLEAN:
            return BasicTypeKind.BOOLEAN;
        case BYTE:
            return BasicTypeKind.BYTE;
        case DATE:
            return BasicTypeKind.DATE;
        case DATETIME:
            return BasicTypeKind.DATETIME;
        case INT:
            return BasicTypeKind.INT;
        case LONG:
            return BasicTypeKind.LONG;
        case SHORT:
            return BasicTypeKind.SHORT;
        case STRING:
            return BasicTypeKind.TEXT;
        default:
            throw new AssertionError(kind);
        }
    }

    /**
     * Returns the description.
     * @param pattern the desciption {@link MessageFormat#format(Object) format}
     * @param arguments the arguments
     * @return the description
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstDescription getDesciption(String pattern, String... arguments) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return new AstDescription(
                null,
                AstLiteral.quote(MessageFormat.format(pattern, (Object[]) arguments)));
    }

    /**
     * Returns the 'auto projection' attribute.
     * @return the attribute
     */
    public static AstAttribute getAutoProjection() {
        return new AstAttribute(
                null,
                toName(AutoProjectionDriver.TARGET_NAME));
    }

    /**
     * Returns the 'namespace' attribute.
     * @param name the namespace
     * @return the attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstAttribute getNamespace(AstName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return new AstAttribute(
                null,
                toName(NamespaceDriver.TARGET_NAME),
                new AstAttributeElement(
                        null,
                        toSimpleName(NamespaceDriver.ELEMENT_NAME),
                        name));
    }

    /**
     * Returns the 'original name' attribute.
     * @param name the original name string
     * @return the attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstAttribute getOriginalName(String name) {
        return new AstAttribute(
                null,
                toName(OriginalNameDriver.TARGET_NAME),
                new AstAttributeElement(
                        null,
                        toSimpleName(OriginalNameDriver.ELEMENT_NAME),
                        new AstLiteral(
                                null,
                                AstLiteral.quote(name),
                                LiteralKind.STRING)));
    }

    /**
     * Returns the 'primary key' attribute.
     * @param model the model
     * @return the attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstAttribute getPrimaryKey(TableModelDescription model) {
        List<AstSimpleName> primaryKeys = Lists.create();
        for (ModelProperty property : model.getProperties()) {
            if (property.getSource().getAttributes().contains(Attribute.PRIMARY_KEY)) {
                primaryKeys.add(toName(property));
            }
        }
        return new AstAttribute(
                null,
                toName(PrimaryKeyDriver.TARGET_NAME),
                new AstAttributeElement(
                        null,
                        toSimpleName(PrimaryKeyDriver.ELEMENT_NAME),
                        new AstAttributeValueArray(null, primaryKeys)));
    }

    /**
     * Returns the 'cache support' attribute.
     * @param sid system ID column
     * @param timestamp modified timestamp column
     * @return the attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.3
     */
    public static AstAttribute getCacheSupport(ModelProperty sid, ModelProperty timestamp) {
        if (sid == null) {
            throw new IllegalArgumentException("sid must not be null"); //$NON-NLS-1$
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null"); //$NON-NLS-1$
        }
        return new AstAttribute(
                null,
                toName(CacheSupportDriver.TARGET_NAME),
                new AstAttributeElement(
                        null,
                        toSimpleName(CacheSupportDriver.SID_ELEMENT_NAME),
                        toName(sid)),
                new AstAttributeElement(
                        null,
                        toSimpleName(CacheSupportDriver.TIMESTAMP_ELEMENT_NAME),
                        toName(timestamp)));
    }

    /**
     * Returns the 'cache support' attribute.
     * @param sid system ID column
     * @param timestamp modified timestamp column
     * @param deleteFlag delete flag column
     * @param deleteFlagValue delete flag value
     * @return the attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.3
     */
    public static AstAttribute getCacheSupport(
            ModelProperty sid, ModelProperty timestamp,
            ModelProperty deleteFlag, AstLiteral deleteFlagValue) {
        if (sid == null) {
            throw new IllegalArgumentException("sid must not be null"); //$NON-NLS-1$
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null"); //$NON-NLS-1$
        }
        if (deleteFlag == null) {
            throw new IllegalArgumentException("deleteFlag must not be null"); //$NON-NLS-1$
        }
        if (deleteFlagValue == null) {
            throw new IllegalArgumentException("deleteFlagValue must not be null"); //$NON-NLS-1$
        }
        return new AstAttribute(
                null,
                toName(CacheSupportDriver.TARGET_NAME),
                new AstAttributeElement(
                        null,
                        toSimpleName(CacheSupportDriver.SID_ELEMENT_NAME),
                        toName(sid)),
                new AstAttributeElement(
                        null,
                        toSimpleName(CacheSupportDriver.TIMESTAMP_ELEMENT_NAME),
                        toName(timestamp)),
                new AstAttributeElement(
                        null,
                        toSimpleName(CacheSupportDriver.DELETE_FLAG_ELEMENT_NAME),
                        toName(deleteFlag)),
                new AstAttributeElement(
                        null,
                        toSimpleName(CacheSupportDriver.DELETE_FLAG_VALUE_ELEMENT_NAME),
                        deleteFlagValue));
    }

    private AstBuilder() {
        return;
    }
}
