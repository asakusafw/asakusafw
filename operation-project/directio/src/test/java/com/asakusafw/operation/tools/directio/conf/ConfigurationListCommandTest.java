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
package com.asakusafw.operation.tools.directio.conf;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.asakusafw.operation.tools.directio.DirectIoToolsTestRoot;

/**
 * Test for {@link ConfigurationListCommand}.
 */
public class ConfigurationListCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        add("root", "/");
        List<String> ds = invoke("configuration", "list");
        assertThat(ds, containsInAnyOrder("root"));
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("configuration", "list", "--help");
    }

    /**
     * multiple data source.
     */
    @Test
    public void multiple() {
        add("a", "a");
        add("b", "b");
        add("c", "c");
        List<String> ds = invoke("configuration", "list");
        assertThat(ds, containsInAnyOrder("a", "b", "c"));
    }

    /**
     * w/o data sources.
     */
    @Test
    public void no_ds() {
        List<String> ds = invoke("configuration", "list");
        assertThat(ds, hasSize(0));
    }

    /**
     * w/ verbose.
     */
    @Test
    public void verbose() {
        add("a", "a");
        add("b", "b");
        add("c", "c");
        invoke("configuration", "list", "-v");
    }

    /**
     * w/ data source ID.
     */
    @Test
    public void single() {
        add("a", "a");
        add("b", "b");
        add("c", "c");
        invoke("configuration", "list", "b");
    }

    /**
     * w/ invalid source ID.
     */
    @Test(expected = RuntimeException.class)
    public void missing_datasource() {
        invoke("configuration", "list", "MISSING");
    }
}
