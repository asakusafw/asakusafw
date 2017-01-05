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
package com.asakusafw.compiler.flow;

import java.util.LinkedList;
import java.util.regex.Pattern;

import com.asakusafw.compiler.common.Precondition;

/**
 * Represents a resource location.
 * @since 0.1.0
 * @version 0.4.0
 */
public class Location {

    /**
     * The suffix name for prefix locations.
     */
    public static final String WILDCARD_SUFFIX = "-*"; //$NON-NLS-1$

    private final Location parent;

    private final String name;

    private boolean prefix;

    /**
     * Creates a new instance.
     * @param parent the parent location, or {@code null} for the root location
     * @param name the resource name
     * @throws IllegalArgumentException if the {@code name} is {@code null}
     */
    public Location(Location parent, String name) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        if (parent != null && parent.isPrefix()) {
            throw new IllegalArgumentException();
        }
        this.parent = parent;
        this.name = name;
        this.prefix = false;
    }

    /**
     * Returns a new location which this location is used as its prefix.
     * @return the prefixed location of this
     */
    public Location asPrefix() {
        Location copy = new Location(parent, name);
        copy.prefix = true;
        return copy;
    }

    /**
     * Returns whether this represents a prefix location or not.
     * @return {@code true} if this represents a prefix location, otherwise {@code false}
     */
    public boolean isPrefix() {
        return prefix;
    }

    /**
     * Returns the parent location.
     * @return the parent location, or {@code null} if there is no parent location
     */
    public Location getParent() {
        return parent;
    }

    /**
     * Returns the name of this location.
     * @return the resource name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a new location which has this as parent and the specified name as its resource name.
     * @param lastName the resource name
     * @return the created location
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Location append(String lastName) {
        Precondition.checkMustNotBeNull(lastName, "lastName"); //$NON-NLS-1$
        return new Location(this, lastName);
    }

    /**
     * Returns a new location which is concatenated this and the specified location.
     * @param suffix the suffix location
     * @return the created location
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Location append(Location suffix) {
        Precondition.checkMustNotBeNull(suffix, "suffix"); //$NON-NLS-1$
        LinkedList<String> segments = new LinkedList<>();
        Location current = suffix;
        while (current != null) {
            segments.addFirst(current.name);
            current = current.parent;
        }
        current = this;
        for (String segment : segments) {
            current = new Location(current, segment);
        }
        if (suffix.isPrefix()) {
            current = current.asPrefix();
        }
        return current;
    }

    /**
     * Parses a path string and returns it as {@link Location}.
     * @param pathString the path string
     * @param separator the separator character
     * @return the parsed location
     * @throws IllegalArgumentException if the path string is something wrong
     */
    public static Location fromPath(String pathString, char separator) {
        Precondition.checkMustNotBeNull(pathString, "pathString"); //$NON-NLS-1$
        boolean prefix = pathString.endsWith(WILDCARD_SUFFIX);
        String normalized = prefix
            ? pathString.substring(0, pathString.length() - WILDCARD_SUFFIX.length())
            : pathString;
        String[] segments = normalized.split(Pattern.quote(String.valueOf(separator)));
        Location current = null;
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            current = new Location(current, segment);
        }
        assert current != null;
        if (prefix) {
            current = current.asPrefix();
        }
        return current;
    }

    /**
     * Returns this as path string.
     * The separator character will not be inserted into the head of the path string.
     * @param separator the separator character
     * @return the path string
     */
    public String toPath(char separator) {
        LinkedList<String> segments = new LinkedList<>();
        Location current = this;
        while (current != null) {
            segments.addFirst(current.name);
            current = current.parent;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(segments.removeFirst());
        for (String segment : segments) {
            buf.append(separator);
            buf.append(segment);
        }
        if (prefix) {
            buf.append(WILDCARD_SUFFIX);
        }
        return buf.toString();
    }

    /**
     * Returns whether this location is a prefix of another location.
     * @param other target location
     * @return {@code true} if is prefix or same location, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean isPrefixOf(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null"); //$NON-NLS-1$
        }
        int thisSegments = count(this);
        int otherSegments = count(other);
        if (thisSegments > otherSegments) {
            return false;
        }
        Location current = other;
        for (int i = 0, n = otherSegments - thisSegments; i < n; i++) {
            current = current.getParent();
        }
        return this.equals(current);
    }

    private int count(Location location) {
        int count = 1;
        Location current = location.getParent();
        while (current != null) {
            count++;
            current = current.getParent();
        }
        return count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        Location current = this;
        result = prime * result + (prefix ? 0 : 1);
        while (current != null) {
            result = prime * result + current.name.hashCode();
            current = current.parent;
        }
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
        Location other = (Location) obj;
        Location thisCur = this;
        Location otherCur = other;
        if (thisCur.prefix != otherCur.prefix) {
            return false;
        }
        while (thisCur != null && otherCur != null) {
            if (thisCur == otherCur) {
                return true;
            }
            if (thisCur.name.equals(otherCur.name) == false) {
                return false;
            }
            thisCur = thisCur.parent;
            otherCur = otherCur.parent;
        }
        return thisCur == otherCur;
    }

    @Override
    public String toString() {
        return toPath('/');
    }
}
