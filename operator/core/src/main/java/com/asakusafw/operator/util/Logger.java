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
package com.asakusafw.operator.util;

import java.util.Objects;
import java.util.logging.Level;

/**
 * A wrapper of logger.
 * @since 0.9.0
 */
public final class Logger {

    static final String PLACEHOLDER = "{}"; //$NON-NLS-1$

    private final java.util.logging.Logger entity;

    private Logger(java.util.logging.Logger entity) {
        this.entity = entity;
    }

    /**
     * Returns a logger for the given context class.
     * @param context the context class
     * @return the logger for the class
     */
    public static Logger get(Class<?> context) {
        Objects.requireNonNull(context);
        return new Logger(java.util.logging.Logger.getLogger(context.getName()));
    }

    /**
     * Returns whether or not TRACE level messages are enabled.
     * @return {@code true} if TRACE level messages are enabled, otherwise {@code false}
     */
    public boolean isTraceEnabled() {
        return isEnabled(Level.FINER);
    }

    /**
     * Returns whether or not DEBUG level messages are enabled.
     * @return {@code true} if DEBUG level messages are enabled, otherwise {@code false}
     */
    public boolean isDebugEnabled() {
        return isEnabled(Level.FINE);
    }

    /**
     * Returns whether or not INFO level messages are enabled.
     * @return {@code true} if INFO level messages are enabled, otherwise {@code false}
     */
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    /**
     * Returns whether or not WARN level messages are enabled.
     * @return {@code true} if WARN level messages are enabled, otherwise {@code false}
     */
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARNING);
    }

    /**
     * Returns whether or not ERROR level messages are enabled.
     * @return {@code true} if ERROR level messages are enabled, otherwise {@code false}
     */
    public boolean isErrorEnabled() {
        return isEnabled(Level.SEVERE);
    }

    /**
     * Log a message at the TRACE level.
     * @param message the message
     */
    public void trace(String message) {
        log(Level.FINER, message);
    }

    /**
     * Log a message at the TRACE level.
     * @param format the message format, which can contain argument placeholders (<code>{}</code>)
     * @param arguments message arguments
     */
    public void trace(String format, Object... arguments) {
        log(Level.FINER, format, arguments);
    }

    /**
     * Log a message at the DEBUG level.
     * @param message the message
     */
    public void debug(String message) {
        log(Level.FINE, message);
    }

    /**
     * Log a message at the DEBUG level.
     * @param format the message format, which can contain argument placeholders (<code>{}</code>)
     * @param arguments message arguments
     */
    public void debug(String format, Object... arguments) {
        log(Level.FINE, format, arguments);
    }

    /**
     * Log a message at the INFO level.
     * @param message the message
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Log a message at the INFO level.
     * @param format the message format, which can contain argument placeholders (<code>{}</code>)
     * @param arguments message arguments
     */
    public void info(String format, Object... arguments) {
        log(Level.INFO, format, arguments);
    }

    /**
     * Log a message at the WARN level.
     * @param message the message
     */
    public void warn(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Log a message at the WARN level.
     * @param format the message format, which can contain argument placeholders (<code>{}</code>)
     * @param arguments message arguments
     */
    public void warn(String format, Object... arguments) {
        log(Level.WARNING, format, arguments);
    }

    /**
     * Log a message at the ERROR level.
     * @param message the message
     */
    public void error(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Log a message at the ERROR level.
     * @param format the message format, which can contain argument placeholders (<code>{}</code>)
     * @param arguments message arguments
     */
    public void error(String format, Object... arguments) {
        log(Level.SEVERE, format, arguments);
    }

    private boolean isEnabled(Level level) {
        return entity.isLoggable(level);
    }

    private void log(Level level, String message) {
        if (isEnabled(level)) {
            entity.log(level, message);
        }
    }

    private void log(Level level, String format, Object... arguments) {
        if (isEnabled(level)) {
            String message = format(format, arguments);
            Throwable exception = extractException(arguments);
            entity.log(level, message, exception);
        }
    }

    private static String format(String format, Object[] arguments) {
        if (format.indexOf(PLACEHOLDER) < 0) {
            return format;
        }
        int offset = 0;
        int phIndex = 0;
        StringBuilder buf = new StringBuilder();
        while (true) {
            int found = format.indexOf(PLACEHOLDER, offset);
            if (found < 0) {
                break;
            }
            buf.append(format, offset, found);
            if (phIndex < arguments.length) {
                buf.append(arguments[phIndex++]);
            } else {
                buf.append(PLACEHOLDER);
            }
            offset = found + 2;
        }
        if (offset != format.length()) {
            buf.append(format, offset, format.length());
        }
        return buf.toString();
    }

    private static Throwable extractException(Object[] arguments) {
        if (arguments.length == 0) {
            return null;
        }
        Object last = arguments[arguments.length - 1];
        if (last instanceof Throwable) {
            return (Throwable) last;
        }
        return null;
    }}
