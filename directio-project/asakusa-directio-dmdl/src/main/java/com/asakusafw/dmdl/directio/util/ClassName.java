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
package com.asakusafw.dmdl.directio.util;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.java.model.util.Models;

/**
 * Represents a class name.
 * @since 0.9.1
 */
public class ClassName {

    static final Logger LOG = LoggerFactory.getLogger(ClassName.class);

    private final String name;

    /**
     * Creates a new instance.
     * @param name the class name
     */
    public ClassName(String name) {
        this.name = name;
    }

    /**
     * Returns whether or not the given string is a valid class name.
     * @param name the target string
     * @return {@code true} if it is valid, otherwise {@code false}
     */
    public static boolean isValid(String name) {
        if (checkSyntax(name) == false) {
            return false;
        }
        try {
            Models.toName(Models.getModelFactory(), name);
            return true;
        } catch (IllegalArgumentException e) {
            LOG.trace("invalid class name: {}", name, e); //$NON-NLS-1$
            return false;
        }
    }

    private static boolean checkSyntax(String name) {
        boolean start = false;
        for (char c : name.toCharArray()) {
            if (start) {
                if (Character.isJavaIdentifierStart(c)) {
                    start = false;
                    continue;
                }
                return false;
            } else {
                if (c == '.') {
                    start = true;
                    continue;
                }
                if (Character.isJavaIdentifierPart(c)) {
                    continue;
                }
                return false;
            }
        }
        return start == false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ClassName other = (ClassName) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
