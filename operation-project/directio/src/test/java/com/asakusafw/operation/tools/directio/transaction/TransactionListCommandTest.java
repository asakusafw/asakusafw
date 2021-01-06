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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * Test for {@link TransactionListCommand}.
 */
public class TransactionListCommandTest extends DirectIoTransactionTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        addTransaction("testing", false);
        assertThat(list(), contains("testing"));
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("transaction", "list", "--help");
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        addTransaction("testing", false);
        assertThat(list("-v"), contains("testing"));
    }

    /**
     * multiple transactions.
     */
    @Test
    public void multiple() {
        addTransaction("a", true);
        addTransaction("b", false);
        addTransaction("c", true);
        assertThat(list(), contains("a", "b", "c"));
    }

    /**
     * N/A.
     */
    @Test
    public void nothing() {
        useSystemDir();
        assertThat(list(), hasSize(0));
    }

    /**
     * committed only.
     */
    @Test
    public void committed() {
        addTransaction("a", true);
        addTransaction("b", false);
        addTransaction("c", true);
        assertThat(list("--committed"), contains("a", "c"));
    }

    /**
     * un-committed only.
     */
    @Test
    public void uncommitted() {
        addTransaction("a", true);
        addTransaction("b", false);
        addTransaction("c", true);
        assertThat(list("--uncommitted"), contains("b"));
    }

    /**
     * un-committed only.
     */
    @Test
    public void both() {
        addTransaction("a", true);
        addTransaction("b", false);
        addTransaction("c", true);
        assertThat(list("--committed", "--uncommitted"), contains("a", "b", "c"));
    }

    private List<String> list(String... args) {
        List<String> all = new ArrayList<>();
        Collections.addAll(all, "transaction", "list");
        Collections.addAll(all, args);
        List<String> lines = invoke(all.toArray(new String[all.size()]));
        return lines.stream()
                .filter(it -> it.matches("^\\w+.*"))
                .sorted()
                .collect(Collectors.toList());
    }
}
