/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.parser;

import static com.asakusafw.dmdl.parser.JjDmdlParserConstants.*;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Region;
import com.asakusafw.utils.collections.Sets;

/**
 * Analyzes {@link ParseException} and converts it to {@link Diagnostic}s.
 * @since 0.5.3
 */
class SyntaxErrorAnalyzer {

    private static final Set<Integer> NAMES = Sets.freeze(new Integer[] {
        NAME,
        PROJECTIVE,
        JOINED,
        SUMMARIZED,
    });

    private static final Set<Integer> TYPES = Sets.freeze(new Integer[] {
        INT,
        LONG,
        BYTE,
        SHORT,
        DECIMAL,
        FLOAT,
        DOUBLE,
        TEXT,
        BOOLEAN,
        DATE,
        DATETIME,
    });

    private static final Set<Integer> DELIMITERS = Sets.freeze(new Integer[] {
        END_OF_DECLARATION,
        PROPERTY_SEPARATOR,
    });

    private final JjDmdlParser parser;

    private final ParseException exception;

    private final Region region;

    public SyntaxErrorAnalyzer(JjDmdlParser parser, ParseException exception) {
        this.parser = parser;
        this.exception = exception;
        this.region = computeRegion();
    }

    private Region computeRegion() {
        assert exception != null;
        assert parser != null;
        Token token = parser.getToken(1);
        if (token != null && token.kind != EOF) {
            return new Region(
                    parser.getSourceFile(),
                    token.beginLine, token.beginColumn,
                    token.endLine, token.endColumn);
        }
        return new Region(parser.getSourceFile(), 0, 0, 0, 0);
    }

    public Diagnostic analyze() {
        assert exception != null;
        assert parser != null;
        Token token = parser.getToken(1);

        // Is invalid identifier?
        if ((token.kind == INVALID_IDENTIFIER_LIKE || TYPES.contains(token.kind)) && isExpected(exception, TYPES)) {
            return diagnostic(SyntaxErrorKind.INVALID_TYPE_NAME, getTokenImage(token));
        }
        if ((token.kind == INVALID_IDENTIFIER_LIKE || TYPES.contains(token.kind)) && isExpected(exception, NAMES)) {
            return diagnostic(SyntaxErrorKind.INVALID_IDENTIFIER, getTokenImage(token));
        }

        // Is the next token kind unique?
        String first = findFirstUniqueToken(exception);
        if (first != null) {
            return diagnostic(SyntaxErrorKind.UNEXPECTED_TOKEN_UNIQUE, getTokenImage(token), first);
        }

        // occurred invalid characters
        if (token.kind == UNEXPECTED || token.kind == INVALID_IDENTIFIER_LIKE) {
            return diagnostic(SyntaxErrorKind.INVALID_TOKEN, getTokenImage(token));
        }

        // May the next token be one of delimiter?
        String delimiter = findMissingDelimiterToken(exception);
        if (delimiter != null) {
            return diagnostic(SyntaxErrorKind.UNEXPECTED_TOKEN_GUESS, getTokenImage(token), delimiter);
        }

        if (token.kind == EOF) {
            return diagnostic(SyntaxErrorKind.UNEXPECTED_EOF);
        }

        return diagnostic(SyntaxErrorKind.UNEXPECTED_TOKEN_UNKNOWN, getTokenImage(token));
    }

    private Diagnostic diagnostic(SyntaxErrorKind kind, Object... arguments) {
        String location;
        if (region.beginLine >= 1 && region.beginColumn >= 1) {
            location = MessageFormat.format(
                    "{0}:{1}:{2}", //$NON-NLS-1$
                    parser.getSourceFile(),
                    region.beginLine,
                    region.beginColumn);
        } else {
            location = parser.getSourceFile().toString();
        }
        String message = MessageFormat.format(
                Messages.getString("DmdlSyntaxException.errorSyntax"), //$NON-NLS-1$
                location,
                kind.getMessage(arguments));
        return new Diagnostic(Diagnostic.Level.ERROR, region, message);
    }

    private String getTokenImage(Token token) {
        if (token.image == null || token.image.isEmpty()) {
            return exception.tokenImage[token.kind];
        } else {
            return token.image;
        }
    }

    private static boolean isExpected(ParseException exception, Collection<Integer> kinds) {
        int[][] nextTokenSequences = exception.expectedTokenSequences;
        for (int[] sequence : nextTokenSequences) {
            Integer first = getFirstTokenKind(sequence);
            if (kinds.contains(first)) {
                return true;
            }
        }
        return false;
    }

    private static String findMissingDelimiterToken(ParseException exception) {
        int[][] nextTokenSequences = exception.expectedTokenSequences;
        for (int[] sequence : nextTokenSequences) {
            Integer first = getFirstTokenKind(sequence);
            if (DELIMITERS.contains(first)) {
                return exception.tokenImage[first];
            }
        }
        return null;
    }

    private static String findFirstUniqueToken(ParseException exception) {
        int[][] nextTokenSequences = exception.expectedTokenSequences;
        if (nextTokenSequences.length == 0) {
            return null;
        }
        Integer first = getFirstTokenKind(nextTokenSequences[0]);
        if (first == null) {
            return null;
        }

        // check is constant
        if (0 > first || first >= tokenImage.length) {
            return null;
        }
        String image = tokenImage[first];
        if (image.startsWith("\"") == false || image.endsWith("\"") == false) { //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

        // check is identical
        for (int i = 1; i < nextTokenSequences.length; i++) {
            Integer other = getFirstTokenKind(nextTokenSequences[i]);
            if (first.equals(other) == false) {
                return null;
            }
        }

        return image;
    }

    private static Integer getFirstTokenKind(int[] nextTokenSequence) {
        if (nextTokenSequence.length == 0) {
            return null;
        }
        return nextTokenSequence[0];
    }
}
