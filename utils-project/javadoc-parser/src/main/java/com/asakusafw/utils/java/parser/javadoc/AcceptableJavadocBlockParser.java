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
package com.asakusafw.utils.java.parser.javadoc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link JavadocBaseParser} which accepts tag blocks by their tag name.
 */
public abstract class AcceptableJavadocBlockParser extends JavadocBlockParser {

    private final Set<String> acceptable;

    /**
     * Creates a new instance.
     */
    public AcceptableJavadocBlockParser() {
        this.acceptable = Collections.emptySet();
    }

    /**
     * Creates a new instance.
     * @param tagName the first acceptable tag name
     * @param tagNames the rest acceptable tag names
     */
    public AcceptableJavadocBlockParser(String tagName, String...tagNames) {
        this.acceptable = new HashSet<>();
        this.acceptable.add(tagName);
        Collections.addAll(this.acceptable, tagNames);
    }

    /**
     * Creates a new instance.
     * @param tagNames the acceptable tag names
     */
    public AcceptableJavadocBlockParser(String[] tagNames) {
        this.acceptable = new HashSet<>();
        this.acceptable.addAll(Arrays.asList(tagNames));
    }

    @Override
    public boolean canAccept(String tag) {
        return acceptable.contains(tag);
    }
}
