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
package com.asakusafw.yaess.bootstrap;

/**
 * Represents an extended argument.
 * @since 0.8.0
 */
public class ExtendedArgument {

    /**
     * The parameter prefix of extended arguments.
     */
    public static final String PREFIX = "-X-"; //$NON-NLS-1$

    private final String name;

    private final String value;

    /**
     * Creates a new instance.
     * @param name the argument name
     * @param value the argument value
     */
    public ExtendedArgument(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name.
     * The name does not include the common {@link #PREFIX prefix}.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s%s:%s", PREFIX, name, value); //$NON-NLS-1$
    }
}
