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
package com.asakusafw.compiler.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.utils.collections.Lists;

/**
 * Naming rules for Java source files.
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

    JavaName(List<? extends String> words) {
        if (words == null) {
            throw new NullPointerException("words"); //$NON-NLS-1$
        }
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(normalize(word));
        }
    }

    /**
     * Parses a name string.
     * @param nameString the target string
     * @return the created object
     */
    public static JavaName of(String nameString) {
        if (nameString.isEmpty()) {
            throw new IllegalArgumentException("nameString must not be empty"); //$NON-NLS-1$
        } else if (nameString.indexOf('_') >= 0 || nameString.toUpperCase().equals(nameString)) {
            String[] segments = nameString.split(EMPTY_NAME);
            return new JavaName(normalize(Arrays.asList(segments)));
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
            return new JavaName(normalize(segments));
        }
    }

    /**
     * Returns words of this name as lower case string.
     * @return a segments in this name
     */
    public List<String> getSegments() {
        return Lists.from(words);
    }

    /**
     * Returns this name as upper CamelCase.
     * @return upper CamelCase name - as a Java type name
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
     * Returns this name as lower camelCase.
     * @return lower camelCase name - as a Java member name
     */
    public String toMemberName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0).toLowerCase());
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
     * Returns this name as UPPER_CASE.
     * @return UPPER_CASE name - as a Java constant name
     */
    public String toConstantName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0).toUpperCase());
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append('_');
            buf.append(words.get(i).toUpperCase());
        }
        return buf.toString();
    }

    /**
     * Adds a word into the head of this name.
     * @param segment the word
     * @throws IllegalArgumentException if the word is not valid
     */
    public void addFirst(String segment) {
        words.add(0, normalize(segment));
    }

    /**
     * Removes the first word of this name.
     * @throws IllegalStateException if the name is empty
     */
    public void removeFirst() {
        if (words.isEmpty()) {
            throw new IllegalStateException();
        }
        words.remove(0);
    }

    /**
     * Adds a word into the tail of this name.
     * @param segment the word
     * @throws IllegalArgumentException if the word is not valid
     */
    public void addLast(String segment) {
        words.add(normalize(segment));
    }

    /**
     * Removes the last word of this name.
     * @throws IllegalStateException if the name is empty
     */
    public void removeLast() {
        if (words.isEmpty()) {
            throw new IllegalStateException();
        }
        words.remove(words.size() - 1);
    }

    private String capitalize(String segment) {
        assert segment != null;
        StringBuilder buf = new StringBuilder(segment.toLowerCase());
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        return buf.toString();
    }

    private static String normalize(String segment) {
        Precondition.checkMustNotBeNull(segment, "segment"); //$NON-NLS-1$
        if (segment.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return segment.toLowerCase();
    }

    private static List<String> normalize(List<String> segments) {
        List<String> results = new ArrayList<>();
        for (String segment : segments) {
            if (segment.isEmpty() == false) {
                results.add(segment);
            }
        }
        return results;
    }
}
