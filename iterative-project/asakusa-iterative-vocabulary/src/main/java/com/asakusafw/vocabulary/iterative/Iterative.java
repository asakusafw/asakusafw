/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.iterative;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.flow.Import;

/**
 * An annotation for <em>iterative elements</em>.
 * Iterative elements must be an external input or user defined operator.
 *
 * <h3> iterative external input </h3>
 * To make an external input iterative, put an <code>&#64;Iterative</code> before {@link Import &#64;Import}.
<pre><code>
// external inputs
&#64;IterativeBatch(name = ...)
public class Something extends FlowDescription {
    ...
    public Something(
            &#64;Iterative(...) &#64;Import(...) In&lt;...&gt; input,
            ...) {
        ...
    }
    ...
}
</code></pre>
 *
 * <h3> iterative user operator </h3>
 * If iterative parameters are used in a user defined operator, put an <code>&#64;Iterative</code> before
 * its operator annotation.
<pre><code>
// operators
public abstract class Something {
    ...
    &#64;Iterative(...)
    &#64;SomeOperator
    public void something(...) {
        ... BatchContext.get("some-iterative-parameter") ...
    }
    ...
}
</code></pre>
 *
 * <h3> iteration scope </h3>
 * If <code>&#64;Iterative</code> has {@link #value() iterative parameter names}, the annotated element will be
 * considered as a <em>scoped iterative element</em>. It sometimes has advantages for <em>scope-less iterative
 * elements</em>: re-evaluating the target element will be reduced (optional operation).
 *
 * @since 0.8.0
 */
@Target({
    ElementType.PARAMETER, // @Import, @Export
    ElementType.METHOD, // operator methods
    ElementType.TYPE, // operator classes, importer/exporter descriptions
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Iterative {

    /**
     * The iterative parameter names.
     * If this is empty, the target element always re-evaluates for each iteration.
     */
    String[] value() default { };
}
