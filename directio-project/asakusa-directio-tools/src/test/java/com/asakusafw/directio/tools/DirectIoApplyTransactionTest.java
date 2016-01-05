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
package com.asakusafw.directio.tools;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.junit.Test;

/**
 * Test for {@link DirectIoApplyTransaction}.
 */
public class DirectIoApplyTransactionTest extends DirectIoCommandTestRoot {

    /**
     * applies via program entry.
     * @throws Exception if failed
     */
    @Test
    public void run() throws Exception {
        Tool exec = new DirectIoApplyTransaction(repo);

        indoubt("ex1");
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(ToolRunner.run(conf, exec, new String[] {"__UNKNOWN__"}), is(0));
        assertThat(count(production1), is(0));
        assertThat(count(production2), is(0));

        assertThat(ToolRunner.run(conf, exec, new String[] {"ex1"}), is(0));
        assertThat(count(production1), is(1));
        assertThat(count(production2), is(1));

        assertThat(ToolRunner.run(conf, exec, new String[] {"ex1"}), is(0));
    }

    /**
     * applies via program entry but failed.
     * @throws Exception if failed
     */
    @Test
    public void run_failure() throws Exception {
        Tool exec = new DirectIoApplyTransaction(repo);
        indoubt("ex1");

        writable(production1, false);
        assertThat(ToolRunner.run(conf, exec, new String[] {"ex1"}), is(not(0)));
    }

    /**
     * Invalid arguments for program entry.
     * @throws Exception if failed
     */
    @Test
    public void run_invalid() throws Exception {
        Tool exec = new DirectIoApplyTransaction(repo);
        assertThat(ToolRunner.run(conf, exec, new String[] { }), is(not(0)));
        assertThat(ToolRunner.run(conf, exec, new String[] {"1", "2"}), is(not(0)));
    }
}
