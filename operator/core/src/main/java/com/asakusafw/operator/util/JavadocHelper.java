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
package com.asakusafw.operator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.model.OperatorDescription.Document;
import com.asakusafw.operator.model.OperatorDescription.ExternalDocument;
import com.asakusafw.operator.model.OperatorDescription.ParameterReference;
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.operator.model.OperatorDescription.ReferenceDocument;
import com.asakusafw.operator.model.OperatorDescription.SpecialReference;
import com.asakusafw.operator.model.OperatorDescription.TextDocument;
import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.DocText;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.parser.javadoc.JavadocConverter;
import com.asakusafw.utils.java.parser.javadoc.JavadocParseException;

/**
 * Common helper methods about documentation comments.
 */
public class JavadocHelper {

    private final CompileEnvironment environment;

    private final Map<String, List<List<DocElement>>> blocks = new HashMap<>();

    private ExecutableElement executable;

    /**
     * Creates a new instance.
     * @param environment current environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JavadocHelper(CompileEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
    }

    /**
     * Put a documented element.
     * @param element the documented element
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void put(Element element) {
        Objects.requireNonNull(element, "element must not be null"); //$NON-NLS-1$
        if (this.executable == null
                && (element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.CONSTRUCTOR)) {
            this.executable = (ExecutableElement) element;
        }
        Javadoc javadoc = parseJavadoc(element);
        boolean sawSummary = false;
        for (DocBlock block : javadoc.getBlocks()) {
            String tag = block.getTag();
            sawSummary |= tag.isEmpty();
            appendBlock(tag, block.getElements());
        }
        if (sawSummary == false) {
            appendBlock("", Collections.emptyList()); //$NON-NLS-1$
        }
    }

    private Javadoc parseJavadoc(Element element) {
        assert environment != null;
        assert element != null;
        String comment = environment.getProcessingEnvironment().getElementUtils().getDocComment(element);
        try {
            return parseJavadoc(comment);
        } catch (JavadocParseException e) {
            environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.WARNING,
                    e.getMessage(),
                    element);
            return Models.getModelFactory().newJavadoc(Collections.emptyList());
        }
    }

    private static Javadoc parseJavadoc(String comment) throws JavadocParseException {
        if (comment == null) {
            return Models.getModelFactory().newJavadoc(Collections.emptyList());
        }
        String string = comment;
        if (comment.startsWith("/**") == false) { //$NON-NLS-1$
            string = "/**" + string; //$NON-NLS-1$
        }
        if (comment.endsWith("*/") == false) { //$NON-NLS-1$
            string = string + "*/"; //$NON-NLS-1$
        }
        return new JavadocConverter(Models.getModelFactory()).convert(string, 0);
    }

    private void appendBlock(String tag, List<? extends DocElement> elements) {
        assert tag != null;
        assert elements != null;
        List<List<DocElement>> list = blocks.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            blocks.put(tag, list);
        }
        list.add(new ArrayList<>(elements));
    }

    /**
     * Returns the document elements for the description.
     * @param document description
     * @return related elements, or an empty list if they are not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<? extends DocElement> get(Document document) {
        Objects.requireNonNull(document, "document must not be null"); //$NON-NLS-1$
        switch (document.getKind()) {
        case TEXT:
            return text(((TextDocument) document).getText());
        case REFERENCE:
            return reference(((ReferenceDocument) document).getReference());
        case EXTERNAL:
            return external(((ExternalDocument) document).getElement());
        default:
            throw new AssertionError(document.getKind());
        }
    }

    /**
     * Returns the parameter document elements.
     * @param name parameter name
     * @return related elements, or an empty list if the parameter does not exist
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<? extends DocElement> getParameter(String name) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        List<List<DocElement>> params = blocks.get("@param"); //$NON-NLS-1$
        if (params == null) {
            return Collections.emptyList();
        }
        for (List<DocElement> param : params) {
            if (param.isEmpty()) {
                continue;
            }
            if (hasName(param.get(0), name)) {
                return param.subList(1, param.size());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the type parameter document elements.
     * @param name type parameter name
     * @return related elements, or an empty list if the type parameter does not exist
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<? extends DocElement> getTypeParameter(String name) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        List<List<DocElement>> params = blocks.get("@param"); //$NON-NLS-1$
        if (params == null) {
            return Collections.emptyList();
        }
        for (List<DocElement> param : params) {
            if (param.size() < 3) {
                continue;
            }
            if (hasName(param.get(0), "<") //$NON-NLS-1$
                    && hasName(param.get(1), name)
                    && hasName(param.get(2), ">")) { //$NON-NLS-1$
                return param.subList(3, param.size());
            }
        }
        return Collections.emptyList();
    }

    private static boolean hasName(DocElement element, String name) {
        assert element != null;
        assert name != null;
        switch (element.getModelKind()) {
        case SIMPLE_NAME:
            return name.equals(((SimpleName) element).getToken());
        case DOC_TEXT:
            return name.equals(((DocText) element).getString());
        default:
            return false;
        }
    }

    private List<? extends DocElement> text(String text) {
        Javadoc javadoc;
        try {
            javadoc = parseJavadoc(text);
        } catch (JavadocParseException e) {
            environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.WARNING,
                    e.getMessage(),
                    executable);
            return Collections.emptyList();
        }
        if (javadoc.getBlocks().isEmpty()) {
            return Collections.emptyList();
        }
        return javadoc.getBlocks().get(0).getElements();
    }

    private List<? extends DocElement> reference(Reference reference) {
        switch (reference.getKind()) {
        case METHOD:
            return getBlock(""); //$NON-NLS-1$
        case RETURN:
            return getBlock("@return"); //$NON-NLS-1$
        case PARAMETER:
            return getParameter(((ParameterReference) reference).getLocation());
        case SPECIAL:
            return text(((SpecialReference) reference).getInfo());
        default:
            throw new AssertionError(reference.getKind());
        }
    }

    private List<? extends DocElement> getParameter(int location) {
        if (executable == null || location >= executable.getParameters().size()) {
            return Collections.emptyList();
        }
        String name = executable.getParameters().get(location).getSimpleName().toString();
        return getParameter(name);
    }

    private List<? extends DocElement> external(Element element) {
        Javadoc javadoc = parseJavadoc(element);
        List<? extends DocBlock> elementBlocks = javadoc.getBlocks();
        if (elementBlocks.isEmpty()) {
            return Collections.emptyList();
        }
        DocBlock first = elementBlocks.get(0);
        if (first.getTag().isEmpty()) {
            return first.getElements();
        }
        return Collections.emptyList();
    }

    private List<? extends DocElement> getBlock(String tag) {
        assert tag != null;
        List<List<DocElement>> list = blocks.get(tag);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.get(0);
    }
}
