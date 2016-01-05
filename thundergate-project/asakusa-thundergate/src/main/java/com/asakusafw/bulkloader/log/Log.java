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
package com.asakusafw.bulkloader.log;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.asakusafw.bulkloader.exception.BulkLoaderReRunnableException;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;

/**
 * A logger wrapper for ThunderGate.
 */
public class Log {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "com.asakusafw.bulkloader.log.messages"); //$NON-NLS-1$

    private static final String LOG_MESSAGE_ID_NULL_STR = "-";

    private final Logger internal;

    /**
     * Creates a new instance.
     * @param base the base class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Log(Class<?> base) {
        if (base == null) {
            throw new IllegalArgumentException("base must not be null"); //$NON-NLS-1$
        }
        this.internal = Logger.getLogger(base);
    }

    /**
     * Log a message at DEBUG level.
     * @param format the message format
     * @param arguments the arguments
     */
    public void debugMessage(String format, Object... arguments) {
        if (internal.isDebugEnabled()) {
            setMessageCode("DEBUG");
            internal.debug(MessageFormat.format(format, arguments));
            clearMessageCode();
        }
    }

    /**
     * Log a message at INFO level.
     * @param code the message code
     * @param arguments the arguments
     */
    public void info(String code, Object... arguments) {
        if (internal.isInfoEnabled()) {
            String message = message(code, arguments);
            setMessageCode(code);
            internal.info(message);
            clearMessageCode();
        }
    }

    /**
     * Log a message at WARN level.
     * @param exception the context exception
     * @param code the message code
     * @param arguments the arguments
     */
    public void info(Throwable exception, String code, Object... arguments) {
        if (internal.isInfoEnabled()) {
            String message = message(code, arguments);
            setMessageCode(code);
            internal.info(message, exception);
            clearMessageCode();
        }
    }

    /**
     * Log a message at WARN level.
     * @param code the message code
     * @param arguments the arguments
     */
    public void warn(String code, Object... arguments) {
        String message = message(code, arguments);
        setMessageCode(code);
        internal.warn(message);
        clearMessageCode();
    }

    /**
     * Log a message at WARN level.
     * @param exception the context exception
     * @param code the message code
     * @param arguments the arguments
     */
    public void warn(Throwable exception, String code, Object... arguments) {
        String message = message(code, arguments);
        setMessageCode(code);
        internal.warn(message, exception);
        clearMessageCode();
    }

    /**
     * Log a system exception.
     * @param exception target exception
     */
    public void log(BulkLoaderReRunnableException exception) {
        String code = exception.getMessageId();
        String message = message(code, exception.getMessageArgs());
        setMessageCode(code);
        Logger.getLogger(exception.getClazz()).warn(message, exception.getCause());
        clearMessageCode();
    }

    /**
     * Log a system exception.
     * @param exception target exception
     */
    public void log(BulkLoaderSystemException exception) {
        String code = exception.getMessageId();
        String message = message(code, exception.getMessageArgs());
        try {
            setMessageCode(code);
            Logger.getLogger(exception.getClazz()).error(message, exception.getCause());
            clearMessageCode();
        } catch (RuntimeException e) {
            System.err.printf("エラーログの出力に失敗しました: %s%n", message);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Log a message at ERROR level.
     * @param code the message code
     * @param arguments the arguments
     */
    public void error(String code, Object... arguments) {
        String message = message(code, arguments);
        try {
            setMessageCode(code);
            internal.error(message);
            clearMessageCode();
        } catch (RuntimeException e) {
            System.err.printf("エラーログの出力に失敗しました: %s%n", message);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Log a message at ERROR level.
     * @param exception the context exception
     * @param code the message code
     * @param arguments the arguments
     */
    public void error(Throwable exception, String code, Object... arguments) {
        String message = message(code, arguments);
        try {
            setMessageCode(code);
            internal.error(message, exception);
            clearMessageCode();
        } catch (RuntimeException e) {
            System.err.printf("エラーログの出力に失敗しました: %s%n", message);
            e.printStackTrace();
            throw e;
        }
    }

    private String message(String code, Object... arguments) {
        String messagePattern = BUNDLE.getString(code);
        return MessageFormat.format(messagePattern, arguments);
    }

    private void setMessageCode(String messageId) {
        Timestamp logTime = new Timestamp(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        MDC.put("LOG_TSTAMP", dateFormat.format(logTime));
        MDC.put("MESSAGE_ID", messageId);
    }

    private void clearMessageCode() {
        MDC.put("MESSAGE_ID", LOG_MESSAGE_ID_NULL_STR);
    }
}
