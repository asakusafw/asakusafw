/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.util;

import java.util.LinkedList;

import com.asakusafw.dmdl.model.AstName;

/**
 * Naming utilities for Java and DMDL.
 */
public class NameUtil {

    /**
     * Convert the DMDL name into Java package-like name.
     * @param name the DMDL name
     * @return corresponded package-like name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String toPackageName(AstName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        LinkedList<String> simples = new LinkedList<String>();
        for (AstName current = name; current != null; current = current.getQualifier()) {
            simples.addFirst(JavaName.of(current.getSimpleName()).toMemberName());
        }
        StringBuilder buf = new StringBuilder();
        buf.append(simples.removeFirst());
        for (String simpleName : simples) {
            buf.append(simpleName);
        }
        return buf.toString();
    }

    private NameUtil() {
        return;
    }
}
