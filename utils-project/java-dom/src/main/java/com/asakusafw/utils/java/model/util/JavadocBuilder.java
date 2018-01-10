/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
 * @since 0.1.0
 * @version 0.9.0
 */
public class JavadocBuilder {

    private final ModelFactory f;

    private final DocElementFactory inlines;

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
        this.inlines = new DocElementFactory(factory);
        this.blocks = new ArrayList<>();
        this.currentTag = ""; // overview //$NON-NLS-1$
        this.elements = new ArrayList<>();
    }

    /**
     * Returns a copy of this builder.
     * @return the copy
     */
    public JavadocBuilder copy() {
        JavadocBuilder copy = new JavadocBuilder(f);
        copy.blocks = new ArrayList<>(blocks);
        copy.currentTag = currentTag;
        copy.elements = new ArrayList<>(elements);
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

    private static final Pattern PATTERN_INLINE = Pattern.compile("\\{(.*?)\\}"); //$NON-NLS-1$

    /**
     * Appends inline elements into this builder.
     * @param pattern the pattern string that can include <code>{n}</code>
     * @param placeholders the placeholders
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder inline(String pattern, Placeholder... placeholders) {
        DocElementFactory factory = new DocElementFactory(f);
        DocElement[] args = Stream.of(placeholders)
                .map(p -> p.apply(factory))
                .toArray(DocElement[]::new);
        Matcher matcher = PATTERN_INLINE.matcher(pattern);
        int start = 0;
        while (matcher.find()) {
            if (start < matcher.start()) {
                text(pattern.substring(start, matcher.start()));
            }
            int index;
            try {
                index = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "invalid placeholder \"{2}\": \"{0}\" at {1}", //$NON-NLS-1$
                        pattern,
                        matcher.start(),
                        matcher.group()));
            }
            if (index < 0 || index >= args.length) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "invalid placeholder \"{2}\": \"{0}\" at {1}", //$NON-NLS-1$
                        pattern,
                        matcher.start(),
                        matcher.group()));
            }
            inline(args[index]);
            start = matcher.end();
        }
        if (start < pattern.length()) {
            text(pattern.substring(start));
        }
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
     * Starts a <code>&#64;throws</code> tag block for the target type in this builder.
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
    public JavadocBuilder seeMethod(String name, Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(null, f.newSimpleName(name), Arrays.asList(parameterTypes));
    }

    /**
     * Starts a <code>&#64;see</code> tag block for the target method in this builder.
     * The current building block will be finished before this operation.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder seeMethod(String name, List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
    public JavadocBuilder seeMethod(SimpleName name, Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
    public JavadocBuilder seeMethod(SimpleName name, List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
    public JavadocBuilder seeMethod(Type type, String name, Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
    public JavadocBuilder seeMethod(Type type, String name, List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
    public JavadocBuilder seeMethod(Type type, SimpleName name, Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
    public JavadocBuilder seeMethod(Type type, SimpleName name, List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
        }
        List<DocMethodParameter> parameters = new ArrayList<>();
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
        return inline(inlines.text(pattern, arguments));
    }

    /**
     * Appends a <code>&#64;code</code> inline block into this builder.
     * @param pattern the text pattern in form of {@link MessageFormat#format(String, Object...)}
     * @param arguments the arguments for the pattern
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder code(String pattern, Object... arguments) {
        return inline(inlines.code(pattern, arguments));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target type into this builder.
     * @param type the target type
     * @return this
     */
    public JavadocBuilder linkType(Type type) {
        return inline(inlines.linkType(type));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(String name) {
        return inline(inlines.linkField(name));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param type the target field type
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(Type type, String name) {
        return inline(inlines.linkField(name));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(SimpleName name) {
        return inline(inlines.linkField(name));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target field into this builder.
     * @param type the target field type
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkField(Type type, SimpleName name) {
        return inline(inlines.linkField(type, name));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(String name, Type... parameterTypes) {
        return inline(inlines.linkMethod(name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(String name, List<? extends Type> parameterTypes) {
        return inline(inlines.linkMethod(name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(SimpleName name, Type... parameterTypes) {
        return inline(inlines.linkMethod(name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(SimpleName name, List<? extends Type> parameterTypes) {
        return inline(inlines.linkMethod(name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(Type type, String name, Type... parameterTypes) {
        return inline(inlines.linkMethod(type, name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(Type type, String name, List<? extends Type> parameterTypes) {
        return inline(inlines.linkMethod(type, name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(Type type, SimpleName name, Type... parameterTypes) {
        return inline(inlines.linkMethod(type, name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target method into this builder.
     * @param type the target method type
     * @param name the target method name
     * @param parameterTypes the target method parameter types
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder linkMethod(Type type, SimpleName name, List<? extends Type> parameterTypes) {
        return inline(inlines.linkMethod(type, name, parameterTypes));
    }

    /**
     * Appends a <code>&#64;link</code> inline block for the target element into this builder.
     * @param element the target element
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocBuilder link(DocElement element) {
        return inline(inlines.link(element));
    }

    private void flushBlock(String nextTag) {
        if (currentTag.length() >= 0 || elements.isEmpty() == false) {
            blocks.add(f.newDocBlock(currentTag, elements));
            elements.clear();
        }
        this.currentTag = nextTag;
    }

    /**
     * A document placeholder.
     * @since 0.9.0
     * @see JavadocBuilder#inline(String, Placeholder...)
     */
    @FunctionalInterface
    public interface Placeholder extends Function<DocElementFactory, DocElement> {
        // no special members
    }

    /**
     * Creates {@link DocElement}.
     * @since 0.9.0
     */
    public static class DocElementFactory {

        private static final Pattern ESCAPE = Pattern.compile("@"); //$NON-NLS-1$

        private final ModelFactory f;

        DocElementFactory(ModelFactory f) {
            this.f = f;
        }

        /**
         * Appends a plain text into this builder.
         * @param text the text
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public DocElement text(Object text) {
            return escape(String.valueOf(text));
        }

        /**
         * Appends a plain text into this builder.
         * @param pattern the text pattern in form of {@link MessageFormat#format(String, Object...)}
         * @param arguments the arguments for the pattern
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public DocElement text(String pattern, Object... arguments) {
            return escape(pattern, arguments);
        }

        /**
         * Appends a <code>&#64;code</code> inline block into this builder.
         * @param text the text
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public DocElement code(Object text) {
            return f.newDocBlock(
                    "@code", //$NON-NLS-1$
                    Collections.singletonList(escape(String.valueOf(text))));
        }

        /**
         * Appends a <code>&#64;code</code> inline block into this builder.
         * @param pattern the text pattern in form of {@link MessageFormat#format(String, Object...)}
         * @param arguments the arguments for the pattern
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public DocElement code(String pattern, Object... arguments) {
            return f.newDocBlock(
                    "@code", //$NON-NLS-1$
                    Collections.singletonList(escape(pattern, arguments)));
        }

        /**
         * Appends a <code>&#64;link</code> inline block for the target type into this builder.
         * @param type the target type
         * @return this
         */
        public DocElement linkType(Type type) {
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
        public DocElement linkField(String name) {
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
        public DocElement linkField(Type type, String name) {
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
        public DocElement linkField(SimpleName name) {
            return linkField(null, name);
        }

        /**
         * Appends a <code>&#64;link</code> inline block for the target field into this builder.
         * @param type the target field type
         * @param name the target field name
         * @return this
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public DocElement linkField(Type type, SimpleName name) {
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
        public DocElement linkMethod(String name, Type... parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
        public DocElement linkMethod(String name, List<? extends Type> parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
        public DocElement linkMethod(SimpleName name, Type... parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
        public DocElement linkMethod(SimpleName name, List<? extends Type> parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
        public DocElement linkMethod(Type type, String name, Type... parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
        public DocElement linkMethod(Type type, String name, List<? extends Type> parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
        public DocElement linkMethod(Type type, SimpleName name, Type... parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
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
        public DocElement linkMethod(Type type, SimpleName name, List<? extends Type> parameterTypes) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameterTypes must not be null"); //$NON-NLS-1$
            }
            List<DocMethodParameter> parameters = new ArrayList<>();
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
        public DocElement link(DocElement element) {
            if (element == null) {
                throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
            }
            return f.newDocBlock(
                    "@link", //$NON-NLS-1$
                    Collections.singletonList(element));
        }

        private DocText escape(String text) {
            String escaped = ESCAPE.matcher(text).replaceAll("&#64;"); //$NON-NLS-1$
            return f.newDocText(escaped);
        }

        private DocText escape(String pattern, Object... arguments) {
            return escape(MessageFormat.format(pattern, arguments));
        }
    }
}
