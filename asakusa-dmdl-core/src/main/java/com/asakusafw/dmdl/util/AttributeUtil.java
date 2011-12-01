/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;

/**
 * Utility methods for {@link AstAttribute}.
 * @since 0.2.0
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
        Map<String, AstAttributeElement> results = new HashMap<String, AstAttributeElement>();
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
        List<Diagnostic> results = new ArrayList<Diagnostic>();
        for (AstAttributeElement element : attribute.elements) {
            results.add(new Diagnostic(
                    Level.ERROR,
                    element.name,
                    "No such element \"{0}\" in attribute \"{1}\"",
                    element.name,
                    attribute.name));
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
        AstAttributeElement target = elements.remove(elementName);
        if (target == null) {
            if (mandatory) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        attribute.name,
                        "@{0} must declare an element \"{1}=...\"",
                        attribute.name.toString(),
                        elementName));
            }
            return null;
        } else if ((target.value instanceof AstLiteral) == false) {
            environment.report(new Diagnostic(
                    Level.ERROR,
                    target,
                    "@{0}({1}) must be a string literal",
                    attribute.name.toString(),
                    elementName));
            return null;
        } else {
            AstLiteral literal = (AstLiteral) target.value;
            if (literal.kind != LiteralKind.STRING) {
                environment.report(new Diagnostic(
                        Level.ERROR,
                        target,
                        "@{0}({1}) must be a string literal",
                        attribute.name.toString(),
                        elementName));
                return null;
            }
            return literal.toStringValue();
        }
    }

    private AttributeUtil() {
        return;
    }
}
