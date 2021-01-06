/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.text.SimpleDateFormat;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a date format.
 * @since 0.9.1
 */
public class DatePattern {

    static final Logger LOG = LoggerFactory.getLogger(DatePattern.class);

    private final String token;

    /**
     * Creates a new instance.
     * @param token the token
     */
    public DatePattern(String token) {
        this.token = token;
    }

    /**
     * Returns whether or not the given string is a valid date pattern.
     * @param pattern the target string
     * @return {@code true} if it is valid, otherwise {@code false}
     */
    public static boolean isValid(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern(pattern);
            return true;
        } catch (IllegalArgumentException e) {
            LOG.trace("invalid date pattern: {}", pattern, e); //$NON-NLS-1$
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(token);
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
        DatePattern other = (DatePattern) obj;
        if (!Objects.equals(token, other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return token;
    }
}
