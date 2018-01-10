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
package com.asakusafw.utils.java.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Java DOM trait for attaching comment text.
 */
public final class CommentEmitTrait {

    private static final String REGEX_LINE_DELIMITER = "\\n|\\r|\\r\\n"; //$NON-NLS-1$

    private List<String> contents;

    /**
     * Creates a new instance.
     * @param contents the comment lines
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public CommentEmitTrait(List<String> contents) {
        if (contents == null) {
            throw new IllegalArgumentException("contents must not be null"); //$NON-NLS-1$
        }
        this.contents = new ArrayList<>(contents.size());
        for (String line: contents) {
            String[] splitted = line.split(REGEX_LINE_DELIMITER);
            for (String s : splitted) {
                this.contents.add(s);
            }
        }
        this.contents = Collections.unmodifiableList(this.contents);
    }

    /**
     * Returns the comment lines.
     * @return the comment lines
     */
    public List<String> getContents() {
        return this.contents;
    }
}
