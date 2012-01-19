/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.hadoop;

import java.util.Arrays;

/**
 * Block information.
 * @since 0.2.5
 */
public final class BlockInfo {

    private static final String[] EMPTY = new String[0];

    final long start;

    final long end;

    final String[] hosts;

    /**
     * Creates a new instance.
     * @param start start byte position (inclusive)
     * @param end end byte position (exclusive)
     * @param hosts host names, or {@code null} for empty
     */
    public BlockInfo(long start, long end, String[] hosts) {
        this.start = start;
        this.end = end;
        if (hosts == null) {
            this.hosts = EMPTY;
        } else {
            this.hosts = hosts.clone();
            Arrays.sort(this.hosts);
        }
    }

    /**
     * Returns whether this and the other block has same owner(s).
     * If both has no owners, this returns {@code true}.
     * @param other the other block
     * @return {@code true} if the both has same owner, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean isSameOwner(BlockInfo other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
        }
        return Arrays.equals(hosts, other.hosts);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BlockInfo [start=");
        builder.append(start);
        builder.append(", end=");
        builder.append(end);
        builder.append(", hosts=");
        builder.append(Arrays.toString(hosts));
        builder.append("]");
        return builder.toString();
    }
}
