/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * An annotation represents which the annotated element will be used only once.
 *
 * This can appear with the following elements:
 * <ul>
 * <li>
 *   Input parameter of {@link CoGroup} and {@link GroupSort} operator methods.
 *
 *   A parameter with this annotation represents that elements in the parameter must be accessed only once.
 *   The parameter must be defined as {@code Iterable} type, and clients can invoke
 *   {@link Iterable#iterator() its iterator()} only once in the operator method.
 *   If clients invoke the method more than once, it MAY raise an exception.
 *
 *   Typically, the annotated parameter is used with {@code for} statement like as following:
<pre><code>
&#64;CoGroup
public void iterate(&#64;Key(...) &#64;Once Iterable&lt;Hoge&gt; input, Result&lt;Hoge&gt; result) {
    for (Hoge hoge : input) {
        ...
    }
}
</code></pre>
 *
 *   With this annotation, obtaining elements in the sequence will change the old object from the sequence.
 *   For example, the operations are not guaranteed in the following case:
<pre><code>
&#64;CoGroup
public void invalid(&#64;Key(...) &#64;Once Iterable&lt;Hoge&gt; input, Result&lt;Hoge&gt; result) {
    Iterator&lt;Hoge&gt; iter = input.iterator();
    Hoge a = iter.next();
    Hoge b = iter.next(); // this operation may break out contents of 'a'
    ...
}
</code></pre>
 *   In such the case, application developers should create a copy of the object:
<pre><code>
final Hoge a = new Hoge();
final Hoge b = new Hoge();

&#64;CoGroup
public void invalid(&#64;Key(...) &#64;Once Iterable&lt;Hoge&gt; input, Result&lt;Hoge&gt; result) {
    a.copyFrom(iter.next()); // create a copy
    b.copyFrom(iter.next());
    ...
}
</code></pre>
 * </li>
 * </ul>
 *
 * @since 0.9.1
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Once {
    // no special members
}
