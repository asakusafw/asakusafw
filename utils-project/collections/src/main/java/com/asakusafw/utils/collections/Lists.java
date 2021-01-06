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
package com.asakusafw.utils.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities about {@link List}.
 */
public final class Lists {

    /**
     * Returns an empty list.
     * The list is modifiable.
     * @param <E> element type
     * @return created list
     */
    public static <E> List<E> create() {
        return new ArrayList<>();
    }

    /**
     * Returns a list which consists of the specified element.
     * The list is modifiable.
     * @param <E> element type
     * @param elem the element
     * @return created list
     */
    public static <E> List<E> of(E elem) {
        ArrayList<E> result = new ArrayList<>();
        result.add(elem);
        return result;
    }

    /**
     * Returns a list which consists of the specified elements.
     * The list is modifiable.
     * @param <E> element type
     * @param elem1 the first element
     * @param elem2 the second element
     * @return created list
     */
    public static <E> List<E> of(E elem1, E elem2) {
        ArrayList<E> result = new ArrayList<>();
        result.add(elem1);
        result.add(elem2);
        return result;
    }

    /**
     * Returns a list which consists of the specified elements.
     * The list is modifiable.
     * @param <E> element type
     * @param elem1 the first element
     * @param elem2 the second element
     * @param elem3 the third element
     * @return created list
     */
    public static <E> List<E> of(E elem1, E elem2, E elem3) {
        ArrayList<E> result = new ArrayList<>();
        result.add(elem1);
        result.add(elem2);
        result.add(elem3);
        return result;
    }

    /**
     * Returns a list which consists of the specified elements.
     * The list is modifiable.
     * @param <E> element type
     * @param elem1 the first element
     * @param elem2 the second element
     * @param elem3 the third element
     * @param elem4 the fourth element
     * @param rest the rest elements
     * @return created list
     */
    @SafeVarargs
    public static <E> List<E> of(E elem1, E elem2, E elem3, E elem4, E... rest) {
        if (rest == null) {
            throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
        }
        ArrayList<E> result = new ArrayList<>();
        result.add(elem1);
        result.add(elem2);
        result.add(elem3);
        result.add(elem4);
        Collections.addAll(result, rest);
        return result;
    }

    /**
     * Returns a list which consists of the specified elements.
     * The list is modifiable.
     * @param <E> element type
     * @param elements the elements
     * @return created list
     */
    public static <E> List<E> from(E[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        ArrayList<E> result = new ArrayList<>();
        Collections.addAll(result, elements);
        return result;
    }

    /**
     * Returns a list which has copy of the elements.
     * The list is modifiable.
     * @param <E> element type
     * @param elements the elements
     * @return created list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <E> List<E> from(Iterable<? extends E> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        ArrayList<E> copy = new ArrayList<>();
        for (E element : elements) {
            copy.add(element);
        }
        return copy;
    }

    /**
     * Returns a frozen list which has copy of the elements.
     * @param <E> element type
     * @param elements the elements
     * @return created list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <E> List<E> freeze(Iterable<? extends E> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        Iterator<? extends E> iter = elements.iterator();
        if (iter.hasNext() == false) {
            return Collections.emptyList();
        }
        E first = iter.next();
        if (iter.hasNext() == false) {
            return Collections.singletonList(first);
        }
        ArrayList<E> copy = new ArrayList<>();
        copy.add(first);
        while (iter.hasNext()) {
            copy.add(iter.next());
        }
        copy.trimToSize();
        return Collections.unmodifiableList(copy);
    }

    /**
     * Returns a frozen list which has copy of the elements.
     * @param <E> element type
     * @param elements the elements
     * @return created list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <E> List<E> freeze(E[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        E[] copy = elements.clone();
        return Collections.unmodifiableList(Arrays.asList(copy));
    }

    private Lists() {
        return;
    }
}
