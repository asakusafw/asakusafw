/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.util;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An implementation of {@link EmitContext} for writing into {@link PrintWriter}.
 */
public class PrintEmitContext implements EmitContext {

    private static final String INDENT = "    "; //$NON-NLS-1$

    private final PrintWriter writer;

    private State state;

    private int indentation;

    private boolean inDocComment;

    private boolean inComment;

    private int column;

    private int bodyColumn;

    private final SortedMap<Integer, String> commentPool;

    /**
     * Creates a new instance.
     * @param writer the target writer
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public PrintEmitContext(PrintWriter writer) {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        this.writer = writer;
        this.state = State.INIT;
        this.indentation = 0;
        this.inDocComment = false;
        this.column = 0;
        this.bodyColumn = 0;
        this.commentPool = new TreeMap<Integer, String>();
    }

    @Override
    public void flushComments() {
        flushComments(commentPool);
    }

    @Override
    public void flushComments(int location) {
        SortedMap<Integer, String> head = commentPool.headMap(location);
        flushComments(head);
    }

    private void flushComments(SortedMap<Integer, String> comments) {
        assert comments != null;
        if (comments.isEmpty()) {
            return;
        }
        inComment = true;
        Iterator<Entry<Integer, String>> iter = comments.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, String> next = iter.next();
            before(State.BLOCK_COMMENT);
            putToken(next.getValue());
            iter.remove();
        }
        inComment = false;
    }

    @Override
    public void keyword(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("keyword must not be null"); //$NON-NLS-1$
        }
        before(State.KEYWORD);
        putToken(keyword);
    }

    @Override
    public void symbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        before(State.SYMBOL);
        putToken(symbol);
    }

    @Override
    public void immediate(String immediate) {
        if (immediate == null) {
            throw new IllegalArgumentException("immediate must not be null"); //$NON-NLS-1$
        }
        before(State.IMMEDIATE);
        putToken(immediate);
    }

    @Override
    public void operator(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        before(State.OPERATOR);
        putToken(symbol);
    }

    @Override
    public void separator(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
        }
        before(State.SEPARATOR);
        putToken(symbol);
    }

    @Override
    public void padding() {
        before(State.PADDING);
    }

    @Override
    public void comment(int location, String content) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null"); //$NON-NLS-1$
        }
        commentPool.put(location, content);
    }

    @Override
    public void classBlock(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        genericBlock(direction);
    }

    @Override
    public void arrayInitializerBlock(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        if (direction == EmitDirection.BEGIN) {
            before(State.SYMBOL);
            putToken("{"); //$NON-NLS-1$
            push();
        } else {
            pop();
            before(State.SEPARATOR);
            putToken("}"); //$NON-NLS-1$
        }
    }

    @Override
    public void statementBlock(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        genericBlock(direction);
    }

    @Override
    public void switchLabel(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        if (direction == EmitDirection.BEGIN) {
            push();
        } else {
            pop();
        }
    }

    @Override
    public void statement(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        if (direction == EmitDirection.END) {
            before(State.LINE_END);
        }
    }

    @Override
    public void declaration(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        if (direction == EmitDirection.END) {
            before(State.LINE_END);
        }
    }

    @Override
    public void docComment(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        if (direction == EmitDirection.BEGIN) {
            before(State.LINE_END);
            before(State.BLOCK_START);
            putToken("/**"); //$NON-NLS-1$
            this.inDocComment = true;
        } else {
            this.inDocComment = false;
            before(State.BLOCK_END);
            putToken(" */"); //$NON-NLS-1$
        }
    }

    @Override
    public void docBlock(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        statement(direction);
    }

    @Override
    public void docInlineBlock(EmitDirection direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        if (direction == EmitDirection.BEGIN) {
            before(State.SYMBOL);
            putToken("{"); //$NON-NLS-1$
        } else {
            before(State.SYMBOL);
            putToken("}"); //$NON-NLS-1$
        }
    }

    @Override
    public void putBlockComment(List<String> contents) {
        if (contents == null) {
            throw new IllegalArgumentException("contents must not be null"); //$NON-NLS-1$
        }
        before(State.BLOCK_COMMENT);
        inComment = true;
        putToken("/*"); //$NON-NLS-1$
        before(State.LINE_END);
        for (String line : contents) {
            before(State.BLOCK_COMMENT);
            putToken(" * "); //$NON-NLS-1$
            putToken(line);
            before(State.LINE_END);
        }
        before(State.BLOCK_COMMENT);
        putToken(" */"); //$NON-NLS-1$
        inComment = false;
        before(State.LINE_END);
    }

    @Override
    public void putLineComment(String content) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null"); //$NON-NLS-1$
        }
        before(State.BLOCK_COMMENT);
        inComment = true;
        putToken("// "); //$NON-NLS-1$
        putToken(content);
        inComment = false;
        before(State.LINE_END);
    }

    @Override
    public void putInlineComment(String content) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null"); //$NON-NLS-1$
        }
        before(State.INLINE_COMMENT);
        inComment = true;
        putToken("/* "); //$NON-NLS-1$
        putToken(content);
        putToken(" */"); //$NON-NLS-1$
        inComment = false;
        before(State.SEPARATOR);
    }

    private void genericBlock(EmitDirection direction) {
        assert direction != null;
        if (direction == EmitDirection.BEGIN) {
            before(State.BLOCK_START);
            putToken("{"); //$NON-NLS-1$
            push();
        } else {
            pop();
            before(State.BLOCK_END);
            putToken("}"); //$NON-NLS-1$
        }
    }

    private void before(State next) {
        assert next != null;
        State prev = state;
        state = next;
        switch (prev) {
        case INIT: {
            if (next == State.LINE_END) {
                state = State.INIT;
            }
            break;
        }
        case IMMEDIATE:
        case KEYWORD: {
            if (next != State.PADDING
                    && next != State.SYMBOL
                    && next != State.SEPARATOR
                    && next != State.LINE_END) {
                putPadding();
            }
            break;
        }
        case OPERATOR: {
            if (next != State.PADDING
                    && next != State.SEPARATOR
                    && next != State.LINE_END) {
                putPadding();
            }
            break;
        }
        case SEPARATOR: {
            if (next != State.PADDING
                    && next != State.SYMBOL
                    && next != State.SEPARATOR
                    && next != State.LINE_END) {
                putPadding();
            }
            break;
        }
        case SYMBOL: {
            // do nothing
            break;
        }
        case PADDING: {
            if (next != State.PADDING) {
                putPadding();
            }
            break;
        }
        case BLOCK_START: {
            if (next != State.LINE_END) {
                putLineBreak();
            }
            break;
        }
        case BLOCK_END: {
            if (next == State.SEPARATOR) {
                state = State.LINE_END;
            } else if (next == State.BLOCK_START) {
                state = State.LINE_END;
            } else if (next != State.LINE_END) {
                putLineBreak();
            }
            break;
        }
        case BLOCK_COMMENT: {
            if (next != State.LINE_END) {
                putLineBreak();
            }
            break;
        }
        case INLINE_COMMENT: {
            if (next != State.LINE_END) {
                putPadding();
            }
            break;
        }
        case LINE_END: {
            if (next != State.LINE_END) {
                putLineBreak();
            }
            break;
        }
        default:
            throw new AssertionError(prev);
        }
    }

    private void push() {
        indentation++;
    }

    private void pop() {
        indentation--;
    }

    private void putToken(String token) {
        assert token != null;
        int length = token.length();
        if (inComment == false
                && column + length > 120
                && bodyColumn + length > 80) {
            putLineBreak(true);
        }
        writer.print(token);
        column += length;
        bodyColumn += length;
    }

    private void putPadding() {
        writer.print(" "); //$NON-NLS-1$
        column += 1;
        bodyColumn += 1;
    }

    private void putLineBreak() {
        putLineBreak(false);
    }

    private void putLineBreak(boolean wrap) {
        writer.println();
        column = 0;
        for (int i = 0; i < indentation; i++) {
            writer.print(INDENT);
            column += INDENT.length();
        }
        if (wrap) {
            writer.print(INDENT);
            writer.print(INDENT);
            column += INDENT.length() * 2;
        }
        if (inDocComment) {
            writer.print(" * "); //$NON-NLS-1$
            column += 3;
            bodyColumn += 3;
        }
    }

    private enum State {

        /**
         * The initial state.
         */
        INIT,

        /**
         * After emit symbols.
         */
        SYMBOL,

        /**
         * After emit immediate.
         */
        IMMEDIATE,

        /**
         * After emit keywords.
         */
        KEYWORD,

        /**
         * After emit operators.
         */
        OPERATOR,

        /**
         * After emit separators.
         */
        SEPARATOR,

        /**
         * After emit paddings.
         */
        PADDING,

        /**
         * After emit block begin symbols.
         */
        BLOCK_START,

        /**
         * After emit block end symbols.
         */
        BLOCK_END,

        /**
         * After emit block comments.
         */
        BLOCK_COMMENT,

        /**
         * After emit inline comments.
         */
        INLINE_COMMENT,

        /**
         * End of line (before line break).
         */
        LINE_END,
    }
}
