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
package com.asakusafw.operation.tools.directio.transaction;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

/**
 * Test for {@link TransactionAbortCommand}.
 */
public class TransactionAbortCommandTest extends DirectIoTransactionTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        add("root", "/");
        addTransaction("testing", false);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        invoke("transaction", "abort", "testing");
        assertThat(file, not(exists()));
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("transaction", "abort", "--help");
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        add("root", "/");
        addTransaction("testing", false);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        invoke("transaction", "abort", "testing", "-v");
        assertThat(file, not(exists()));
    }

    /**
     * w/ missing transaction.
     */
    @Test(expected = RuntimeException.class)
    public void missing() {
        useSystemDir();
        invoke("transaction", "abort", "MISSING");
    }

    /**
     * w/ missing transaction.
     */
    @Test
    public void missing_quiet() {
        useSystemDir();
        invoke("transaction", "abort", "MISSING", "--quiet");
    }

    /**
     * for committed transaction.
     */
    @Test
    public void committed() {
        add("root", "/");
        addTransaction("testing", true);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        try {
            invoke("transaction", "abort", "testing");
            fail();
        } catch(RuntimeException e) {
            e.printStackTrace();
            // ok.
        }
        assertThat(file, exists());
    }

    /**
     * for committed transaction w/ quiet.
     */
    @Test
    public void committed_quiet() {
        add("root", "/");
        addTransaction("testing", true);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        invoke("transaction", "abort", "testing", "--quiet");
        assertThat(file, exists());
    }

    /**
     * for committed transaction w/ force.
     */
    @Test
    public void committed_force() {
        add("root", "/");
        addTransaction("testing", true);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        invoke("transaction", "abort", "testing", "--force");
        assertThat(file, not(exists()));
    }
}
