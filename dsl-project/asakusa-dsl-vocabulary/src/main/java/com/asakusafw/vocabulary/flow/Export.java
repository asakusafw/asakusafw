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
package com.asakusafw.vocabulary.flow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * An annotation for specifying <em>export operations</em> of jobflows.
 * This annotates each {@link Out} parameter of jobflow class constructors.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Export {

    /**
     * The export operation identifier.
     * Each <em>export operation</em> must have a unique identifier in the same jobflow,
     * and must be in the form of the following rule:
<pre><code>
Name :
    NameStart NamePart*
NameStart: one of
    A-Z
    a-z
    _
NamePart: one of
    NameStart
    0-9
</code></pre>
     * In other words, the above rule is a subset of Java class or names.
     */
    String name();

    /**
     * The exporter description class for describing the export operation of the target flow output.
     */
    Class<? extends ExporterDescription> description();
}
