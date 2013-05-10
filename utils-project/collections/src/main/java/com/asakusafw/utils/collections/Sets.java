/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.utils.collections;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Utilities about {@link Set}.
 */
public final class Sets {

    /**
     * Returns an empty set.
     * The set is modifiable.
     * @param <E> element type
     * @return created set
     */
    public static <E> Set<E> create() {
        return new HashSet<E>();
    }

    /**
     * Returns a set which consists of the specified element.
     * The set is modifiable.
     * @param <E> element type
     * @param elem the element
     * @return created set
     */
    public static <E> Set<E> of(E elem) {
        HashSet<E> result = new HashSet<E>();
        result.add(elem);
        return result;
    }

    /**
     * Returns a set which consists of the specified elements.
     * The set is modifiable.
     * @param <E> element type
     * @param elem1 the first element
     * @param elem2 the second element
     * @return created set
     */
    public static <E> Set<E> of(E elem1, E elem2) {
        HashSet<E> result = new HashSet<E>();
        result.add(elem1);
        result.add(elem2);
        return result;
    }

    /**
     * Returns a set which consists of the specified elements.
     * The set is modifiable.
     * @param <E> element type
     * @param elem1 the first element
     * @param elem2 the second element
     * @param elem3 the third element
     * @return created set
     */
    public static <E> Set<E> of(E elem1, E elem2, E elem3) {
        HashSet<E> result = new HashSet<E>();
        result.add(elem1);
        result.add(elem2);
        result.add(elem3);
        return result;
    }

    /**
     * Returns a set which consists of the specified elements.
     * The set is modifiable.
     * @param <E> element type
     * @param elem1 the first element
     * @param elem2 the second element
     * @param elem3 the third element
     * @param elem4 the fourth element
     * @param rest the rest elements
     * @return created set
     */
    public static <E> Set<E> of(E elem1, E elem2, E elem3, E elem4, E... rest) {
        if (rest == null) {
            throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
        }
        HashSet<E> result = new HashSet<E>();
        result.add(elem1);
        result.add(elem2);
        result.add(elem3);
        result.add(elem4);
        Collections.addAll(result, rest);
        return result;
    }

    /**
     * Returns a set which consists of the specified elements.
     * The set is modifiable.
     * @param <E> element type
     * @param elements the elements
     * @return created set
     */
    public static <E> Set<E> from(E[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        HashSet<E> result = new HashSet<E>();
        Collections.addAll(result, elements);
        return result;
    }

    /**
     * Returns a set which has copy of the elements.
     * The set is modifiable.
     * @param <E> element type
     * @param elements the elements
     * @return created set
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <E> Set<E> from(Iterable<? extends E> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        HashSet<E> copy = new HashSet<E>();
        for (E element : elements) {
            copy.add(element);
        }
        return copy;
    }

    /**
     * Returns a freezed set which has copy of the elements.
     * @param <E> element type
     * @param elements the elements
     * @return created set
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <E> Set<E> freeze(Iterable<? extends E> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        Iterator<? extends E> iter = elements.iterator();
        if (iter.hasNext() == false) {
            return Collections.emptySet();
        }
        E first = iter.next();
        if (iter.hasNext() == false) {
            return Collections.singleton(first);
        }
        HashSet<E> copy = new HashSet<E>();
        copy.add(first);
        while (iter.hasNext()) {
            copy.add(iter.next());
        }
        return Collections.unmodifiableSet(copy);
    }

    /**
     * Returns a freezed set which has copy of the elements.
     * @param <E> element type
     * @param elements the elements
     * @return created set
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <E> Set<E> freeze(E[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        return Collections.unmodifiableSet(from(elements));
    }

    private Sets() {
        return;
    }
}
