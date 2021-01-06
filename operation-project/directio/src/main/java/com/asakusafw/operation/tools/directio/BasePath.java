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
package com.asakusafw.operation.tools.directio;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.asakusafw.runtime.directio.FilePattern;

/**
 * Represents a base path of Direct I/O.
 * @since 0.10.0
 */
public class BasePath {

    /**
     * The empty path.
     */
    public static final BasePath EMPTY = new BasePath(Collections.emptyList());

    private final List<String> segments;

    BasePath(List<String> segments) {
        this.segments = Collections.unmodifiableList(segments);
    }

    /**
     * Returns whether or not the given path is empty.
     * @param path the path
     * @return {@code true} if it is empty
     */
    public static boolean isEmpty(String path) {
        return Arrays.stream(path.split("/"))
                .filter(it -> it.isEmpty() == false)
                .findAny()
                .isPresent() == false;
    }

    /**
     * Resolves the given path.
     * @param path the given path
     * @return the base path, or {@code empty} if it is not a valid base path
     */
    public static Optional<BasePath> parse(String path) {
        if (isEmpty(path)) {
            return Optional.of(EMPTY);
        }
        FilePattern pattern = FilePattern.compile(path);
        List<String> results = new ArrayList<>();
        for (FilePattern.Segment segment : pattern.getSegments()) {
            if (isPlain(segment)) {
                results.add(segment.toString());
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(new BasePath(results));
    }

    /**
     * Resolves the given path.
     * @param path the given path
     * @return the base path
     */
    public static BasePath of(String path) {
        return parse(path).orElseThrow(() -> new IllegalArgumentException(path));
    }

    /**
     * Extracts a base path from the given path.
     * @param path the given path
     * @return the base path of the given path
     */
    public static BasePath headOf(String path) {
        if (isEmpty(path)) {
            return BasePath.EMPTY;
        }
        FilePattern pattern = FilePattern.compile(path);
        return headOf(pattern);
    }

    private static BasePath headOf(FilePattern pattern) {
        List<String> results = new ArrayList<>();
        for (FilePattern.Segment segment : pattern.getSegments()) {
            if (isPlain(segment)) {
                results.add(segment.toString());
            } else {
                break;
            }
        }
        return new BasePath(results);
    }

    private static boolean isPlain(FilePattern.Segment segment) {
        return segment.isTraverse() == false &&  segment.getElements().stream()
                .map(FilePattern.PatternElement::getKind)
                .allMatch(Predicate.isEqual(FilePattern.PatternElementKind.TOKEN));
    }

    /**
     * Returns whether this represents the root path.
     * @return {@code true} if this represents the root path
     */
    public boolean isRoot() {
        return segments.isEmpty();
    }

    /**
     * Returns the path segments.
     * @return the path segments
     */
    public List<String> getSegments() {
        return segments;
    }

    /**
     * Returns whether or not this path is a prefix of the given path.
     * @param other the target path
     * @return {@code true} if this path is a prefix of the given path, otherwise {@code false}
     */
    public boolean isPrefixOf(BasePath other) {
        if (segments.isEmpty()) {
            return true;
        }
        if (segments.size() > other.segments.size()) {
            return false;
        }
        return segments.equals(other.segments.subList(0, segments.size()));
    }

    /**
     * Extracts the rest resource pattern of the given path.
     * @param path the target path
     * @return the rest resource pattern
     * @see #headOf(String)
     */
    public Optional<FilePattern> restOf(String path) {
        if (BasePath.isEmpty(path)) {
            return Optional.empty();
        }
        FilePattern pattern = FilePattern.compile(path);
        if (segments.isEmpty()) {
            return Optional.of(pattern);
        }
        BasePath other = headOf(pattern);
        if (this.isPrefixOf(other) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" must be a prefix of the path: {1}",
                    this,
                    path));
        }
        if (segments.size() == pattern.getSegments().size()) {
            return Optional.empty();
        } else {
            return Optional.of(FilePattern.of(pattern.getSegments().subList(
                segments.size(),
                pattern.getSegments().size())));
        }
    }

    /**
     * Returns the relative path of the given path.
     * @param other the target path
     * @return the relative path
     */
    public BasePath relativise(BasePath other) {
        if (segments.isEmpty()) {
            return other;
        }
        if (this.isPrefixOf(other) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" must be a prefix of the path: {1}",
                    this,
                    other));
        }
        return new BasePath(new ArrayList<>(other.segments.subList(segments.size(), other.segments.size())));
    }

    /**
     * Returns a file pattern that is a sequence of this and the given pattern.
     * @param pattern the target pattern
     * @return the resolved pattern
     */
    public FilePattern resolve(FilePattern pattern) {
        if (segments.isEmpty()) {
            return pattern;
        }
        List<FilePattern.Segment> results = new ArrayList<>();
        FilePattern prefix = FilePattern.compile(toString());
        results.addAll(prefix.getSegments());
        results.addAll(pattern.getSegments());
        return FilePattern.of(results);
    }

    /**
     * Returns a base path that is concatenation of this and the given path.
     * @param other the suffix path
     * @return the resolved path
     */
    public BasePath resolve(BasePath other) {
        if (segments.isEmpty()) {
            return other;
        }
        List<String> results = new ArrayList<>();
        results.addAll(segments);
        results.addAll(other.segments);
        return new BasePath(results);
    }

    /**
     * Returns the path string.
     * @return the path string, or an empty string if it represents the root
     */
    public String getPathString() {
        return String.join("/", segments);
    }

    /**
     * Returns the file pattern.
     * @return the file pattern
     */
    public FilePattern asFilePattern() {
        if (segments.isEmpty()) {
            throw new IllegalStateException();
        } else {
            return FilePattern.compile(getPathString());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(segments);
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
        BasePath other = (BasePath) obj;
        return Objects.equals(segments, other.segments);
    }

    @Override
    public String toString() {
        if (segments.isEmpty()) {
            return "/";
        } else {
            return getPathString();
        }
    }
}
