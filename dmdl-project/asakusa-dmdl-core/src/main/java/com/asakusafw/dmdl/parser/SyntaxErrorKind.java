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
package com.asakusafw.dmdl.parser;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Represents a syntax error kind.
 * @since 0.5.3
 */
public enum SyntaxErrorKind {

    /**
     * Occurs unexpected token and the next expected token is unique.
     * <ol start="0">
     * <li> current token image </li>
     * <li> expected token </li>
     * </ol>
     */
    UNEXPECTED_TOKEN_UNIQUE,

    /**
     * Occurs unexpected token and the next expected token is guessed.
     * <ol start="0">
     * <li> current token image </li>
     * <li> expected token </li>
     * </ol>
     */
    UNEXPECTED_TOKEN_GUESS,

    /**
     * Occurs unexpected token and the next expected token is unknown.
     * <ol start="0">
     * <li> current token image </li>
     * </ol>
     */
    UNEXPECTED_TOKEN_UNKNOWN,

    /**
     * Occurs unexpected EOF.
     * <ol start="0">
     * <li> current token image </li>
     * </ol>
     */
    UNEXPECTED_EOF,

    /**
     * Occurs may invalid identifier.
     * <ol start="0">
     * <li> current token image </li>
     * </ol>
     */
    INVALID_IDENTIFIER,

    /**
     * Occurs may invalid type name.
     * <ol start="0">
     * <li> current token image </li>
     * </ol>
     */
    INVALID_TYPE_NAME,

    /**
     * Occurs unknown token.
     * <ol start="0">
     * <li> current token image </li>
     * </ol>
     */
    INVALID_TOKEN,

    ;
    private static final ResourceBundle MESSAGE_BUNDLE = ResourceBundle.getBundle(SyntaxErrorKind.class.getName());

    /**
     * Returns a diagnostic message for the arguments.
     * @param arguments the diagnostic arguments
     * @return diagnostic message
     */
    public String getMessage(Object[] arguments) {
        String format = getMessageFormat();
        return MessageFormat.format(format, arguments);
    }

    private String getMessageFormat() {
        String format = MESSAGE_BUNDLE.getString(name());
        return format;
    }
}
