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
package com.asakusafw.utils.java.model.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.DocText;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * A builder for building Javadoc.
 */
public class JavadocBuilder {

    private static final Pattern ESCAPE = Pattern.compile("@"); //$NON-NLS-1$

    private final ModelFactory f;

    private List<DocBlock> blocks;

    private String currentTag;

    private List<DocElement> elements;

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        this.f = factory;
        this.blocks = new ArrayList<DocBlock>();
        this.currentTag = ""; // overview //$NON-NLS-1$
        this.elements = new ArrayList<DocElement>();
    }

    /**
     * Returns a copy of this builder.
     * @return the copy
     */
    public JavadocBuilder copy() {
        JavadocBuilder copy = new JavadocBuilder(f);
        copy.blocks = new ArrayList<DocBlock>(blocks);
        copy.currentTag = currentTag;
        copy.elements = new ArrayList<DocElement>(elements);
        return copy;
    }

    /**
     * Returns the built {@link Javadoc} object.
     * @return the built object
     */
    public Javadoc toJavadoc() {
        flushBlock(""); //$NON-NLS-1$
        return f.newJavadoc(blocks);
    }

    /**
     * Starts a new block in this builder.
     * The current building block will be finished before this operation.
     * @param tag the block tag (the starting <code>&quot;&#64;&quot;</code> can be omitted)
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder block(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag must not be null"); //$NON-NLS-1$
        }
        if (tag.startsWith("@")) { //$NON-NLS-1$
            flushBlock(tag);
        } else {
            flushBlock("@" + tag); //$NON-NLS-1$
        }
        return this;
    }

    /**
     * Appends an inline element into this builder.
     * @param element the target element
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder inline(DocElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        elements.add(element);
        return this;
    }

    /**
     * Appends inline elements into this builder.
     * @param elems the target elements
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder inline(List<? extends DocElement> elems) {
        if (elems == null) {
            throw new IllegalArgumentException("elems must not be null"); //$NON-NLS-1$
        }
        elements.addAll(elems);
        return this;
    }

    /**
     * Starts a <code>&#64;param</code> tag block for the target parameter in this builder.
     * The current building block will be finished before this operation.
     * @param name the target parameter name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder param(String name) {
        return param(f.newSimpleName(name));
    }

    /**
     * Starts a <code>&#64;param</code> tag block for the target parameter in this builder.
     * The current building block will be finished before this operation.
     * @param name the target parameter name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder param(SimpleName name) {
        block("@param"); //$NON-NLS-1$
        elements.add(name);
        return this;
    }

    /**
     * Starts a <code>&#64;param</code> tag block for the target type parameter in this builder.
     * The current building block will be finished before this operation.
     * @param name the target parameter name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder typeParam(String name) {
        return typeParam(f.newSimpleName(name));
    }

    /**
     * Starts a <code>&#64;param</code> tag block for the target type parameter in this builder.
     * The current building block will be finished before this operation.
     * @param name the target parameter name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder typeParam(SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        block("@param"); //$NON-NLS-1$
        elements.add(f.newDocText("<")); //$NON-NLS-1$
        elements.add(name);
        elements.add(f.newDocText(">")); //$NON-NLS-1$
        return this;
    }

    /**
     * Starts a <code>&#64;param</code> tag block for the target type parameter in this builder.
     * The current building block will be finished before this operation.
     * @param typeVariable the type variable
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder typeParam(Type typeVariable) {
        if (typeVariable == null) {
            throw new IllegalArgumentException("typeVariable must not be null"); //$NON-NLS-1$
        }
        if (typeVariable.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("typeVariable must be a simple name-type");
        }
        NamedType named = (NamedType) typeVariable;
        if (named.getModelKind() != ModelKind.SIMPLE_NAME) {
            throw new IllegalArgumentException("typeVariable must have a simple name");
        }

        return typeParam((SimpleName) named.getName());
    }

    /**
     * Starts a <code>&#64;return</code> tag block in this builder.
     * The current building block will be finished before this operation.
     * @return this
     */
    public JavadocBuilder returns() {
        block("@return"); //$NON-NLS-1$
        return this;
    }

    /**
     * Starts a <code>&#64;throw</code> tag block for the target type in this builder.
     * The current building block will be finished before this operation.
     * @param type the target exception type
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder exception(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple name-type");
        }
        block("@throws"); //$NON-NLS-1$
        elements.add(((NamedType) type).getName());
        return this;
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target type in this builder.
     * The current building block will be finished before this operation.
     * @param type the target type
     * @return this
     */
    public JavadocBuilder seeType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple name-type");
        }
        return see(((NamedType) type).getName());
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target field in this builder.
     * The current building block will be finished before this operation.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeField(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return seeField(null, f.newSimpleName(name));
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target field in this builder.
     * The current building block will be finished before this operation.
     * @param type the target field type
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeField(Type type, String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return seeField(type, f.newSimpleName(name));
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target field in this builder.
     * The current building block will be finished before this operation.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeField(SimpleName name) {
        return seeField(null, name);
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target field in this builder.
     * The current building block will be finished before this operation.
     * @param type the target field type
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeField(Type type, SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return see(f.newDocField(type, name));
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target method in this builder.
     * The current building block will be finished before this operation.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(
                null,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target method in this builder.
     * The current building block will be finished before this operation.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(null, f.newSimpleName(name), parameterTypes);
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target method in this builder.
     * The current building block will be finished before this operation.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(null, name, Arrays.asList(parameterTypes));
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target method in this builder.
     * The current building block will be finished before this operation.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(null, name, parameterTypes);
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target method in this builder.
     * The current building block will be finished before this operation.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            Type type,
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(
                type,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target method in this builder.
     * The current building block will be finished before this operation.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            Type type,
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(type, f.newSimpleName(name), parameterTypes);
    }

    /**
     * Starts <code>&#64;see</code> tag block about the specified element in this builder.
     * The current building block will be finished before this operation.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            Type type,
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(type, name, Arrays.asList(parameterTypes));
    }

    /**
     * Starts <code>&#64;see</code> tag block about the specified element in this builder.
     * The current building block will be finished before this operation.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(
            Type type,
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        List<DocMethodParameter> parameters = new ArrayList<DocMethodParameter>();
        for (Type parameterType : parameterTypes) {
            parameters.add(f.newDocMethodParameter(parameterType, null, false));
        }
        return see(f.newDocMethod(type, name, parameters));
    }

    /**
     * Starts <code>&#64;see</code> tag block about the specified element in this builder.
     * The current building block will be finished before this operation.
     * @param element the target element
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder see(DocElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        block("@see"); //$NON-NLS-1$
        elements.add(element);
        return this;
    }

    /**
     * Appends a plain text into this builder.
     * @param pattern the text pattern in form of {@link MessageFormat#format(String, Object...)}
     * @param arguments the arguments for the pattern
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder text(String pattern, Object... arguments) {
        elements.add(escape(pattern, arguments));
        return this;
    }

    /**
     * Appends a <code>&#64;code</code> inline block into this builder.
     * @param pattern the text pattern in form of {@link MessageFormat#format(String, Object...)}
     * @param arguments the arguments for the pattern
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder code(String pattern, Object... arguments) {
        elements.add(f.newDocBlock(
                "@code", //$NON-NLS-1$
                Collections.singletonList(escape(pattern, arguments))));
        return this;
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target type into this builder.
     * @param type the target type
     * @return this
     */
    public JavadocBuilder linkType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple name-type");
        }
        return link(((NamedType) type).getName());
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return linkField(null, f.newSimpleName(name));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param type the target field type
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(Type type, String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return linkField(type, f.newSimpleName(name));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(SimpleName name) {
        return linkField(null, name);
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param type the target field type
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(Type type, SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return link(f.newDocField(type, name));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(
                null,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(null, f.newSimpleName(name), parameterTypes);
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(null, name, Arrays.asList(parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(null, name, parameterTypes);
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            Type type,
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(
                type,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            Type type,
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(type, f.newSimpleName(name), parameterTypes);
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            Type type,
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(type, name, Arrays.asList(parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(
            Type type,
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        List<DocMethodParameter> parameters = new ArrayList<DocMethodParameter>();
        for (Type parameterType : parameterTypes) {
            parameters.add(f.newDocMethodParameter(parameterType, null, false));
        }
        return link(f.newDocMethod(type, name, parameters));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target element into this builder.
     * @param element the target element
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder link(DocElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        elements.add(f.newDocBlock(
            "@link", //$NON-NLS-1$
            Collections.singletonList(element)));
        return this;
    }

    private DocText escape(String pattern, Object... arguments) {
        String text = MessageFormat.format(pattern, arguments);
        String escaped = ESCAPE.matcher(text).replaceAll("&#64;"); //$NON-NLS-1$
        return f.newDocText(escaped);
    }

    private void flushBlock(String nextTag) {
        if (currentTag.length() >= 0 || elements.isEmpty() == false) {
            blocks.add(f.newDocBlock(currentTag, elements));
            elements.clear();
        }
        this.currentTag = nextTag;
    }
}
