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
package com.asakusafw.operator.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.logging.Level;

import org.junit.Test;

/**
 * Test for {@link Logger}.
 */
public class LoggerTest {

    static final java.util.logging.Logger ORIGIN = java.util.logging.Logger.getLogger(LoggerTest.class.getName());

    /**
     * check enabled.
     */
    @Test
    public void enabled() {
        Logger logger = Logger.get(LoggerTest.class);
        assertThat(logger.isTraceEnabled(), is(ORIGIN.isLoggable(Level.FINER)));
        assertThat(logger.isDebugEnabled(), is(ORIGIN.isLoggable(Level.FINE)));
        assertThat(logger.isInfoEnabled(), is(ORIGIN.isLoggable(Level.INFO)));
        assertThat(logger.isWarnEnabled(), is(ORIGIN.isLoggable(Level.WARNING)));
        assertThat(logger.isErrorEnabled(), is(ORIGIN.isLoggable(Level.SEVERE)));
    }

    /**
     * log w/ message only.
     */
    @Test
    public void log_simple() {
        Logger logger = Logger.get(LoggerTest.class);
        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
    }

    /**
     * log w/ format.
     */
    @Test
    public void log_format() {
        Logger logger = Logger.get(LoggerTest.class);
        logger.trace("trace: {}", 1);
        logger.debug("debug: {}", 2);
        logger.info("info: {}", 3);
        logger.warn("warn: {}", 4);
        logger.error("error: {}", 5);
    }

    /**
     * log w/ multiple arguments.
     */
    @Test
    public void log_format_multi() {
        Logger logger = Logger.get(LoggerTest.class);
        logger.trace("trace: -{}-{}-{}-", 1, 2, 3);
        logger.debug("debug: -{}-{}-{}-", 4, 5, 6);
        logger.info("info: -{}-{}-{}-", 7, 8, 9);
        logger.warn("warn: -{}-{}-{}-", 10, 11, 12);
        logger.error("error: -{}-{}-{}-", 13, 14, 15);
    }

    /**
     * log w/ exception.
     */
    @Test
    public void log_exception() {
        Logger logger = Logger.get(LoggerTest.class);
        logger.trace("trace w/ exc: {}", 1, new Exception());
        logger.debug("debug w/ exc: {}", 2, new Exception());
        logger.info("info w/ exc: {}", 3, new Exception());
        logger.warn("warn w/ exc: {}", 4, new Exception());
        logger.error("error w/ exc: {}", 5, new Exception());
    }

    /**
     * log w/ less arguments.
     */
    @Test
    public void log_less_arguments() {
        Logger logger = Logger.get(LoggerTest.class);
        logger.trace("trace: {}", new Object[0]);
        logger.debug("debug: {}", new Object[0]);
        logger.info("info: {}", new Object[0]);
        logger.warn("warn: {}", new Object[0]);
        logger.error("error: {}", new Object[0]);
    }
}
