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
package com.asakusafw.dmdl.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;

/**
 * Utility methods for {@link AstAttribute}.
 * @since 0.2.0
 * @version 0.7.0
 */
public final class AttributeUtil {

    /**
     * Extract the attributes into (name, element) pairs.
     * <p>
     * The result map can be modified in user programs.
     * </p>
     * @param attribute the target attribute
     * @return the extracted pairs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Map<String, AstAttributeElement> getElementMap(AstAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        Map<String, AstAttributeElement> results = Maps.create();
        for (AstAttributeElement element : attribute.elements) {
            results.put(element.name.identifier, element);
        }
        return results;
    }

    /**
     * Returns erroneous diagnostics for the undefined
     * {@link AstAttributeElement attribut elements}.
     * @param attribute the elements' owner
     * @param elements the invalid elements
     * @return the corresponded diagnostics, or an empty list if the elements are empty
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<Diagnostic> reportInvalidElements(
            AstAttribute attribute,
            Collection<? extends AstAttributeElement> elements) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }
        List<Diagnostic> results = Lists.create();
        for (AstAttributeElement element : elements) {
            results.add(new Diagnostic(
                    Level.ERROR,
                    element.name,
                    Messages.getString("AttributeUtil.diagnosticUnknownElement"), //$NON-NLS-1$
                    attribute.name,
                    element.name));
        }
        return results;
    }

    /**
     * Removes {@link AstAttributeElement} from {@code elements} and extracts string value from it.
     * If the element is not a string literal, this reports error into the {@code environment}.
     * @param environment current environment
     * @param attribute target attribute
     * @param elements elements in target attribute
     * @param elementName target element name
     * @param literalKind target element kind
     * @param mandatory {@code true} for mandatory element (report error if the element is not in map)
     * @return the extracted literal, or {@code null} if is not defined or not a valid literal
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.0
     */
    public static AstLiteral takeLiteral(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements,
            String elementName,
            LiteralKind literalKind,
            boolean mandatory) {
        if (environment == null) {
            throw new IllegalArgumentException("environment must not be null"); //$NON-NLS-1$
        }
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        if (elementName == null) {
            throw new IllegalArgumentException("elementName must not be null"); //$NON-NLS-1$
        }
        if (literalKind == null) {
            throw new IllegalArgumentException("literalKind must not be null"); //$NON-NLS-1$
        }
        AstAttributeElement target = elements.remove(elementName);
        if (target == null) {
            if (mandatory) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        attribute.name,
                        Messages.getString("AttributeUtil.diagnosticMissingElement"), //$NON-NLS-1$
                        attribute.name.toString(),
                        elementName));
            }
            return null;
        } else if (isLiteral(target.value, literalKind) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    target,
                    Messages.getString("AttributeUtil.diagnosticNotLiteral"), //$NON-NLS-1$
                    attribute.name.toString(),
                    elementName,
                    literalKind));
            return null;
        } else {
            return (AstLiteral) target.value;
        }
    }

    private static boolean isLiteral(AstNode node, LiteralKind kind) {
        if (node instanceof AstLiteral) {
            return ((AstLiteral) node).getKind() == kind;
        }
        return false;
    }

    /**
     * Removes {@link AstAttributeElement} from {@code elements} and extracts string value from it.
     * If the element is not a string literal, this reports error into the {@code environment}.
     * @param environment current environment
     * @param attribute target attribute
     * @param elements elements in target attribute
     * @param elementName target element name
     * @param mandatory {@code true} for mandatory element (report error if the element is not in map)
     * @return the extracted string, or {@code null} if is not defined or not a string
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String takeString(
            DmdlSemantics environment,
            AstAttribute attribute,
            Map<String, AstAttributeElement> elements,
            String elementName,
            boolean mandatory) {
        AstLiteral literal = takeLiteral(environment, attribute, elements, elementName, LiteralKind.STRING, mandatory);
        if (literal != null) {
            return literal.toStringValue();
        }
        return null;
    }

    /**
     * Checks field type and report diagnostics.
     * @param environment the current environment
     * @param declaration the target declaration
     * @param attribute the target attribute
     * @param types expected types
     * @return {@code true} if check is successfully completed, otherwise {@code false}
     * @since 0.7.0
     */
    public static boolean checkFieldType(
            DmdlSemantics environment,
            PropertyDeclaration declaration,
            AstAttribute attribute,
            BasicTypeKind... types) {
        if (environment == null) {
            throw new IllegalArgumentException("environment must not be null"); //$NON-NLS-1$
        }
        if (declaration == null) {
            throw new IllegalArgumentException("declaration must not be null"); //$NON-NLS-1$
        }
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        if (types == null) {
            throw new IllegalArgumentException("types must not be null"); //$NON-NLS-1$
        }
        Type type = declaration.getType();
        if (type instanceof BasicType) {
            BasicTypeKind kind = ((BasicType) type).getKind();
            for (BasicTypeKind accept : types) {
                if (kind == accept) {
                    return true;
                }
            }
        }
        environment.report(new Diagnostic(
                Level.ERROR,
                attribute,
                Messages.getString("AttributeUtil.diagnosticInvalidTypeElement"), //$NON-NLS-1$
                declaration.getOwner().getName().identifier,
                declaration.getName().identifier,
                attribute.name.toString(),
                Arrays.asList(types)));
        return false;
    }

    /**
     * Checks which string is not null and report diagnostics.
     * @param environment the current environment
     * @param targetNode the target node
     * @param targetLabel the target label
     * @param value the target value
     * @return {@code true} if check is successfully completed, otherwise {@code false}
     * @since 0.7.0
     */
    public static boolean checkPresent(
            DmdlSemantics environment,
            AstNode targetNode,
            String targetLabel,
            String value) {
        if (value == null || value.isEmpty()) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    targetNode,
                    Messages.getString("AttributeUtil.diagnosticEmptyString"), //$NON-NLS-1$
                    targetLabel));
            return false;
        }
        return true;
    }

    /**
     * Checks integer range and report diagnostics.
     * @param environment the current environment
     * @param targetNode the target node
     * @param targetLabel the target label
     * @param value the target value
     * @param minimum the minimum value (inclusive), or {@code null} to unlimited
     * @param maximum the maximum value (inclusive), or {@code null} to unlimited
     * @return {@code true} if check is successfully completed, otherwise {@code false}
     * @since 0.7.0
     */
    public static boolean checkRange(
            DmdlSemantics environment,
            AstNode targetNode,
            String targetLabel,
            BigInteger value,
            Long minimum, Long maximum) {
        if (maximum != null && value.compareTo(BigInteger.valueOf(maximum)) > 0) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    targetNode,
                    Messages.getString("AttributeUtil.diagnosticNumberTooLarge"), //$NON-NLS-1$
                    targetLabel,
                    maximum,
                    value));
            return false;
        } else if (minimum != null && value.compareTo(BigInteger.valueOf(minimum)) < 0) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    targetNode,
                    Messages.getString("AttributeUtil.diagnosticNumberTooSmall"), //$NON-NLS-1$
                    targetLabel,
                    minimum,
                    value));
            return false;
        }
        return true;
    }

    private AttributeUtil() {
        return;
    }
}
