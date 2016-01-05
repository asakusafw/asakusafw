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
package com.asakusafw.dmdl.thundergate.view;

/**
 * VIEW definition information.
 */
public class ViewDefinition {

    /**
     * The name of VIEW.
     */
    public final String name;

    /**
     * The SQL statement of VIEW.
     */
    public final String statement;

    /**
     * Creates and returns a new instance.
     * @param name the name
     * @param statement the SQL statement of this definition
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ViewDefinition(String name, String statement) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (statement == null) {
            throw new IllegalArgumentException("statement must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.statement = statement;
    }
}
