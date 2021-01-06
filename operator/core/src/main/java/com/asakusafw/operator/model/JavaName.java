/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.operator.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a simple name in Java.
 */
public class JavaName {

    private static final Set<String> RESERVED;
    static {
        // see http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.9
        Set<String> set = new HashSet<>();
        set.add("abstract"); //$NON-NLS-1$
        set.add("continue"); //$NON-NLS-1$
        set.add("for"); //$NON-NLS-1$
        set.add("new"); //$NON-NLS-1$
        set.add("switch"); //$NON-NLS-1$
        set.add("assert"); //$NON-NLS-1$
        set.add("default"); //$NON-NLS-1$
        set.add("if"); //$NON-NLS-1$
        set.add("package"); //$NON-NLS-1$
        set.add("synchronized"); //$NON-NLS-1$
        set.add("boolean"); //$NON-NLS-1$
        set.add("do"); //$NON-NLS-1$
        set.add("goto"); //$NON-NLS-1$
        set.add("private"); //$NON-NLS-1$
        set.add("this"); //$NON-NLS-1$
        set.add("break"); //$NON-NLS-1$
        set.add("double"); //$NON-NLS-1$
        set.add("implements"); //$NON-NLS-1$
        set.add("protected"); //$NON-NLS-1$
        set.add("throw"); //$NON-NLS-1$
        set.add("byte"); //$NON-NLS-1$
        set.add("else"); //$NON-NLS-1$
        set.add("import"); //$NON-NLS-1$
        set.add("public"); //$NON-NLS-1$
        set.add("throws"); //$NON-NLS-1$
        set.add("case"); //$NON-NLS-1$
        set.add("enum"); //$NON-NLS-1$
        set.add("instanceof"); //$NON-NLS-1$
        set.add("return"); //$NON-NLS-1$
        set.add("transient"); //$NON-NLS-1$
        set.add("catch"); //$NON-NLS-1$
        set.add("extends"); //$NON-NLS-1$
        set.add("int"); //$NON-NLS-1$
        set.add("short"); //$NON-NLS-1$
        set.add("try"); //$NON-NLS-1$
        set.add("char"); //$NON-NLS-1$
        set.add("final"); //$NON-NLS-1$
        set.add("interface"); //$NON-NLS-1$
        set.add("static"); //$NON-NLS-1$
        set.add("void"); //$NON-NLS-1$
        set.add("class"); //$NON-NLS-1$
        set.add("finally"); //$NON-NLS-1$
        set.add("long"); //$NON-NLS-1$
        set.add("strictfp"); //$NON-NLS-1$
        set.add("volatile"); //$NON-NLS-1$
        set.add("const"); //$NON-NLS-1$
        set.add("float"); //$NON-NLS-1$
        set.add("native"); //$NON-NLS-1$
        set.add("super"); //$NON-NLS-1$
        set.add("while"); //$NON-NLS-1$
        RESERVED = Collections.unmodifiableSet(set);
    }

    private static final String EMPTY_NAME = "_"; //$NON-NLS-1$

    private final List<String> words;

    JavaName(List<String> words) {
        this.words = normalize(Objects.requireNonNull(words, "words must not be null")); //$NON-NLS-1$
    }

    /**
     * Returns a Java name represented in the specified string.
     * @param nameString a string which represents a Java name
     * @return related object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static JavaName of(String nameString) {
        Objects.requireNonNull(nameString, "nameString must not be null"); //$NON-NLS-1$
        if (nameString.isEmpty()) {
            throw new IllegalArgumentException("nameString must not be empty"); //$NON-NLS-1$
        } else if (nameString.indexOf('_') >= 0 || nameString.toUpperCase(Locale.ENGLISH).equals(nameString)) {
            String[] segments = nameString.split(EMPTY_NAME);
            return new JavaName(Arrays.asList(segments));
        } else {
            List<String> segments = new ArrayList<>();
            int start = 0;
            for (int i = 1, n = nameString.length(); i < n; i++) {
                if (Character.isUpperCase(nameString.charAt(i))) {
                    segments.add(nameString.substring(start, i));
                    start = i;
                }
            }
            segments.add(nameString.substring(start));
            return new JavaName(segments);
        }
    }

    /**
     * Returns name segments as lower case characters.
     * @return name segments
     */
    public List<String> getSegments() {
        return words;
    }

    /**
     * Returns this name in form of type names ({@code CamelCase}).
     * @return CamelCase name
     */
    public String toTypeName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = words.size(); i < n; i++) {
            buf.append(capitalize(words.get(i)));
        }
        return buf.toString();
    }

    /**
     * Returns this name in form of member names ({@code camelCase}).
     * @return camelCase name
     */
    public String toMemberName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0));
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append(capitalize(words.get(i)));
        }
        String result = buf.toString();
        if (RESERVED.contains(result) || Character.isJavaIdentifierStart(result.charAt(0)) == false) {
            return escape(result);
        }
        return result;
    }

    private String escape(String result) {
        assert result != null;
        return result + '_';
    }

    /**
     * Returns this name in form of constant names ({@code UPPER_CASE}).
     * @return UPPER_CASE name
     */
    public String toConstantName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
        return toSnakeName().toUpperCase(Locale.ENGLISH);
    }

    /**
     * Returns this name in form of constant names ({@code snake_case}).
     * @return snake_case name
     */
    public String toSnakeName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0));
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append('_');
            buf.append(words.get(i));
        }
        return buf.toString();
    }

    /**
     * Inserts a name segment into beginning of this name.
     * @param segment the name segment
     * @throws IllegalArgumentException if the name segment is not valid
     */
    public void addFirst(String segment) {
        words.add(0, normalize(segment));
    }

    /**
     * Removes the first name segment in this name.
     * @throws IllegalStateException if this name is empty
     */
    public void removeFirst() {
        if (words.isEmpty()) {
            throw new IllegalStateException();
        }
        words.remove(0);
    }

    /**
     * Inserts a name segment into ending of this name.
     * @param segment the name segment
     * @throws IllegalArgumentException if the name segment is not valid
     */
    public void addLast(String segment) {
        words.add(normalize(segment));
    }

    /**
     * Removes the last name segment in this name.
     * @throws IllegalStateException if this name is empty
     */
    public void removeLast() {
        if (words.isEmpty()) {
            throw new IllegalStateException();
        }
        words.remove(words.size() - 1);
    }

    private String capitalize(String segment) {
        assert segment != null;
        StringBuilder buf = new StringBuilder(segment);
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        return buf.toString();
    }

    private static String normalize(String segment) {
        Objects.requireNonNull(segment, "segment must not be null"); //$NON-NLS-1$
        if (segment.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return segment.toLowerCase(Locale.ENGLISH);
    }

    private static List<String> normalize(List<String> segments) {
        assert segments != null;
        List<String> results = new ArrayList<>();
        for (String segment : segments) {
            if (segment.isEmpty() == false) {
                results.add(normalize(segment));
            }
        }
        return results;
    }
}
