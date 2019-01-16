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
package com.asakusafw.runtime.io.text.tabular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a set of escape sequences definition.
 * @since 0.9.1
 */
public final class EscapeSequence {

    private final char escapeCharacter;

    private final boolean escapeLineSeparator;

    private final List<Entry> entries;

    EscapeSequence(char escapeCharacter, boolean escapeLineSeparator, List<Entry> entries) {
        this.escapeCharacter = escapeCharacter;
        this.escapeLineSeparator = escapeLineSeparator;
        this.entries = Collections.unmodifiableList(entries);
    }

    /**
     * Returns a new builder.
     * @param escapeCharacter the escape character
     * @return the created builder
     */
    public static Builder builder(char escapeCharacter) {
        return new Builder(escapeCharacter);
    }

    /**
     * Returns the escape character.
     * @return the escape character
     */
    public char getEscapeCharacter() {
        return escapeCharacter;
    }

    /**
     * Returns whether or not line separator character can be escaped.
     * @return {@code true} if line separator character can be escaped, otherwise {@code false}
     */
    public boolean canEscapeLineSeparator() {
        return escapeLineSeparator;
    }

    /**
     * Returns whether or not the given character can be escaped.
     * @param c the target character
     * @return {@code true} if the given character can be escaped, otherwise {@code false}
     */
    public boolean canEscape(char c) {
        return entries.stream()
                .anyMatch(e -> e.from == c);
    }

    /**
     * Returns the entries.
     * @return the entries
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Represents an entry of {@link EscapeSequence}.
     * @since 0.9.1
     */
    public static class Entry {

        final char from;

        final Character to;

        Entry(char from, Character to) {
            this.from = from;
            this.to = to;
        }

        /**
         * Returns the original character.
         * @return the original character
         */
        public char getFrom() {
            return from;
        }

        /**
         * Returns the replacement character.
         * @return the replacement character, or {@code null} if represents {@code null} field
         */
        public Character getTo() {
            return to;
        }
    }

    /**
     * A builder of {@link EscapeSequence}.
     * @since 0.9.1
     */
    public static class Builder {

        private final char escapeCharacter;

        private boolean escapeLineSeparator = false;

        private final List<Entry> entries = new ArrayList<>();

        Builder(char escapeCharacter) {
            this.escapeCharacter = escapeCharacter;
        }

        /**
         * Adds a mapping.
         * @param from the original character
         * @param to the replacement character
         * @return this
         */
        public Builder addMapping(char from, char to) {
            this.entries.add(new Entry(from, to));
            return this;
        }

        /**
         * Adds a mapping to {@code NULL}.
         * @param from the original character
         * @return this
         */
        public Builder addNullMapping(char from) {
            this.entries.add(new Entry(from, null));
            return this;
        }

        /**
         * Adds a mapping about line separators.
         * @return this
         */
        public Builder addLineSeparator() {
            this.escapeLineSeparator = true;
            return this;
        }

        /**
         * Builds {@link EscapeSequence}.
         * @return the built object
         */
        public EscapeSequence build() {
            return new EscapeSequence(escapeCharacter, escapeLineSeparator, entries);
        }
    }
}
