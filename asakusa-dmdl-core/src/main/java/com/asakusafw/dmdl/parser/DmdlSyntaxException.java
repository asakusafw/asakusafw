/**
 * Copyright 2011 Asakusa Framework Team.
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

import com.asakusafw.dmdl.Region;
import com.asakusafw.dmdl.parser.JjDmdlParser.ParseFrame;

/**
 * DMDL Syntax Error.
 */
public class DmdlSyntaxException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Region region;

    /**
     * Creates and returns a new instance.
     * @param exception original {@link ParseException}
     * @param parser the parser which failed to parse
     */
    public DmdlSyntaxException(ParseException exception, JjDmdlParser parser) {
        super(buildMessage(exception, parser), exception);
        this.region = computeRegion(exception, parser);
    }

    /**
     * Returns the region on this error.
     * @return the region, or {@code null} if not known
     */
    public Region getRegion() {
        return region;
    }

    private Region computeRegion(ParseException exception, JjDmdlParser parser) {
        assert exception != null;
        assert parser != null;
        Token token = parser.getToken(1);
        if (token != null && token.kind != EOF) {
            return new Region(
                    parser.getSourceFile(),
                    token.beginLine, token.beginColumn,
                    token.endLine, token.endColumn);
        }
        return null;
    }

    private static String buildMessage(ParseException exception, JjDmdlParser parser) {
        assert exception != null;
        assert parser != null;
        ParseFrame[] frames = parser.getFrames();
        if (frames.length == 0) {
            return MessageFormat.format(
                    "Invalid DMDL Script {0}: {1}",
                    parser.getSourceFile(),
                    getReason(exception, parser));
        } else {
            ParseFrame top = frames[0];
            return MessageFormat.format(
                    "Invalid DMDL Script {0} in the grammar: {1} (in \"{2}\")",
                    parser.getSourceFile(),
                    getReason(exception, parser),
                    top.getRuleName());
        }
    }

    private static String getReason(ParseException exception, JjDmdlParser parser) {
        assert exception != null;
        assert parser != null;
        Token token = parser.getToken(1);
        if (token.kind == UNEXPECTED) {
            return MessageFormat.format(
                    "invalid token: \"{0}\"",
                    token.image);
        }
        for (int[] sequence : exception.expectedTokenSequences) {
            // may not occur
            if (sequence.length == 0) {
                continue;
            }
            int next = sequence[0];
            if (next == END_OF_DECLARATION) {
                return MessageFormat.format(
                        "missing {0}?",
                        exception.tokenImage[END_OF_DECLARATION]);
            }
        }
        if (token.kind == EOF) {
            return "unexpected EOF";
        }
        if (token.image == null || token.image.isEmpty()) {
            return exception.tokenImage[token.kind];
        } else {
            return token.image;
        }
    }
}
