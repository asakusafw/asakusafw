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
package com.asakusafw.utils.java.parser.javadoc;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder for building {@link JavadocParser}.
 */
public class JavadocParserBuilder {

    private boolean generated;
    private final List<JavadocBlockParser> inlines;
    private final List<JavadocBlockParser> toplevels;

    /**
     * Creates a new instance.
     */
    public JavadocParserBuilder() {
        this.generated = false;
        this.inlines = new ArrayList<>();
        this.toplevels = new ArrayList<>();
    }

    /**
     * Adds an inline block parser.
     * @param parser the parser
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws IllegalStateException if {@link #build()} has already been invoked
     */
    public synchronized void addSpecialInlineBlockParser(JavadocBlockParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser"); //$NON-NLS-1$
        }
        if (generated) {
            throw new IllegalStateException();
        }
        inlines.add(parser);
    }

    /**
     * Adds a top-level block parser.
     * @param parser the parser
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws IllegalStateException if {@link #build()} has already been invoked
     */
    public synchronized void addSpecialStandAloneBlockParser(JavadocBlockParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser"); //$NON-NLS-1$
        }
        if (generated) {
            throw new IllegalStateException();
        }
        toplevels.add(parser);
    }

    /**
     * Builds a new {@link JavadocParser}.
     * @return the built object
     * @throws IllegalStateException if {@link #build()} has already been invoked
     */
    public synchronized JavadocParser build() {
        if (generated) {
            throw new IllegalStateException();
        }
        generated = true;

        // enable generic inline blocks
        inlines.add(new DefaultJavadocBlockParser());

        for (JavadocBlockParser p: toplevels) {
            p.setBlockParsers(inlines);
        }

        // enable generic top-level blocks
        toplevels.add(new DefaultJavadocBlockParser(inlines));

        JavadocParser parser = new JavadocParser(toplevels);
        return parser;
    }
}
