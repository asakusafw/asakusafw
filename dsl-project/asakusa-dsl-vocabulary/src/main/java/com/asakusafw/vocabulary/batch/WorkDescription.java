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

import java.util.regex.Pattern;

/**
 * A description of Unit-of-Work in batch.
 * @see Work
 * @see BatchDescription
 */
public abstract class WorkDescription {

    /**
     * Returns the identifier of this work.
     * <p>
     * The ID must be identical in the batch, and its name can be following format:
     * </p>
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
     * @return the ID of this work
     */
    public abstract String getName();

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*"); //$NON-NLS-1$

    /**
     * Returns whether the name is valid identifier or not.
     * @param name the name
     * @return {@code true} if the name is valid identifier, or {@code false} if it is not valid
     */
    protected static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return VALID_NAME.matcher(name).matches();
    }
}
