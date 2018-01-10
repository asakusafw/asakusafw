/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.GroupSort;

/**
 * An annotation represents which the annotated element can spill its contents into backing store.
 *
 * This can appear with the following elements:
 * <ul>
 * <li>
 *   Input parameter of {@link CoGroup} and {@link GroupSort} operator methods.
 *
 *   A parameter with this annotation may have too many elements in its list.
 *   In such the case, we spill the elements into a temporary file, and flush the elements from the Java heap.
 *
 *   With this annotation, obtaining elements from the sequence will change the previously obtained object.
 *   For example, the operations are not guaranteed in the following case:
<pre><code>
&#64;CoGroup
public void invalid(&#64;Key(...) &#64;Spill List&lt;Hoge&gt; input, Result&lt;Hoge&gt; result) {
    Hoge a = input.get(0);
    Hoge b = input.get(1); // this operation may break out contents of 'a'
    ...
}
</code></pre>
 *   In such the case, application developers should create a copy of the object:
<pre><code>
final Hoge a = new Hoge();
final Hoge b = new Hoge();

&#64;CoGroup
public void invalid(&#64;Key(...) &#64;Spill List&lt;Hoge&gt; input, Result&lt;Hoge&gt; result) {
    a.copyFrom(input.get(0)); // take a copy
    b.copyFrom(input.get(1));
    ...
}
</code></pre>
 * </li>
 * </ul>
 *
 * @see Once
 * @since 0.9.1
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Spill {
    // no special members
}
