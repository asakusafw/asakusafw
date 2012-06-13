/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps logger facility.
 * @since 0.2.6
 */
public abstract class YaessLogger {

    private final Logger internal;

    private final MessageFormat format = new MessageFormat("[YS-{0}-{1}] {2}");

    private final String componentName;

    /**
     * Creates a new instance.
     * @param target target classes
     * @param componentName the target component name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public YaessLogger(Class<?> target, String componentName) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        if (componentName == null) {
            throw new IllegalArgumentException("componentName must not be null"); //$NON-NLS-1$
        }
        this.componentName = componentName;
        this.internal = LoggerFactory.getLogger(target);
    }

    /**
     * Log a message at INFO level.
     * @param code the message code
     * @param arguments the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void info(String code, Object... arguments) {
        if (internal.isInfoEnabled()) {
            String message = message(code, arguments);
            internal.info(message);
        }
    }

    /**
     * Log a message at WARN level.
     * @param exception the context exception
     * @param code the message code
     * @param arguments the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void info(Exception exception, String code, Object... arguments) {
        if (internal.isInfoEnabled()) {
            String message = message(code, arguments);
            internal.info(message, exception);
        }
    }

    /**
     * Log a message at WARN level.
     * @param code the message code
     * @param arguments the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void warn(String code, Object... arguments) {
        if (internal.isWarnEnabled()) {
            String message = message(code, arguments);
            internal.warn(message);
        }
    }

    /**
     * Log a message at WARN level.
     * @param exception the context exception
     * @param code the message code
     * @param arguments the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void warn(Exception exception, String code, Object... arguments) {
        if (internal.isWarnEnabled()) {
            String message = message(code, arguments);
            internal.warn(message, exception);
        }
    }

    /**
     * Log a message at ERROR level.
     * @param code the message code
     * @param arguments the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void error(String code, Object... arguments) {
        if (internal.isErrorEnabled()) {
            String message = message(code, arguments);
            internal.error(message);
        }
    }

    /**
     * Log a message at ERROR level.
     * @param exception the context exception
     * @param code the message code
     * @param arguments the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void error(Exception exception, String code, Object... arguments) {
        if (internal.isErrorEnabled()) {
            String message = message(code, arguments);
            internal.error(message, exception);
        }
    }

    private String message(String code, Object... arguments) {
        assert code != null;
        assert arguments != null;
        String message = getMessage(code, arguments);
        return format.format(new Object[] { componentName, code, message });
    }

    /**
     * Returns the message corrensponded to the code.
     * @param code message code
     * @param arguments message arguments
     * @return the message
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected abstract String getMessage(String code, Object... arguments);
}
