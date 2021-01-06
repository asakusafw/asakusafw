/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
public class TransactionApplyCommandTest extends DirectIoTransactionTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        File root = add("root", "/");
        addTransaction("testing", true);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        invoke("transaction", "apply", "testing");
        assertThat(file, not(exists()));
        assertThat(new File(root, "a.txt"), is(file()));
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("transaction", "apply", "--help");
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        File root = add("root", "/");
        addTransaction("testing", true);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        invoke("transaction", "apply", "testing", "-v");
        assertThat(file, not(exists()));
        assertThat(new File(root, "a.txt"), is(file()));
    }

    /**
     * w/ missing transaction.
     */
    @Test(expected = RuntimeException.class)
    public void missing() {
        useSystemDir();
        invoke("transaction", "apply", "MISSING");
    }

    /**
     * w/ missing transaction.
     */
    @Test
    public void missing_quiet() {
        useSystemDir();
        invoke("transaction", "apply", "MISSING", "--quiet");
    }

    /**
     * for uncommitted transaction.
     */
    @Test
    public void uncommitted() {
        File root = add("root", "/");
        addTransaction("testing", false);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        try {
            invoke("transaction", "apply", "testing");
            fail();
        } catch(RuntimeException e) {
            e.printStackTrace();
            // ok.
        }
        assertThat(file, exists());
        assertThat(new File(root, "a.txt"), not(exists()));
    }

    /**
     * for uncommitted transaction w/ quiet.
     */
    @Test
    public void committed_quiet() {
        File root = add("root", "/");
        addTransaction("testing", false);
        File stage = useStageDir("testing", "root");
        File file = touch(stage, "a.txt");
        invoke("transaction", "apply", "testing", "--quiet");
        assertThat(file, exists());
        assertThat(new File(root, "a.txt"), not(exists()));
    }
}
