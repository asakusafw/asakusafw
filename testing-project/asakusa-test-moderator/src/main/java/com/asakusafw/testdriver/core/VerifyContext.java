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
package com.asakusafw.testdriver.core;

import java.util.Date;

/**
 * Verification context.
 * @since 0.2.0
 */
public class VerifyContext {

    private final TestContext testContext;

    private final Date testStarted;

    private volatile Date testFinished;

    /**
     * Creates a new instance.
     * @param testContext the current test context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyContext(TestContext testContext) {
        this(testContext, new Date());
    }

    /**
     * Creates a new instance.
     * @param testContext the current test context
     * @param testStarted when test was started
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyContext(TestContext testContext, Date testStarted) {
        if (testContext == null) {
            throw new IllegalArgumentException("testContext must not be null"); //$NON-NLS-1$
        }
        if (testStarted == null) {
            throw new IllegalArgumentException("testStarted must not be null"); //$NON-NLS-1$
        }
        this.testContext = testContext;
        this.testStarted = (Date) testStarted.clone();
    }

    /**
     * Returns the corresponded test context.
     * @return the test context
     */
    public TestContext getTestContext() {
        return testContext;
    }

    /**
     * Set current time as when test was finished.
     */
    public void testFinished() {
        setTestFinished(new Date());
    }

    /**
     * Set time when test was finished.
     * @param testFinished the time to set
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void setTestFinished(Date testFinished) {
        if (testFinished == null) {
            throw new IllegalArgumentException("testFinished must not be null"); //$NON-NLS-1$
        }
        this.testFinished = (Date) testFinished.clone();
    }

    /**
     * Returns the test started time.
     * @return the started time
     */
    public Date getTestStarted() {
        return (Date) testStarted.clone();
    }

    /**
     * Returns the test finished time.
     * @return the finished time
     * @throws IllegalStateException if the test have not been finished
     */
    public Date getTestFinished() {
        if (testFinished == null) {
            throw new IllegalStateException("Test have not been finished");
        }
        return (Date) testFinished.clone();
    }
}
