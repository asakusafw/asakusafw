/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.util;

import java.util.List;

import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.utils.collections.Lists;

/**
 * Manipulates names used in Java.
 */
public class JavaName {

    private final List<String> words;

    JavaName(List<? extends String> words) {
        if (words == null) {
            throw new NullPointerException("words"); //$NON-NLS-1$
        }
        if (words.isEmpty()) {
            throw new IllegalArgumentException("words"); //$NON-NLS-1$
        }
        this.words = Lists.create();
        for (String word : words) {
            this.words.add(normalize(word));
        }
    }

    /**
     * Creates a new object from a DMDL simple name.
     * @param name the target name
     * @return the created object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static JavaName of(AstSimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return new JavaName(normalize(name.getWordList()));
    }

    /**
     * Returns the {@code CamelCase} of this name.
     * @return the converted name
     */
    public String toTypeName() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = words.size(); i < n; i++) {
            buf.append(capitalize(words.get(i)));
        }
        return buf.toString();
    }

    /**
     * Returns the {@code camelCase} of this name.
     * @return the converted name
     */
    public String toMemberName() {
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0).toLowerCase());
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append(capitalize(words.get(i)));
        }
        return buf.toString();
    }

    /**
     * Returns the {@code UPPER_CASE} of this name.
     * @return the converted name
     */
    public String toConstantName() {
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0).toUpperCase());
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append('_');
            buf.append(words.get(i).toUpperCase());
        }
        return buf.toString();
    }

    /**
     * Adds the name segment to the head of this.
     * @param segment the segment to add
     * @throws IllegalArgumentException if the segment is invalid
     */
    public void addFirst(String segment) {
        words.add(0, normalize(segment));
    }

    /**
     * Adds the name segment to the tail of this.
     * @param segment the segment to add
     * @throws IllegalArgumentException if the segment is invalid
     */
    public void addLast(String segment) {
        words.add(normalize(segment));
    }

    /**
     * Adds other name to the tail of this.
     * @param other name to add
     * @return this object (for method chain)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JavaName append(JavaName other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
        }
        words.addAll(other.words);
        return this;
    }

    private String capitalize(String segment) {
        assert segment != null;
        StringBuilder buf = new StringBuilder(segment.toLowerCase());
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        return buf.toString();
    }

    private static String normalize(String segment) {
        if (segment == null) {
            throw new IllegalArgumentException("segment must not be null"); //$NON-NLS-1$
        }
        if (segment.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return segment.toLowerCase();
    }

    private static List<String> normalize(List<String> segments) {
        List<String> results = Lists.create();
        for (String segment : segments) {
            if (segment.isEmpty() == false) {
                results.add(segment);
            }
        }
        return results;
    }
}
