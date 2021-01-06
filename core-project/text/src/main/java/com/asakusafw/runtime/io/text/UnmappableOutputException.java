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
package com.asakusafw.runtime.io.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An exception occurred if a content contains characters which cannot map to output sequence.
 * @since 0.9.1
 */
public class UnmappableOutputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final UnmappableOutput[] entries;

    /**
     * Creates a new instance.
     * @param entries the error entries
     */
    public UnmappableOutputException(List<? extends UnmappableOutput> entries) {
        super(entries.stream()
                .map(UnmappableOutput::toString)
                .collect(Collectors.joining(", "))); //$NON-NLS-1$
        this.entries = entries.toArray(new UnmappableOutput[entries.size()]);
    }

    /**
     * Returns the entries.
     * @return the entries
     */
    public List<UnmappableOutput> getEntries() {
        return Collections.unmodifiableList(Arrays.asList(entries));
    }
}
