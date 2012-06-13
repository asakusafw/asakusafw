/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
 * 変更不可能なリンクリスト。
 * @param <E> 保持する要素の型
 */
public class SingleLinkedList<E> implements Iterable<E>, Serializable {

    private static final long serialVersionUID = 1L;

    private transient Node<E> head;

    /**
     * 要素を含まないリストを作成して返す。
     */
    public SingleLinkedList() {
        head = null;
    }

    /**
     * 指定のリストのコピーを返す。
     * @param list コピー元のリスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public SingleLinkedList(List<? extends E> list) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null"); //$NON-NLS-1$
        }
        head = restoreFromList(list);
    }

    /**
     * 指定のリストのコピーを返す。
     * @param list コピー元のリスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public SingleLinkedList(SingleLinkedList<? extends E> list) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null"); //$NON-NLS-1$
        }
        head = restoreFromList(list.fill(new ArrayList<E>()));
    }

    /**
     * 指定のコレクションのコピーを返す。
     * @param collection コピー元のコレクション
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public SingleLinkedList(Collection<? extends E> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection must not be null"); //$NON-NLS-1$
        }
        head = restoreFromList(new ArrayList<E>(collection));
    }

    private SingleLinkedList(Node<E> head) {
        this.head = head;
    }

    /**
     * このリストが空のリストである場合のみ{@code null}を返す。
     * @return 空のリストである場合のみ{@code null}
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * このリストに含まれる要素数を返す。
     * @return このリストに含まれる要素数
     */
    public int size() {
        int size = 0;
        for (Node<E> node = head; node != null; node = node.next) {
            size++;
        }
        return size;
    }

    /**
     * このリストの先頭に指定の値を追加した新しいリストを返す。
     * この操作によって、このリストの状態は変更されない。
     * @param value 追加する値
     * @return 先頭に指定の値を追加した新しいリスト
     */
    public SingleLinkedList<E> concat(E value) {
        Node<E> next = new Node<E>(value, head);
        return new SingleLinkedList<E>(next);
    }

    /**
     * このリストの先頭の要素を返す。
     * @return 先頭の要素
     * @throws NoSuchElementException このリストが空であった場合
     */
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.value;
    }

    /**
     * このリストの先頭の要素を返す。
     * @return 先頭の要素
     * @throws NoSuchElementException このリストが空であった場合
     */
    public SingleLinkedList<E> rest() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return new SingleLinkedList<E>(head.next);
    }

    /**
     * このリストの{@code index}番目の要素を返す。
     * {@code index}は0以上で指定し、0が指定された場合に最初の要素を返す。
     * @param index 対象の要素番号
     * @return {@code index}番目の要素
     * @throws IndexOutOfBoundsException 範囲外の要素番号を指定した場合
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
        return new NodeIterator<E>(head);
    }

    /**
     * この要素の内容を指定のコレクションにコピーして返す。
     * @param <C> コレクションの型
     * @param target 追加先のコレクション
     * @return 引数に渡されたコレクション
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
            }
            else if (thatNode == null) {
                return false;
            }
            else {
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
        }
        else {
            List<E> store = new ArrayList<E>(size);
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
        for (ListIterator<? extends E> iter = store.listIterator(store.size());
                iter.hasPrevious();) {
            E value = iter.previous();
            Node<E> prev = new Node<E>(value, node);
            node = prev;
        }
        return node;
    }
}
