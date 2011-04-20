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

/**
 * Utility methods for {@link AstAttribute}.
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

    private AttributeUtil() {
        return;
    }
}
