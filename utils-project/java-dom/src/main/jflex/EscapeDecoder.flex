/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

@SuppressWarnings("all")
%%
%class EscapeDecoder
%unicode
%integer
%{
	private StringBuilder builder = new StringBuilder();
	
	/**
	 * scanns string or character literal.
	 * @param literal a literal to be scanned
	 * @return scanned runtime data
	 **/
	public static String scan(String literal) {
		if (literal == null) {
			throw new NullPointerException("literal"); //$NON-NLS-1$
		}
		EscapeDecoder scanner = new EscapeDecoder(new java.io.StringReader(literal));
		try {
			while (scanner.yylex() != YYEOF) {
				// do nothing
			}
        }
		catch (java.io.IOException e) {
			throw new IllegalStateException();
		}
		return scanner.builder.toString();
	}
	
	private void addChar(int c) {
		this.builder.append((char) c);
	}
%}

// [3.3]
UNICODE_ESCAPE = "\\" "u"+ [0-9a-fA-F]{4}

// [3.10.6] Escape Sequences for Character and String Literals
BACKSPACE      = "\\b"
HORIZONTALTAB  = "\\t"
LINEFEED       = "\\n"
FORMFEED       = "\\f"
CARRIAGERETURN = "\\r"
OCTAL_ESCAPE   = "\\" [0-3]? [0-7]{1,2}

%%
{BACKSPACE}      { addChar('\b'); }
{HORIZONTALTAB}  { addChar('\t'); }
{LINEFEED}       { addChar('\n'); }
{FORMFEED}       { addChar('\f'); }
{CARRIAGERETURN} { addChar('\r'); }
{OCTAL_ESCAPE}   { addChar(Integer.parseInt(yytext().substring(1), 8)); }
{UNICODE_ESCAPE} {
                   String text = yytext();
                   int length = text.length(); 
                   text = text.substring(length - 4, length);
                   addChar(Integer.parseInt(text, 16));
                 }
"\\" .           { addChar(yytext().charAt(1)); }
.|"\r"|"\n" { addChar(yytext().charAt(0)); }

