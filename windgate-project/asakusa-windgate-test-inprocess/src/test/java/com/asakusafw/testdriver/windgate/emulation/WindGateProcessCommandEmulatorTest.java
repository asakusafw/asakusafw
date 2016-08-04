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
package com.asakusafw.testdriver.windgate.emulation;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testdriver.windgate.emulation.testing.io.LineStreamSupport;
import com.asakusafw.testdriver.windgate.emulation.testing.model.Line;
import com.asakusafw.windgate.bootstrap.ExecutionKind;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.core.vocabulary.StreamProcess;

/**
 * Test for {@link WindGateProcessCommandEmulator}.
 */
public class WindGateProcessCommandEmulatorTest extends WindGateCommandEmulatorTestRoot {

    private final ConfigurationFactory configurations = ConfigurationFactory.getDefault();

    private final AtomicInteger counter = new AtomicInteger();

    /**
     * accepts - simple case.
     */
    @Test
    public void accepts() {
        WindGateProcessCommandEmulator emulator = new WindGateProcessCommandEmulator();
        boolean result = emulator.accepts(
                context,
                configurations,
                command(ExecutionKind.ONESHOT, "dummy.properties"));
        assertThat(result, is(true));
    }

    /**
     * accepts - invalid command path.
     */
    @Test
    public void accepts_invalid_path() {
        WindGateProcessCommandEmulator emulator = new WindGateProcessCommandEmulator();
        boolean result = emulator.accepts(
                context,
                configurations,
                command("INVALID.sh",
                        PROFILE,
                        ExecutionKind.BEGIN.symbol,
                        "dummy.properties",
                        getContext().getCurrentBatchId(),
                        getContext().getCurrentFlowId(),
                        getContext().getCurrentExecutionId()));
        assertThat(result, is(false));
    }

    /**
     * accepts - invalid arguments.
     */
    @Test
    public void accepts_invalid_args() {
        WindGateProcessCommandEmulator emulator = new WindGateProcessCommandEmulator();
        boolean result = emulator.accepts(
                context,
                configurations,
                command(WindGateProcessCommandEmulator.COMMAND_SUFFIX,
                        ExecutionKind.BEGIN.symbol,
                        "dummy.properties",
                        getContext().getCurrentBatchId(),
                        getContext().getCurrentFlowId(),
                        getContext().getCurrentExecutionId()));
        assertThat(result, is(false));
    }

    /**
     * execute - simple case.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        WindGateProcessCommandEmulator emulator = new WindGateProcessCommandEmulator();
        File input = put("aaa", "bbb", "ccc");
        String script = script(gate(input));
        emulator.execute(context, configurations, command(ExecutionKind.ONESHOT, script));
        assertThat(get(input), contains("aaa", "bbb", "ccc"));
    }

    private TestExecutionPlan.Command command(
            ExecutionKind sessionKind,
            String script,
            String... arguments) {
        return command(
                WindGateProcessCommandEmulator.COMMAND_SUFFIX,
                PROFILE,
                sessionKind.symbol,
                script,
                getContext().getCurrentBatchId(),
                getContext().getCurrentFlowId(),
                getContext().getCurrentExecutionId(),
                Stream.of(arguments).collect(Collectors.joining(",")));
    }

    private File put(String... lines) {
        File base = getLocalBase();
        Assume.assumeTrue(base.mkdirs() || base.isDirectory());
        File file = new File(base, String.format("%d.txt", counter.incrementAndGet()));
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8.name())) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return file;
    }

    private List<String> get(File input) {
        List<String> results = new ArrayList<>();
        try (Scanner scanner = new Scanner(outputOf(input), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                results.add(scanner.nextLine());
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return results;
    }

    private GateScript gate(File in) {
        return new GateScript("testing", Arrays.asList(p("p", in)));
    }

    private ProcessScript<?> p(String name, File in) {
        File output = outputOf(in);
        return new ProcessScript<>(
                name, "basic", Line.class,
                d("local", in),
                d("local", output));
    }

    private File outputOf(File in) {
        File output = new File(in.getPath().replaceFirst("\\.txt$", "-output.txt"));
        return output;
    }

    private DriverScript d(String name, File file) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(FileProcess.FILE.key(), file.getName());
        map.put(StreamProcess.STREAM_SUPPORT.key(), LineStreamSupport.class.getName());
        return new DriverScript(name, map);
    }

    private String script(GateScript script) {
        try {
            Properties properties = new Properties();
            script.storeTo(properties);
            File file = new File(getLocalBase(), "script.properties");
            try (OutputStream output = new FileOutputStream(file)) {
                properties.store(output, null);
            }
            return file.getAbsoluteFile().toURI().toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
