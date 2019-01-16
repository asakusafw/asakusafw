/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * Test for {@link DirectIoListTransaction}.
 */
public class DirectIoListTransactionTest extends DirectIoCommandTestRoot {

    /**
     * list via program entry.
     * @throws Exception if failed
     */
    @Test
    public void run() throws Exception {
        Tool exec = new DirectIoListTransaction(repo);
        indoubt("ex1");
        indoubt("ex2");
        indoubt("ex3");

        assertThat(ToolRunner.run(conf, exec, new String[0]), is(0));

        editor.apply("ex2");
        assertThat(ToolRunner.run(conf, exec, new String[0]), is(0));

        editor.apply("ex1");
        assertThat(ToolRunner.run(conf, exec, new String[0]), is(0));

        editor.apply("ex3");
        assertThat(ToolRunner.run(conf, exec, new String[0]), is(0));
    }

    /**
     * Invalid arguments for program entry.
     * @throws Exception if failed
     */
    @Test
    public void run_invalid() throws Exception {
        Tool exec = new DirectIoListTransaction(repo);
        assertThat(ToolRunner.run(conf, exec, new String[] { "1" }), is(not(0)));
    }
}
