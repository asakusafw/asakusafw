/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * A pattern describes file location.
 * <h3 id="pattern-expression">Pattern Expression</h3>
<pre><code>
expression:
    (segment "/")* segment

segment:
    traverse
    directory

traverse:
    "**" "/"

directory:
    resource "/"

file:
    resource

resource:
    token (wildcard token)* wildcard?
    wildcard (token wildcard)* token?

token:
    variable
    character+

wildcard:
    "*"

variable:
    "${" character* "}"

character:
    (any character except "/", "\", "$", "*", "?", "#", "{", "}", "[", "]")

</code></pre>
 * For example:
 * <table border="1">
 * <tr>
 *   <th> Expression </th>
 *   <th> Description </th>
 * </tr>
 * <tr>
 *   <td> <code>a/b/c.csv</code> </td>
 *   <td> just {@code a/b/c.csv} </td>
 * </tr>
 * <tr>
 *   <td> <code>&#42;</code> </td>
 *   <td> all files in target directory </td>
 * </tr>
 * <tr>
 *   <td> <code>&#42;&#42;</code> </td>
 *   <td> all files in target directory (recursive) </td>
 * </tr>
 * <tr>
 *   <td> <code>&#42;.csv</code> </td>
 *   <td> all CSV files in target directory</td>
 * </tr>
 * <tr>
 *   <td> <code>&#42;&#42;/&#42;.csv</code> </td>
 *   <td> all CSV files in target directory (recursive) </td>
 * </tr>
 * </table>
 * @since 0.2.5
 */
public class FilePattern implements ResourcePattern {

    private static final int CHAR_ESCAPE = '\\';

    private static final int CHAR_SEPARATOR = '/';

    private static final int CHAR_ASTERISK = '*';

    private static final int CHAR_BAR = '|';

    private static final int CHAR_DOLLER = '$';

    private static final int CHAR_QUESTION = '?';

    private static final int CHAR_NUMBER = '#';

    private static final int CHAR_OPEN_BRACE = '{';

    private static final int CHAR_CLOSE_BRACE = '}';

    private static final int CHAR_OPEN_BRACKET = '[';

    private static final int CHAR_CLOSE_BRACKET = ']';

    private static final int CHAR_EOF = -1;

    private static final int CHAR_ALPHABET = -2;

    static final BitSet CHARMAP_META;
    static {
        BitSet set = new BitSet();
        set.set(0, 0x20);
        set.set(CHAR_ESCAPE);
        set.set(CHAR_SEPARATOR);
        set.set(CHAR_ASTERISK);
        set.set(CHAR_BAR);
        set.set(CHAR_DOLLER);
        set.set(CHAR_QUESTION);
        set.set(CHAR_NUMBER);
        set.set(CHAR_OPEN_BRACE);
        set.set(CHAR_CLOSE_BRACE);
        set.set(CHAR_OPEN_BRACKET);
        set.set(CHAR_CLOSE_BRACKET);
        CHARMAP_META = set;
    }

    private final List<Segment> segments;

    private final String patternString;

    FilePattern(List<Segment> segments, String patternString) {
        assert segments != null;
        assert patternString != null;
        assert segments.isEmpty() == false;
        this.segments = Collections.unmodifiableList(segments);
        this.patternString = patternString;
    }

    /**
     * Returns whether this pattern contains variables (${var}).
     * @return {@code true} if contains
     */
    public boolean containsVariables() {
        for (Segment segment : segments) {
            for (PatternElement element : segment.getElements()) {
                if (element.getKind() == PatternElementKind.VARIABLE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns segments of this pattern.
     * @return the segments
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * Returns a string which represents this pattern.
     * @return the pattern string
     */
    public String getPatternString() {
        return patternString;
    }

    @Override
    public String toString() {
        return patternString;
    }

    /**
     * Compiles pattern expression.
     * @param patternString pattern expression
     * @return the compiled object
     * @throws IllegalArgumentException if pattern is not valid
     * @see <a href="#pattern-expression">Pattern Expression</a>
     */
    public static FilePattern compile(String patternString) {
        if (patternString == null) {
            throw new IllegalArgumentException("patternString must not be null"); //$NON-NLS-1$
        }
        if (patternString.isEmpty()) {
            throw new IllegalArgumentException("patternString must not be empty"); //$NON-NLS-1$
        }
        List<Segment> segments = compileSegments(patternString);
        return new FilePattern(segments, patternString);
    }

    private static List<Segment> compileSegments(String patternString) {
        assert patternString != null;
        Cursor cursor = new Cursor(patternString.toCharArray());
        List<Segment> segments = new ArrayList<Segment>();
        cursor.skipWhile(CHAR_SEPARATOR);
        while (cursor.get(0) != CHAR_EOF) {
            Segment segment = consumeSegment(cursor);
            segments.add(segment);
        }
        return segments;
    }

    private static Segment consumeSegment(Cursor cursor) {
        assert cursor != null;
        int first = cursor.get(0);
        assert first != CHAR_SEPARATOR && first != CHAR_EOF : cursor;

        // special case
        if (first == CHAR_ASTERISK
                && cursor.get(1) == CHAR_ASTERISK
                && (cursor.get(2) == CHAR_SEPARATOR || cursor.get(2) == CHAR_EOF)) {
            cursor.skipWhile(CHAR_ASTERISK);
            cursor.skipWhile(CHAR_SEPARATOR);
            return Segment.TRAVERSE;
        }

        List<PatternElement> elements = consumeElements(cursor);
        return new Segment(elements);
    }

    private static List<PatternElement> consumeElements(Cursor cursor) {
        assert cursor != null;
        ArrayList<PatternElement> results = new ArrayList<PatternElement>();
        LOOP: while (true) {
            int c = cursor.get(0);
            switch (c) {
            case CHAR_EOF:
                break LOOP;
            case CHAR_SEPARATOR:
                cursor.skipWhile(CHAR_SEPARATOR);
                break LOOP;
            case CHAR_ASTERISK:
                if (cursor.get(1) == CHAR_ASTERISK) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Invalid consecutive wildcard \"**\" (cursor=\"{0}\", offset={1})",
                            cursor,
                            cursor.getOffset()));
                }
                cursor.skip();
                results.add(SingletonPatternElement.WILDCARD);
                break;
            case CHAR_OPEN_BRACE:
                results.add(new Selection(cursor.consumeSelection()));
                break;
            case CHAR_DOLLER:
                results.add(new Variable(cursor.consumeVariable()));
                break;
            case CHAR_ALPHABET:
                results.add(new Token(cursor.consumeToken()));
                break;
            default:
                // TODO escape sequence
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid character \"{2}\" (pattern=\"{0}\", offset={1})",
                        cursor,
                        cursor.getOffset(),
                        (char) c));
            }
        }
        return results;
    }

    private static class Cursor {

        private final char[] cbuf;

        private int cursor;

        Cursor(char[] cbuf) {
            assert cbuf != null;
            this.cbuf = cbuf;
            this.cursor = 0;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            for (int i = 0, n = cursor; i < n; i++) {
                buf.append(cbuf[i]);
            }
            buf.append(" >> ");
            for (int i = cursor, n = cbuf.length; i < n; i++) {
                buf.append(cbuf[i]);
            }
            return buf.toString();
        }

        int getOffset() {
            return cursor;
        }

        int get(int offset) {
            if (cursor + offset >= cbuf.length) {
                return CHAR_EOF;
            }
            char c = cbuf[cursor + offset];
            if (CHARMAP_META.get(c)) {
                return c;
            }
            return CHAR_ALPHABET;
        }

        void skip() {
            cursor = Math.min(cbuf.length, cursor + 1);
        }

        void skipWhile(int kind) {
            while (true) {
                int result = get(0);
                if (result == CHAR_EOF || result != kind) {
                    break;
                }
                skip();
            }
        }

        String consumeToken() {
            StringBuilder buf = new StringBuilder();
            while (true) {
                // TODO escape sequence
                int kind = get(0);
                if (kind != CHAR_ALPHABET) {
                    break;
                }
                buf.append(cbuf[cursor]);
                skip();
            }
            return buf.toString();
        }

        String consumeVariable() {
            assert get(0) == CHAR_DOLLER;
            int start = cursor;
            skip();
            if (get(0) != CHAR_OPEN_BRACE) {
                cursor = start;
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid variable format (cursor=\"{0}\", offset={1})",
                        this,
                        cursor));
            }
            skip();
            int nameStart = cursor;
            skipWhile(CHAR_ALPHABET);
            if (get(0) != CHAR_CLOSE_BRACE) {
                cursor = start;
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid variable format (cursor=\"{0}\", offset={1})",
                        this,
                        cursor));
            }
            String name = String.valueOf(cbuf, nameStart, cursor - nameStart);
            skip();
            return name;
        }

        public List<String> consumeSelection() {
            assert get(0) == CHAR_OPEN_BRACE;
            int start = cursor;
            skip();
            List<String> contents = new ArrayList<String>();
            boolean head = true;
            while (true) {
                int first = get(0);
                if (first == CHAR_EOF) {
                    cursor = start;
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Selection is not closed (cursor=\"{0}\", offset={1})",
                            this,
                            cursor));
                } else if (head || first == CHAR_BAR) {
                    if (head == false) {
                        assert get(0) == CHAR_BAR;
                        skip();
                    }
                    int alterStart = cursor;
                    while (true) {
                        skipWhile(CHAR_ALPHABET);
                        if (get(0) == CHAR_SEPARATOR) {
                            skip();
                        } else {
                            break;
                        }
                    }
                    contents.add(String.valueOf(cbuf, alterStart, cursor - alterStart));
                } else if (first == CHAR_CLOSE_BRACE) {
                    skip();
                    break;
                } else {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Invalid character in selection (cursor=\"{0}\", offset={1})",
                            this,
                            cursor));
                }
                head = false;
            }
            return contents;
        }
    }

    /**
     * Each segment in {@link FilePattern}.
     * This means a file/directory name pattern, or a any directories ({@code "**"}).
     * @since 0.2.5
     */
    public static final class Segment {

        private final List<PatternElement> elements;

        static final Segment TRAVERSE = new Segment(Collections.<PatternElement>emptyList());

        Segment(List<PatternElement> elements) {
            assert elements != null;
            boolean sawWildcard = false;
            for (PatternElement element : elements) {
                boolean isWildcard = element.getKind() == PatternElementKind.WILDCARD;
                assert sawWildcard == false || isWildcard == false;
                sawWildcard = isWildcard;
            }
            this.elements = Collections.unmodifiableList(elements);
        }

        /**
         * Returns pattern elements in this segment.
         * @return pattern elements
         */
        public List<PatternElement> getElements() {
            return elements;
        }

        /**
         * Returns whether this segment describes traverse ({@code "**"}) or not.
         * @return {@code true} to describe traverse, {@code false} to not
         */
        public boolean isTraverse() {
            return elements.isEmpty();
        }
    }

    /**
     * Kind of {@link PatternElement}.
     * @since 0.2.5
     */
    public enum PatternElementKind {

        /**
         * Normal token.
         */
        TOKEN,

        /**
         * Variable.
         * If element is this kind, the element must be type of {@link Variable}.
         */
        VARIABLE,

        /**
         * Selection.
         * If element is this kind, the element must be type of {@link Selection}.
         */
        SELECTION,

        /**
         * Any string.
         */
        WILDCARD,
    }

    /**
     * A piece of file/directory name pattern.
     * @since 0.2.5
     * @see PatternElementKind
     */
    public interface PatternElement {

        /**
         * Returns the kind of this element.
         * @return the kind of this element
         */
        PatternElementKind getKind();

        /**
         * Returns the token of this element.
         * If this element a kind of {@link PatternElementKind#TOKEN}, this returns the original token.
         * @return the token
         */
        String getToken();
    }

    private enum SingletonPatternElement implements PatternElement {

        WILDCARD {
            @Override
            public PatternElementKind getKind() {
                return PatternElementKind.WILDCARD;
            }
            @Override
            public String getToken() {
                return "*";
            }
        },

        ;
        @Override
        public String toString() {
            return getToken();
        }
    }

    private static class Token implements PatternElement {

        private final String contents;

        Token(String contents) {
            assert contents != null;
            assert contents.isEmpty() == false;
            this.contents = contents;
        }

        @Override
        public PatternElementKind getKind() {
            return PatternElementKind.TOKEN;
        }

        @Override
        public String getToken() {
            return contents;
        }

        @Override
        public String toString() {
            return getToken();
        }
    }

    /**
     * Represents a variable.
     * @since 0.2.5
     * @see PatternElementKind#VARIABLE
     */
    public static class Variable implements PatternElement {

        private final String name;

        Variable(String name) {
            assert name != null;
            this.name = name;
        }

        /**
         * Returns the name of this variable.
         * @return the name
         */
        public String getName() {
            return name;
        }

        @Override
        public PatternElementKind getKind() {
            return PatternElementKind.VARIABLE;
        }

        @Override
        public String getToken() {
            return String.format("${%s}", getName());
        }

        @Override
        public String toString() {
            return getToken();
        }
    }

    /**
     * Represents a selecion.
     * @since 0.2.5
     * @see PatternElementKind#SELECTION
     */
    public static class Selection implements PatternElement {

        private final List<String> contents;

        Selection(List<String> contents) {
            assert contents != null;
            assert contents.isEmpty() == false;
            this.contents = Collections.unmodifiableList(contents);
        }

        /**
         * Returns contents in this selection.
         * @return the contents
         */
        public List<String> getContents() {
            return contents;
        }

        @Override
        public PatternElementKind getKind() {
            return PatternElementKind.SELECTION;
        }

        @Override
        public String getToken() {
            StringBuilder buf = new StringBuilder();
            buf.append((char) CHAR_OPEN_BRACE);
            if (contents.isEmpty() == false) {
                buf.append(contents.get(0));
                for (int i = 1, n = contents.size(); i < n; i++) {
                    buf.append((char) CHAR_BAR);
                    buf.append(contents.get(i));
                }
            }
            buf.append((char) CHAR_CLOSE_BRACE);
            return buf.toString();
        }

        @Override
        public String toString() {
            return getToken();
        }
    }
}
