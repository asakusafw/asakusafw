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
package com.asakusafw.runtime.flow;

import java.util.List;

/**
 * 変更可能な要素を再利用しながらリストの機能を提供する。
 * <p>
 * 以下のように利用する。
 * </p>
<pre><code>
Iterator&lt;Hoge&gt; iter = ...;
ListBuffer&lt;Hoge&gt; hoges = ...;
...

hoges.begin();
while (iter.hasNext()) {
    Hoge hoge = iter.next();
    if (hoges.isExpandRequired()) {
        hoges.expand(new Hoge());
    }
    hoges.advance().set(hoge);
}
hoges.end();

// use hoges as List

hoges.shrink();
</code></pre>
 * @param <E> 要素の型
 */
public interface ListBuffer<E> extends List<E> {

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
     * @throws BufferException if failed to prepare buffer
     */
    void begin();

    /**
     * バッファの変更を終了する。
     * <p>
     * 以後、このリストは{@link #begin()}から{@link #advance()}を起動した
     * 回数と同じだけの要素を持つことになる。
     * </p>
     * @throws BufferException if failed to prepare buffer
     */
    void end();

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
     * @throws BufferException if failed to prepare buffer
     */
    boolean isExpandRequired();

    /**
     * バッファの末尾に新しい要素を追加する。
     * @param value 追加する要素
     * @see #isExpandRequired()
     * @throws IndexOutOfBoundsException if expand is not required (Optional)
     * @see #isExpandRequired()
     * @throws BufferException if failed to prepare buffer
     */
    void expand(E value);

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
     * @throws BufferException if failed to prepare buffer
     */
    E advance();

    /**
     * Shrinks this buffer.
     */
    void shrink();
}
