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
package com.asakusafw.compiler.common;

import java.util.HashSet;
import java.util.Set;

import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;

/**
 * Generates unique names.
 */
public class NameGenerator {

    private final ModelFactory factory;

    private final Set<String> used = new HashSet<>();

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public NameGenerator(ModelFactory factory) {
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        this.factory = factory;
    }

    /**
     * Adds a name as reserved one.
     * @param name the target name
     * @return the reserved name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public String reserve(String name) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        used.add(name);
        return name;
    }

    /**
     * Adds a new unique name and returns it.
     * @param hint the hint of the new name
     * @return the unique name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public SimpleName create(String hint) {
        Precondition.checkMustNotBeNull(hint, "hint"); //$NON-NLS-1$
        int initial = 0;
        String name = hint;
        if (used.contains(name)) {
            int number = initial;
            String current;
            do {
                current = name + number;
                number++;
            } while (used.contains(current));
            name = current;
        }
        used.add(name);
        return factory.newSimpleName(name);
    }
}
