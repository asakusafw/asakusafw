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
package com.asakusafw.utils.java.parser.javadoc;

import java.util.*;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.*;

@SuppressWarnings("all")
%%
%class JavadocTokenizer
%unicode
%char
%type int
%eofval{
    return eof();
%eofval}

%{
    private StringBuilder buffer = new StringBuilder();
    private List<JavadocToken> store = new ArrayList<JavadocToken>();
    
    List<JavadocToken> getStore() {
        return store;
    }

    private int token(JavadocTokenKind kind) {
        split();
        store.add(new JavadocToken(kind, yytext(), yychar));
        return 1;
    }
    
    private int append() {
        buffer.append(yytext());
        return 0;
    }
    
    private int eof() {
        split();
        return -1;
    }
    
    private void split() {
        if (buffer.length() != 0) {
            int s = yychar - buffer.length();
            store.add(new JavadocToken(JavadocTokenKind.TEXT, buffer.toString(), s));
            buffer = new StringBuilder();
        }
    }
%}
// [:jletter:] ?
LETTER =
    "\u0024"
  | [\u0041-\u005a]
  | "\u005f"
  | [\u0061-\u007a]
  | [\u00c0-\u00d6]
  | [\u00d8-\u00f6]
  | [\u00f8-\u00ff]
  | [\u0100-\u1fff]
  | [\u3040-\u318f]
  | [\u3300-\u337f]
  | [\u3400-\u3d2d]
  | [\u4e00-\u9fff]
  | [\uf900-\ufaff]

WHITE_SPACES  = [ \t]+
LINE_BREAK    = (\r\n|\r|\n)
ASTERISK      = "*"
IDENTIFIER    = {LETTER} ( {LETTER} | [0-9] )*
AT            = "@"
DOT           = "."
COMMA         = ","
SHARP         = "#"
LEFT_BRACKET  = "["
RIGHT_BRACKET = "]"
LEFT_BRACE    = "{"
RIGHT_BRACE   = "}"
LEFT_PAREN    = "("
RIGHT_PAREN   = ")"
LESS          = "<"
GREATER       = ">"
SLASH         = "/"
QUESTION      = "?"
TEXT          = .

%%
{WHITE_SPACES}  { return token(JavadocTokenKind.WHITE_SPACES); }
{LINE_BREAK}    { return token(JavadocTokenKind.LINE_BREAK); }
{ASTERISK}      { return token(JavadocTokenKind.ASTERISK); }
{IDENTIFIER}    { return token(JavadocTokenKind.IDENTIFIER); }
{AT}            { return token(JavadocTokenKind.AT); }
{DOT}           { return token(JavadocTokenKind.DOT); }
{COMMA}         { return token(JavadocTokenKind.COMMA); }
{SHARP}         { return token(JavadocTokenKind.SHARP); }
{LEFT_BRACKET}  { return token(JavadocTokenKind.LEFT_BRACKET); }
{RIGHT_BRACKET} { return token(JavadocTokenKind.RIGHT_BRACKET); }
{LEFT_BRACE}    { return token(JavadocTokenKind.LEFT_BRACE); }
{RIGHT_BRACE}   { return token(JavadocTokenKind.RIGHT_BRACE); }
{LEFT_PAREN}    { return token(JavadocTokenKind.LEFT_PAREN); }
{RIGHT_PAREN}   { return token(JavadocTokenKind.RIGHT_PAREN); }
{LESS}          { return token(JavadocTokenKind.LESS); }
{GREATER}       { return token(JavadocTokenKind.GREATER); }
{SLASH}         { return token(JavadocTokenKind.SLASH); }
{QUESTION}      { return token(JavadocTokenKind.QUESTION); }
{TEXT}          { return append(); }
