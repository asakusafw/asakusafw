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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Unmodifiable linked list.
 * @param <E> element type
 */
public class SingleLinkedList<E> implements Iterable<E>, Serializable {

    private static final long serialVersionUID = 1L;

    private transient Node<E> head;

    /**
     * Creates a new instance without elements.
     */
    public SingleLinkedList() {
        head = null;
    }

    /**
     * Creates a new instance from {@link List}.
     * @param list target list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SingleLinkedList(List<? extends E> list) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null"); //$NON-NLS-1$
        }
        head = restoreFromList(list);
    }

    /**
     * Creates a new instance from {@link Iterable}.
     * @param iterable target iterable
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SingleLinkedList(Iterable<? extends E> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException("iterable must not be null"); //$NON-NLS-1$
        }
        head = restoreFromList(Lists.from(iterable));
    }

    private SingleLinkedList(Node<E> head) {
        this.head = head;
    }

    /**
     * Returns whether this list has no elements.
     * @return {@code true} if this list is empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Returns number of elements in this list.
     * @return number of elements in this list
     */
    public int size() {
        int size = 0;
        for (Node<E> node = head; node != null; node = node.next) {
            size++;
        }
        return size;
    }

    /**
     * Creates and returns a new list with the value in head of this list.
     * This operation does not modify this list.
     * @param element target element
     * @return the created list
     */
    public SingleLinkedList<E> concat(E element) {
        Node<E> next = new Node<>(element, head);
        return new SingleLinkedList<>(next);
    }

    /**
     * Returns the first element in this list.
     * @return the first element
     * @throws NoSuchElementException if this list is empty
     */
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.value;
    }

    /**
     * Returns a new list dropping the first element from this list.
     * @return the rest of list
     * @throws NoSuchElementException if this list is empty
     */
    public SingleLinkedList<E> rest() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return new SingleLinkedList<>(head.next);
    }

    /**
     * Returns the {@code index}-th element in this list.
     * @param index target element index (0-origin)
     * @return {@code index}-th element
     * @throws IndexOutOfBoundsException if index is out of bound
     */
    public E get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        Node<E> node = head;
        for (int i = 0; i < index && node != null; i++) {
            node = node.next;
        }
        if (node == null) {
            throw new IndexOutOfBoundsException();
        }
        return node.value;
    }

    @Override
    public Iterator<E> iterator() {
        return new NodeIterator<>(head);
    }

    /**
     * Fills elements in this list into the target {@link Collection}.
     * @param <C> collection type
     * @param target the target collection
     * @return same as {@code target}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <C extends Collection<? super E>> C fill(C target) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        for (Node<E> node = head; node != null; node = node.next) {
            target.add(node.value);
        }
        return target;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (Node<E> node = head; node != null; node = node.next) {
            result = prime * result + (node.value == null ? 0 : node.value.hashCode());
        }
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
        SingleLinkedList<?> other = (SingleLinkedList<?>) obj;
        Node<E> thisNode = head;
        Node<?> thatNode = other.head;
        while (true) {
            if (thisNode == null) {
                return thatNode == null;
            } else if (thatNode == null) {
                return false;
            } else {
                if (thisNode.value == null) {
                    return thatNode.value == null;
                }
                if (thisNode.value.equals(thatNode.value) == false) {
                    return false;
                }
                thisNode = thisNode.next;
                thatNode = thatNode.next;
            }
        }
    }

    @Override
    public String toString() {
        return fill(new ArrayList<E>()).toString();
    }

    private static final class Node<E> {

        E value;

        Node<E> next;

        Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }

    private static final class NodeIterator<E> implements Iterator<E> {

        private Node<E> current;

        NodeIterator(Node<E> node) {
            this.current = node;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            E value = current.value;
            current = current.next;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(size());
        for (Node<E> node = head; node != null; node = node.next) {
            stream.writeObject(node.value);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        int size = stream.readInt();
        if (size == 0) {
            head = null;
        } else {
            List<E> store = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                E value = (E) stream.readObject();
                store.add(value);
            }
            Node<E> node = restoreFromList(store);
            head = node;
        }
    }

    private Node<E> restoreFromList(List<? extends E> store) {
        Node<E> node = null;
        for (ListIterator<? extends E> iter = store.listIterator(store.size()); iter.hasPrevious();) {
            E value = iter.previous();
            Node<E> prev = new Node<>(value, node);
            node = prev;
        }
        return node;
    }
}
