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
package com.asakusafw.dmdl;

import java.text.MessageFormat;

import com.asakusafw.dmdl.model.AstNode;

/**
 * Diagnostics of DMDL compiler.
 * @since 0.2.0
 * @version 0.5.3
 */
public class Diagnostic {

    /**
     * The level of this diagnostic.
     */
    public final Diagnostic.Level level;

    /**
     * The user friendly message.
     */
    public final String message;

    /**
     * Corresponded region, or {@code null} if unknown.
     */
    public final Region region;

    /**
     * Creates and returns a new instance.
     * @param level the level of this
     * @param region the corresponded region, or {@code null} if unknown
     * @param message the message
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.3
     */
    public Diagnostic(Diagnostic.Level level, Region region, String message) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null"); //$NON-NLS-1$
        }
        if (message == null) {
            throw new IllegalArgumentException("message must not be null"); //$NON-NLS-1$
        }
        this.level = level;
        this.message = message;
        this.region = region;
    }

    /**
     * Creates and returns a new instance.
     * @param level the level of this
     * @param region the corresponded region, or {@code null} if unknown
     * @param message the message pattern in form of {@link MessageFormat#format(String, Object...)}
     * @param arguments arguments of the message
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Diagnostic(Diagnostic.Level level, Region region, String message, Object... arguments) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null"); //$NON-NLS-1$
        }
        if (message == null) {
            throw new IllegalArgumentException("message must not be null"); //$NON-NLS-1$
        }
        this.level = level;
        this.message = MessageFormat.format(message, arguments);
        this.region = region;
    }

    /**
     * Creates and returns a new instance.
     * @param level the level of this
     * @param node the corresponded AST, or {@code null} if unknown
     * @param message the message pattern in form of {@link MessageFormat#format(String, Object...)}
     * @param arguments arguments of the message
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Diagnostic(Diagnostic.Level level, AstNode node, String message, Object... arguments) {
        this(level, node == null ? null : node.getRegion(), message, arguments);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}@{1}:{2}", //$NON-NLS-1$
                level,
                region,
                message);
    }

    /**
     * Diagnostic levels.
     */
    public enum Level {

        /**
         * Information level.
         */
        INFO,

        /**
         * Warning level.
         */
        WARN,

        /**
         * Error level.
         */
        ERROR,
    }
}