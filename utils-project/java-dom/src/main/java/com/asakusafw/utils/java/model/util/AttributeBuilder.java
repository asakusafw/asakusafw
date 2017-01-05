/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.utils.java.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.Type;

// CHECKSTYLE:OFF
/**
 * A builder for building attributes of declarations.
 * @since 0.1.0
 * @version 0.9.0
 */
public class AttributeBuilder {

    private final ModelFactory f;

    private final List<Attribute> attributes;

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public AttributeBuilder(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        this.f = factory;
        this.attributes = new ArrayList<>();
    }

    /**
     * Returns a copy of this builder.
     * @return the copy
     */
    public AttributeBuilder copy() {
        AttributeBuilder copy = new AttributeBuilder(f);
        copy.attributes.addAll(attributes);
        return copy;
    }

    /**
     * Returns the attribute list which contains the added attributes as their order.
     * @return the attributes
     */
    public List<Attribute> toAttributes() {
        return new ArrayList<>(attributes);
    }

    /**
     * Returns the Java modifier list which contains the added modifiers as their order.
     * Note that, each attribute which is not a modifier is ignored.
     * @return the Java modifiers
     */
    public List<Modifier> toModifiers() {
        List<Modifier> results = new ArrayList<>();
        for (Attribute attribute : toAttributes()) {
            if (attribute instanceof Modifier) {
                results.add((Modifier) attribute);
            }
        }
        return results;
    }

    /**
     * Returns the Java annotation list which contains the added annotations as their order.
     * Note that, each attribute which is not an annotation will be ignored.
     * @return the Java annotations
     */
    public List<Annotation> toAnnotations() {
        List<Annotation> results = new ArrayList<>();
        for (Attribute attribute : toAttributes()) {
            if (attribute instanceof Annotation) {
                results.add((Annotation) attribute);
            }
        }
        return results;
    }

// CHECKSTYLE:OFF MethodNameCheck

    /**
     * Appends {@code public} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Public() {
        return modifier(ModifierKind.PUBLIC);
    }

    /**
     * Appends {@code protected} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Protected() {
        return modifier(ModifierKind.PROTECTED);
    }

    /**
     * Appends {@code private} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Private() {
        return modifier(ModifierKind.PRIVATE);
    }

    /**
     * Appends {@code static} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Static() {
        return modifier(ModifierKind.STATIC);
    }

    /**
     * Appends {@code abstract} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Abstract() {
        return modifier(ModifierKind.ABSTRACT);
    }

    /**
     * Appends {@code default} modifier to this builder.
     * @return this
     * @since 0.9.0
     */
    public AttributeBuilder Default() {
        return modifier(ModifierKind.DEFAULT);
    }

    /**
     * Appends {@code native} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Native() {
        return modifier(ModifierKind.NATIVE);
    }

    /**
     * Appends {@code final} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Final() {
        return modifier(ModifierKind.FINAL);
    }

    /**
     * Appends {@code synchronized} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Synchronized() {
        return modifier(ModifierKind.SYNCHRONIZED);
    }

    /**
     * Appends {@code transient} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Transient() {
        return modifier(ModifierKind.TRANSIENT);
    }

    /**
     * Appends {@code volatile} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Volatile() {
        return modifier(ModifierKind.VOLATILE);
    }

    /**
     * Appends {@code strictfp} modifier to this builder.
     * @return this
     */
    public AttributeBuilder Strictfp() {
        return modifier(ModifierKind.STRICTFP);
    }

// CHECKSTYLE:ON MethodNameCheck

