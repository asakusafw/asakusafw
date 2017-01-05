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
package com.asakusafw.utils.java.internal.model.util;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
%%
%class LiteralParser
%unicode
%type LiteralTokenKind
%eofval{
    return null;
%eofval}

%{
    /**
     * scanns a literal.
     * @param literal a literal to be scanned
     * @return Kind of this literal
     **/
    public static LiteralTokenKind scan(String literal) {
        if (literal == null) {
            throw new NullPointerException("literal"); //$NON-NLS-1$
        }
        LiteralParser scanner = new LiteralParser(new java.io.StringReader(literal));
        try {
            LiteralTokenKind kind = scanner.yylex();
            if (scanner.yylex() != null) {
                return LiteralTokenKind.UNKNOWN;
            }
            if (kind == LiteralTokenKind.INT) {
                char last = literal.charAt(literal.length() - 1);
                if (last == 'l' || last == 'L') {
                    return LiteralTokenKind.LONG;
                }
            }
            else if (kind == LiteralTokenKind.FLOAT) {
                char last = literal.charAt(literal.length() - 1);
                if (last != 'f' && last != 'F') {
                    return LiteralTokenKind.DOUBLE;
                }
            }
            return kind;
        }
        catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * scans a sequence of literals.
     * @param literals a sequence of literals to be scanned
     * @return A list of scanned tokens
     **/
    public static List<LiteralToken> scanSequence(String literals) {
        if (literals == null) {
            throw new NullPointerException("literals"); //$NON-NLS-1$
        }
        LiteralParser scanner = new LiteralParser(new java.io.StringReader(literals));
        try {
            List<LiteralToken> tokens = new ArrayList<LiteralToken>();
            while (true) {
                LiteralTokenKind kind = scanner.yylex();
                if (kind == null) {
                    break;
                }
                if (kind == LiteralTokenKind.UNKNOWN) {
                    tokens.add(new LiteralToken(scanner.yytext(), kind, null));
                }
                else {
                    tokens.add(LiteralAnalyzer.parse(scanner.yytext()));
                }
            }
            return tokens;
        }
        catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
%}


// [3.3], [3.10.6] Escape Sequences for Character and String Literals
ESCAPE = "\\"
    ( [btnfr\"'\\]
    | "u"+ [0-9a-fA-F]{4}
    | [0-3]? [0-7]? [0-7]
    )
EXP = [eE] [+\-]? [0-9]+
BIN_EXP = [pP] [+\-]? [0-9]+
INTEGER_SUFFIX = [lL]
REAL_SUFFIX = [fFdD]
WHITESPACE = [ \t\r\n]

INTEGER   = 
    ( "-" {WHITESPACE}* )?
    ( [1-9][0-9]*
    | "0"
      ( [xX] [0-9a-fA-F]+
      | [0-7]+
      )?
    )
    {INTEGER_SUFFIX}?

REAL      = 
    ( [0-9]+ "." [0-9]* {EXP}? {REAL_SUFFIX}?
    | "." [0-9]+ {EXP}? {REAL_SUFFIX}?
    | [0-9]+ {EXP}? {REAL_SUFFIX}
    | "0" [xX]
      ( [0-9a-fA-F]+ "."?
      | [0-9a-fA-F]* "." [0-9a-fA-F]+
      )
      {BIN_EXP} {REAL_SUFFIX}?
    )

BOOLEAN   = "true" | "false"

CHARACTER = "'" ({ESCAPE} | [^'\\]) "'"

STRING    = "\"" ({ESCAPE} | [^\"\\])* "\""

NULL      = "null"
%%

{INTEGER}   { return LiteralTokenKind.INT; }
{REAL}      { return LiteralTokenKind.FLOAT; }
{BOOLEAN}   { return LiteralTokenKind.BOOLEAN; }
{CHARACTER} { return LiteralTokenKind.CHAR; }
{STRING}    { return LiteralTokenKind.STRING; }
{NULL}      { return LiteralTokenKind.NULL; }
.|"\r"|"\n" { return LiteralTokenKind.UNKNOWN; }
