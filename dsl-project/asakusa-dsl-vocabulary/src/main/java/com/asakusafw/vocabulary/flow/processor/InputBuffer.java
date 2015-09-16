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
package com.asakusafw.vocabulary.flow.processor;

import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;

/**
 * Represents buffering strategies of list-style operator inputs.
 * @since 0.2.0
 */
public enum InputBuffer implements FlowElementAttribute {

    /**
     * Builds a buffer onto the JVM heap, and will <em>expand its area</em> each time the buffer is full.
     * This strategy is relatively fast, but it is limited by JVM heap constraints.
     * For example, it is hard to handle a large input group for this strategy.
     */
    EXPAND,

    /**
     * Builds a buffer onto the JVM heap, and will <em>escape it to disks</em> each time the buffer is full.
     * With this strategy, the operator can handle a large input group, but clients retrieve only one by one object
     * from it (old objects may by changed when retrieving another one). Additionally, even if an object in the list
     * was changed, its change may be lost when another object was retrieved from the list.
     *
     * For example, the operations are not guaranteed in the following case:
<pre><code>
&#64;CoGroup(inputBuffer = InputBuffer.ESCAPE)
public void invalid(&#64;Key(...) List&lt;Hoge&gt; list, Result&lt;Hoge&gt; result) {
    // contents of 'a' is not guaranteed after 'list.get(1)'
    Hoge a = list.get(0);
    Hoge b = list.get(1);

    // the change may be lost after 'list.get(2)'
    b.setValue(100);
    list.get(2);
}
</code></pre>
     * In such the case, application developers should create a copy of the object:
<pre><code>
Hoge a = new Hoge();
Hoge b = new Hoge();

&#64;CoGroup(inputBuffer = InputBuffer.ESCAPE)
public void invalid(&#64;Key(...) List&lt;Hoge&gt; list, Result&lt;Hoge&gt; result) {
    a.copyFrom(list.get(0));
    b.copyFrom(list.get(1));
    b.setValue(100);
    list.get(2);
    ...
}
</code></pre>
     * Note that, copy is not necessary if objects are only used one by one:
<pre><code>
&#64;CoGroup(inputBuffer = InputBuffer.ESCAPE)
public void invalid(&#64;Key(...) List&lt;Hoge&gt; list, Result&lt;Hoge&gt; result) {
    for (Hoge hoge : list) {
        hoge.setValue(100);
        result.add(hoge);
    }
}
</code></pre>
     */
    ESCAPE,
}