    /**
     * Appends the modifier to this builder.
     * @param modifier the target modifier kind
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public AttributeBuilder modifier(ModifierKind modifier) {
        if (modifier == null) {
            throw new IllegalArgumentException("modifier must not be null"); //$NON-NLS-1$
        }
        return chain(f.newModifier(modifier));
    }

    /**
     * Appends the annotation to the builder.
     * @param type the annotation type
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public AttributeBuilder annotation(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        return annotation(f.newMarkerAnnotation((NamedType) type));
    }

    /**
     * Appends the marker annotation to the builder.
     * @param type the annotation type
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public AttributeBuilder annotation(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return annotation(Models.toType(f, type));
    }

    /**
     * Appends the single element annotation to the builder.
     * @param type the annotation type
     * @param value the annotation value
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public AttributeBuilder annotation(Type type, Expression value) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        return annotation(f.newSingleElementAnnotation((NamedType) type, value));
    }

    /**
     * Appends the single element annotation to the builder.
     * @param type the annotation type
     * @param value the annotation value
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public AttributeBuilder annotation(java.lang.reflect.Type type, Expression value) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return annotation(Models.toType(f, type), value);
    }

    /**
     * Adds the specified annotation.
     * @param type the target annotation type
     * @param elements the element name-value pairs
     * @return this
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public AttributeBuilder annotation(Type type, Map<? extends String, ? extends Expression> elements) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elems = new ArrayList<>();
        for (Map.Entry<? extends String, ? extends Expression> entry : elements.entrySet()) {
            elems.add(f.newAnnotationElement(f.newSimpleName(entry.getKey()), entry.getValue()));
        }
        return annotation(f.newNormalAnnotation((NamedType) type, elems));
    }

    /**
     * Appends the annotation to the builder.
     * @param type the annotation type
     * @param elementName the element name
     * @param elementValue the element value
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName, Expression elementValue) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName == null) {
            throw new IllegalArgumentException("elementName must not be null"); //$NON-NLS-1$
        }
        if (elementValue == null) {
            throw new IllegalArgumentException("elementValue must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elements = new ArrayList<>();
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName), elementValue));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * Appends the annotation to the builder.
     * @param type the annotation type
     * @param elementName1 the element name (1)
     * @param elementValue1 the element value (1)
     * @param elementName2 the element name (2)
     * @param elementValue2 the element value (2)
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-1$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elements = new ArrayList<>();
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName2), elementValue2));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * Appends the annotation to the builder.
     * @param type the annotation type
     * @param elementName1 the element name (1)
     * @param elementValue1 the element value (1)
     * @param elementName2 the element name (2)
     * @param elementValue2 the element value (2)
     * @param elementName3 the element name (3)
     * @param elementValue3 the element value (3)
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2,
            String elementName3, Expression elementValue3) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-1$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-1$
        }
        if (elementName3 == null) {
            throw new IllegalArgumentException("elementName3 must not be null"); //$NON-NLS-1$
        }
        if (elementValue3 == null) {
            throw new IllegalArgumentException("elementValue3 must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elements = new ArrayList<>();
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName2), elementValue2));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName3), elementValue3));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * Appends the annotation to the builder.
     * @param type the annotation type
     * @param elementName1 the element name (1)
     * @param elementValue1 the element value (1)
     * @param elementName2 the element name (2)
     * @param elementValue2 the element value (2)
     * @param elementName3 the element name (3)
     * @param elementValue3 the element value (3)
     * @param elementName4 the element name (4)
     * @param elementValue4 the element value (4)
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2,
            String elementName3, Expression elementValue3,
            String elementName4, Expression elementValue4) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-1$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-1$
        }
        if (elementName3 == null) {
            throw new IllegalArgumentException("elementName3 must not be null"); //$NON-NLS-1$
        }
        if (elementValue3 == null) {
            throw new IllegalArgumentException("elementValue3 must not be null"); //$NON-NLS-1$
        }
        if (elementName4 == null) {
            throw new IllegalArgumentException("elementName4 must not be null"); //$NON-NLS-1$
        }
        if (elementValue4 == null) {
            throw new IllegalArgumentException("elementValue4 must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elements = new ArrayList<>();
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName2), elementValue2));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName3), elementValue3));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName4), elementValue4));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

// CHECKSTYLE:OFF ParameterNumberCheck

    /**
     * Appends the annotation to the builder.
     * @param type the annotation type
     * @param elementName1 the element name (1)
     * @param elementValue1 the element value (1)
     * @param elementName2 the element name (2)
     * @param elementValue2 the element value (2)
     * @param elementName3 the element name (3)
     * @param elementValue3 the element value (3)
     * @param elementName4 the element name (4)
     * @param elementValue4 the element value (4)
     * @param elementName5 the element name (5)
     * @param elementValue5 the element value (5)
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2,
            String elementName3, Expression elementValue3,
            String elementName4, Expression elementValue4,
            String elementName5, Expression elementValue5) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-1$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-1$
        }
        if (elementName3 == null) {
            throw new IllegalArgumentException("elementName3 must not be null"); //$NON-NLS-1$
        }
        if (elementValue3 == null) {
            throw new IllegalArgumentException("elementValue3 must not be null"); //$NON-NLS-1$
        }
        if (elementName4 == null) {
            throw new IllegalArgumentException("elementName4 must not be null"); //$NON-NLS-1$
        }
        if (elementValue4 == null) {
            throw new IllegalArgumentException("elementValue4 must not be null"); //$NON-NLS-1$
        }
        if (elementName5 == null) {
            throw new IllegalArgumentException("elementName5 must not be null"); //$NON-NLS-1$
        }
        if (elementValue5 == null) {
            throw new IllegalArgumentException("elementValue5 must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elements = new ArrayList<>();
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName2), elementValue2));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName3), elementValue3));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName4), elementValue4));
        elements.add(f.newAnnotationElement(f.newSimpleName(elementName5), elementValue5));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

// CHECKSTYLE:ON ParameterNumberCheck

    /**
     * Appends the annotation to the builder.
     * @param annotation the target annotation
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public AttributeBuilder annotation(Annotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("annotation must not be null"); //$NON-NLS-1$
        }
        return chain(annotation);
    }

    private AttributeBuilder chain(Attribute attribute) {
        assert attribute != null;
        attributes.add(attribute);
        return this;
    }
}
