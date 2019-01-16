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
package com.asakusafw.runtime.directio;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The fragment of each input dataset.
 * @since 0.2.5
 */
public final class DirectInputFragment {

    private final String path;

    private final long offset;

    private final long length;

    private final List<String> ownerNodeNames;

    private final Map<String, String> attributes;

    /**
     * Creates a new instance with no attributes.
     * @param path the path
     * @param offset the byte offset
     * @param length the byte length
     * @param ownerNodeNames the owner host names
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectInputFragment(String path, long offset, long length, List<String> ownerNodeNames) {
        this(path, offset, length, ownerNodeNames, Collections.emptyMap());
    }

    /**
     * Creates a new instance.
     * @param path the path
     * @param offset the byte offset
     * @param length the byte length
     * @param ownerNodeNames the owner host names
     * @param attributes the attributes for each fragments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectInputFragment(
            String path,
            long offset,
            long length,
            List<String> ownerNodeNames,
            Map<String, String> attributes) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        if (ownerNodeNames == null) {
            throw new IllegalArgumentException("ownerNodeNames must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.path = path;
        this.offset = offset;
        this.length = length;
        this.ownerNodeNames = ownerNodeNames;
        this.attributes = attributes;
    }

    /**
     * Returns the path to this fragment.
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the byte offset of this fragment.
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the byte length of this fragment.
     * @return the length
     */
    public long getSize() {
        return length;
    }

    /**
     * Returns the node names of this owners.
     * Without this owners, this will return an empty list.
     * @return the node names, or an empty list
     */
    public List<String> getOwnerNodeNames() {
        return ownerNodeNames;
    }

    /**
     * Returns the extra attributes.
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "DirectInputFragment(path={0}, offset={1}, length={2}, owner={3}, attributes={4})", //$NON-NLS-1$
                path,
                offset,
                length,
                ownerNodeNames,
                attributes);
    }
}
