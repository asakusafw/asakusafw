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
package com.asakusafw.testdriver.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The normal-form of property names.
 * @since 0.2.0
 * @version 0.7.0
 */
public final class PropertyName implements Comparable<PropertyName>, Serializable {

    private static final long serialVersionUID = -362878710044414915L;

    /**
     * The system property key of property name segment separator.
     * @since 0.7.0
     */
    public static final String KEY_SEGMENT_SEPARATOR =
            "com.asakusafw.testdriver.property.segmentSeparator"; //$NON-NLS-1$

    static final String DEFAULT_SEGMENT_SEPARATOR = "-"; //$NON-NLS-1$

    static final String SEGMENT_SEPARATOR;
    static {
        String separator = null;
        try {
            separator = System.getProperty(KEY_SEGMENT_SEPARATOR, DEFAULT_SEGMENT_SEPARATOR);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (separator == null || separator.isEmpty()) {
            separator = DEFAULT_SEGMENT_SEPARATOR;
        }
        SEGMENT_SEPARATOR = separator;
    }

    private final List<String> originalWords;

    private final List<String> normalized;

    /**
     * Creates a new instance.
     * @param words the words which consists this name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    private PropertyName(List<String> words) {
        this.originalWords = words;
        this.normalized = normalize(words);
    }

    private static List<String> normalize(List<String> words) {
        assert words != null;
        List<String> results = new ArrayList<>(words.size());
        Iterator<String> iter = words.iterator();
        assert iter.hasNext();
        String last = iter.next();
        while (iter.hasNext()) {
            String next = iter.next();
            assert next.isEmpty() == false;
            char c = next.charAt(0);
            if ('0' <= c && c <= '9') {
                last += next;
            } else {
                results.add(last);
                last = next;
            }
        }
        results.add(last);
        return Collections.unmodifiableList(results);
    }

    /**
     * Creates a new instance.
     * @param words the words which consists this name
     * @return the created instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static PropertyName newInstance(List<String> words) {
        if (words == null) {
            throw new IllegalArgumentException("words must not be null"); //$NON-NLS-1$
        }
        if (words.isEmpty()) {
            throw new IllegalArgumentException("words must not be empty"); //$NON-NLS-1$
        }
        List<String> work = new ArrayList<>(words.size());
        for (String w : words) {
            work.add(normalize(w));
        }
        return new PropertyName(work);
    }

    /**
     * Creates a new instance.
     * @param words the words which consists this name
     * @return the created instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static PropertyName newInstance(String... words) {
        if (words == null) {
            throw new IllegalArgumentException("words must not be null"); //$NON-NLS-1$
        }
        return newInstance(Arrays.asList(words));
    }

    private static String normalize(String word) {
        if (word == null) {
            throw new IllegalArgumentException("word must not be null"); //$NON-NLS-1$
        }
        return word.toLowerCase();
    }

    /**
     * Returns the words which consists this property name.
     * @return the words in this property name
     */
    public List<String> getWords() {
        return originalWords;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + normalized.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertyName other = (PropertyName) obj;
        if (!normalized.equals(other.normalized)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(PropertyName o) {
        Iterator<String> left = normalized.iterator();
        Iterator<String> right = o.normalized.iterator();
        while (true) {
            if (left.hasNext() == false) {
                if (right.hasNext() == false) {
                    break;
                }
                return -1;
            }
            if (right.hasNext() == false) {
                return +1;
            }
            int diff = left.next().compareTo(right.next());
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        Iterator<String> iter = originalWords.iterator();
        assert iter.hasNext();
        buf.append(iter.next());
        while (iter.hasNext()) {
            buf.append(SEGMENT_SEPARATOR);
            buf.append(iter.next());
        }
        return buf.toString();
    }
}
