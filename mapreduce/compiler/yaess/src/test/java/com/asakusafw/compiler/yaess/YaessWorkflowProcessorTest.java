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
package com.asakusafw.compiler.yaess;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.yaess.testing.batch.ComplexBatch;
import com.asakusafw.compiler.yaess.testing.batch.DiamondBatch;
import com.asakusafw.compiler.yaess.testing.batch.SimpleBatch;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.yaess.core.BatchScript;
import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.FlowScript;

/**
 * Test for {@link YaessWorkflowProcessor}.
 */
public class YaessWorkflowProcessorTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Properties p = compile(SimpleBatch.class);
        BatchScript script = BatchScript.load(p);
        assertThat(FlowScript.extractFlowIds(p), is(set("first")));

        FlowScript first = script.findFlow("first");
        assertThat(first.getId(), is("first"));
        assertThat(first.getBlockerIds(), is(set()));
        Map<ExecutionPhase, Set<ExecutionScript>> firstScripts = first.getScripts();
        assertThat(firstScripts.get(ExecutionPhase.SETUP).size(), is(0));
        assertThat(firstScripts.get(ExecutionPhase.CLEANUP).size(), is(0));
        assertThat(firstScripts.get(ExecutionPhase.INITIALIZE), hasCommands("initialize"));
        assertThat(firstScripts.get(ExecutionPhase.IMPORT), hasCommands("import"));
        assertThat(firstScripts.get(ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(firstScripts.get(ExecutionPhase.MAIN).size(), is(1));
        assertThat(firstScripts.get(ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(firstScripts.get(ExecutionPhase.EXPORT), hasCommands("export"));
        assertThat(firstScripts.get(ExecutionPhase.FINALIZE), hasCommands("finalize"));
        assertThat(firstScripts.get(ExecutionPhase.SETUP).size(), is(0));
    }

    /**
     * Complex stages.
     * @throws Exception if failed
     */
    @Test
    public void complex() throws Exception {
        Properties p = compile(ComplexBatch.class);
        BatchScript script = BatchScript.load(p);
        assertThat(FlowScript.extractFlowIds(p), is(set("last")));

        FlowScript last = script.findFlow("last");
        assertThat(last.getId(), is("last"));
        assertThat(last.getBlockerIds(), is(set()));
        Map<ExecutionPhase, Set<ExecutionScript>> lastScripts = last.getScripts();
        assertThat(lastScripts.get(ExecutionPhase.MAIN).size(), is(3));
        Set<String> blockers = new HashSet<>();
        for (ExecutionScript ex : lastScripts.get(ExecutionPhase.MAIN)) {
            blockers.addAll(ex.getBlockerIds());
        }
        assertThat(blockers.size(), is(greaterThan(0)));
    }

    /**
     * Complex flows.
     * @throws Exception if failed
     */
    @Test
    public void diamond() throws Exception {
        Properties p = compile(DiamondBatch.class);
        BatchScript script = BatchScript.load(p);

        assertThat(FlowScript.extractFlowIds(p), is(set("first", "left", "right", "last")));

        FlowScript first = script.findFlow("first");
        FlowScript left = script.findFlow("left");
        FlowScript right = script.findFlow("right");
        FlowScript last = script.findFlow("last");

        assertThat(first.getId(), is("first"));
        assertThat(left.getId(), is("left"));
        assertThat(right.getId(), is("right"));
        assertThat(last.getId(), is("last"));

        assertThat(first.getBlockerIds(), is(set()));
        assertThat(left.getBlockerIds(), is(set("first")));
        assertThat(right.getBlockerIds(), is(set("first")));
        assertThat(last.getBlockerIds(), is(set("left", "right")));
    }

    private Matcher<Set<ExecutionScript>> hasCommands(String... executables) {
        Set<String> expected = new TreeSet<>();
        Collections.addAll(expected, executables);
        return new BaseMatcher<Set<ExecutionScript>>() {
            @Override
            public boolean matches(Object target) {
                if ((target instanceof Set<?>) == false) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                Set<ExecutionScript> scripts = (Set<ExecutionScript>) target;
                Set<String> actual = new TreeSet<>();
                for (ExecutionScript ex : scripts) {
                    if ((ex instanceof CommandScript) == false) {
                        return false;
                    }
                    CommandScript cs = (CommandScript) ex;
                    actual.add(cs.getCommandLineTokens().get(0));
                }
                return actual.equals(expected);
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText("executables of ");
                desc.appendValue(expected);
            }
        };
    }

    private Set<String> set(String... values) {
        return new TreeSet<>(Arrays.asList(values));
    }

    private Properties compile(Class<? extends BatchDescription> batchClass) throws IOException {
        File output = folder.newFolder("output");
        DirectBatchCompiler.compile(
                batchClass,
                "com.example",
                Location.fromPath("testing", '/'),
                output,
                folder.newFolder("working"),
                Collections.emptyList(),
                getClass().getClassLoader(),
                new FlowCompilerOptions());
        File script = YaessWorkflowProcessor.getScriptOutput(output);
        assertThat(script.isFile(), is(true));
        try (FileInputStream in = new FileInputStream(script)) {
            Properties result = new Properties();
            result.load(in);
            return result;
        }
    }
}
