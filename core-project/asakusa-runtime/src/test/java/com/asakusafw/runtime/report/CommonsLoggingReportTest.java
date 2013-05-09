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
package com.asakusafw.runtime.report;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.runtime.core.Report;

/**
 * Test for {@link CommonsLoggingReport}.
 */
public class CommonsLoggingReportTest {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        Report.setDelegate(new CommonsLoggingReport());
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        Report.setDelegate(null);
    }

    /**
     * Connect report API.
     */
    @Test
    public void report() {
        Report.info("1");
        Report.warn("2");
        Report.error("3");
    }
}
