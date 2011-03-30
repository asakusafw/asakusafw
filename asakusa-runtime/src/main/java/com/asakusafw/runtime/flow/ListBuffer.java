/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * 変更可能な要素を再利用しながらリストの機能を提供するクラス。
 * <p>
 * 以下のように利用する。
 * </p>
<pre><code>
Iterator&lt;Composite&gt; iter = ...;

ListBuffer&lt;Hoge&gt; hoges = new ListBuffer&lt;Hoge&gt;();
ListBuffer&lt;Foo&gt; foos = new ListBuffer&lt;Foo&gt;();
...

hoges.begin();
foos.begin();
while (iter.hasNext()) {
    Composite composite = iter.next();
    if (composite.isHoge()) {
        if (hoges.isExpandRequired()) {
            hoges.expand(new Hoge());
        }
        hoges.advance().set(composite.getHoge());
    } else if (composite.isFoo()) {
        if (foos.isExpandRequired()) {
            foos.expand(new Hoge());
        }
        foos.advance().set(composite.getFoo());
    } else ...
}
hoges.end();
foos.end();
</code></pre>
 * @param <E> 要素の型
 */
public class ListBuffer<E> extends AbstractList<E> implements RandomAccess {

    private static final int BUFFER_SIZE = 64;

    private Object[] buffer;

    private int size;

    private int cursor;

    private int limit;

    /**
     * インスタンスを生成する。
     */
    public ListBuffer() {
        this.buffer = new Object[BUFFER_SIZE];
        this.size = 0;
        this.cursor = -1;
        this.limit = 0;
    }

    /**
     * バッファの変更を開始する。
     * <p>
     * 内部カーソルはバッファの先頭を指し、{@link #advance()}を実行した際に
     * 先頭から順にバッファの内容を返す。
     * </p>
     * <p>
     * これによって{@link #end()}が起動されるまで、
     * バッファの内容を読み出した際の動作を保証しない。
     * </p>
     * @see #advance()
     */
    public void begin() {
        size = -1;
        cursor = 0;
        modCount++;
    }

    /**
     * バッファの変更を終了する。
     * <p>
     * 以後、このリストは{@link #begin()}から{@link #advance()}を起動した
     * 回数と同じだけの要素を持つことになる。
     * </p>
     */
    public void end() {
        if (cursor >= 0) {
            size = cursor;
            cursor = -1;
            modCount++;
        }
    }

    /**
     * {@link #begin()}から{@link #end()}の期間に起動され、
     * 直前の{@link #begin()}以降に{@link #advance()}を起動した回数を返す。
     * <p>
     * 内部カーソルの位置は次に{@link #advance()}メソッドがオブジェクトを
     * 返すリスト内の位置を表している。
     * 例えば、{@link #begin()}の直後にこのメソッドを起動した場合には{@code 0}が返される。
     * </p>
     * <p>
     * なお、{@link #begin()}から{@link #end()}の期間の外で起動された場合、
     * このメソッドの動作は保証されない。
     * </p>
     * @return 直前の{@link #begin()}以降に{@link #advance()}を起動した回数
     */
    public int getCursorPosition() {
        return cursor;
    }

    /**
     * {@link #begin()}から{@link #end()}の期間に起動され、
     * 処理中の内部カーソルがバッファサイズを超えた際に{@code true}を返す。
     * <p>
     * このメソッドが{@code true}を返す場合、{@link #advance()}は
     * {@link IndexOutOfBoundsException}をスローする。
     * {@link #expand(Object)}でバッファに要素を追加していやることで、
     * 以後正しく{@link #advance()}メソッドを実行できる。
     * </p>
     * <p>
     * なお、{@link #begin()}から{@link #end()}の期間の外で起動された場合、
     * このメソッドの動作は保証されない。
     * </p>
     * @return 次の{@link #advance()}がバッファ内の要素を返せない場合に{@code true}
     * @see #expand(Object)
     */
    public boolean isExpandRequired() {
        return limit <= cursor;
    }

    /**
     * バッファの末尾に新しい要素を追加する。
     * @param value 追加する要素
     * @see #isExpandRequired()
     */
    public void expand(E value) {
        expandBuffer(buffer.length << 2);
        buffer[limit++] = value;
    }

    /**
     * {@link #begin()}から{@link #end()}の期間に起動され、
     * 処理中の内部カーソルの位置にあるバッファの内容を返す。
     * <p>
     * この処理によって、カーソルは次の要素を指すように変更される。
     * </p>
     * <p>
     * なお、{@link #begin()}から{@link #end()}の期間の外で起動された場合、
     * このメソッドの動作は保証されない。
     * </p>
     * @return 内部カーソルの位置らにあるバッファの内容
     * @throws IndexOutOfBoundsException バッファの容量を超えて要素を参照しようとした場合
     * @see #isExpandRequired()
     * @see #expand(Object)
     */
    public E advance() {
        @SuppressWarnings("unchecked")
        E next = (E) buffer[cursor];
        cursor++;
        return next;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return (E) buffer[index];
    }

    private void expandBuffer(int newLength) {
        if (buffer.length <= limit) {
            Object[] newBuffer = new Object[newLength];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }
}
