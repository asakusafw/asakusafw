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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utilities about charset and encoding.
 * @since 0.10.3
 */
public final class CharsetUtil {

    private static final Pattern PATTERN_ASCII_NOT_COMPAT = Pattern.compile("\\bUTF-(16|32)(BE|LE)?\\b"); //$NON-NLS-1$

    private static final Set<Charset> KNOWN_ASCII_NOT_COMPAT;
    static {
        KNOWN_ASCII_NOT_COMPAT = Charset.availableCharsets().values().stream()
                .filter(s -> PATTERN_ASCII_NOT_COMPAT.matcher(s.name()).find())
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    private CharsetUtil() {
        return;
    }

    /**
     * returns whether or not the given charset encoding is compatible with ASCII.
     * @param cs the target charset encoding
     * @return {@code true} if it is compatible, otherwise {@code false}
     */
    public static boolean isAsciiCompatible(Charset cs) {
        return !KNOWN_ASCII_NOT_COMPAT.contains(cs);
    }
}
