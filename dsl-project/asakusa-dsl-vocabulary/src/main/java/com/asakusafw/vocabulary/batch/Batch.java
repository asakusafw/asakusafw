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
package com.asakusafw.vocabulary.batch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

/**
 * An annotation for batch classes.
 * @since 0.1.0
 * @version 0.5.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Batch {

    /**
     * The default parameter value pattern.
     */
    String DEFAULT_PARAMETER_VALUE_PATTERN = ".*"; //$NON-NLS-1$

    /**
     * The identifier for the target batch ({@literal a.k.a.} <em>batch ID</em>).
     * The batch ID must be unique in the system, and must be in the form of the following rule:
<pre><code>
Name:
    SimpleName
    Name "." SimpleName
SimpleName:
    NameStart NamePart*
NameStart: one of
    A-Z
    a-z
    _
NamePart: one of
    NameStart
    0-9
</code></pre>
     * In other words, the above rule is a subset of Java package names (e.g. {@code com.asakusafw}).
     */
    String name();

    /**
     * The comments for the target batch.
     * @since 0.5.0
     */
    String comment() default "";

    /**
     * The available batch arguments.
     * @since 0.5.0
     * @see #strict()
     */
    Parameter[] parameters() default { };

    /**
     * Whether this batch accepts only predefined batch arguments (in {@link #parameters()}) or not.
     * {@code false} accepts any batch arguments even if they are not predefined.
     * @since 0.5.0
     */
    boolean strict() default false;

    /**
     * Represents a batch parameter.
     * @since 0.5.0
     */
    public @interface Parameter {

        /**
         * The name of this parameter.
         */
        String key();

        /**
         * Comments for this parameter.
         */
        String comment() default "";

        /**
         * Whether this parameter is mandatory or not.
         */
        boolean required() default true;

        /**
         * The valid argument value pattern in {@link Pattern Java regular expressions}.
         * If this is not specified, any values will be accepted.
         */
        String pattern() default DEFAULT_PARAMETER_VALUE_PATTERN;
    }
}
