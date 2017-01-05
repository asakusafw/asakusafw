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
package com.asakusafw.runtime.flow;

import java.util.List;

/**
 * A List interface for reusing instances of element objects.
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
 * @param <E> the element type
 */
public interface ListBuffer<E> extends List<E> {

    /**
     * Begins changing the list buffer.
     * Initially, the internal cursor is on the head of this buffer, and clients can move it to the next element
     * by invoking {@link #advance()}.
     * After changing the buffer, then clients must invoke {@link #end()} and the buffer can be used as the
     * unmodifiable list.
     * @see #advance()
     * @throws BufferException if failed to prepare buffer
     */
    void begin();

    /**
     * Ends changing the list buffer.
     * After this, clients should not change the buffer contents.
     * If clients want to change the buffer, must invoke {@link #begin()} once more.
     * @throws BufferException if failed to prepare buffer
     */
    void end();

    /**
     * Returns whether a new element object is required for the buffer or not.
     * If it required, clients must use {@link #expand(Object)} to add a new object before invoke {@link #advance()}.
     * This method must be invoked between {@link #begin()} and {@link #end()}.
     * @return {@code true} if a new element object is required, otherwise {@code false}
     * @see #expand(Object)
     * @throws BufferException if failed to prepare buffer
     */
    boolean isExpandRequired();

    /**
     * Adds a new element object into the tail of this buffer.
     * This method must be invoked between {@link #begin()} and {@link #end()}.
     * @param value the object
     * @see #isExpandRequired()
     * @throws IndexOutOfBoundsException if expand is not required (optional operation)
     * @see #isExpandRequired()
     * @throws BufferException if failed to prepare buffer
     */
    void expand(E value);

    /**
     * Returns the next element object on the internal cursor, and then move the cursor to the next element.
     * This method must be invoked between {@link #begin()} and {@link #end()}.
     * @return the next element object
     * @throws IndexOutOfBoundsException if the next element object is not prepared
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
