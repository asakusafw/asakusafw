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
package com.asakusafw.testdriver.windgate.inprocess;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.asakusafw.testdriver.windgate.inprocess.model.Line;
import com.asakusafw.testdriver.windgate.inprocess.testing.io.LineStreamSupport;
import com.asakusafw.windgate.cli.ExecutionKind;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.core.vocabulary.StreamProcess;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Test for {@link InProcessWindGateProcessTaskExecutor}.
 */
public class InProcessWindGateProcessTaskExecutorTest extends InProcessWindGateTaskExecutorTestRoot {

    private final AtomicInteger counter = new AtomicInteger();

    /**
     * accepts - simple case.
     */
    @Test
    public void accepts() {
        TaskExecutor executor = new InProcessWindGateProcessTaskExecutor();
        boolean result = executor.isSupported(
                context,
                command(ExecutionKind.ONESHOT, "dummy.properties"));
        assertThat(result, is(true));
    }

    /**
     * accepts - invalid command path.
     */
    @Test
    public void accepts_invalid_path() {
        TaskExecutor executor = new InProcessWindGateProcessTaskExecutor();
        boolean result = executor.isSupported(
                context,
                command("INVALID.sh",
                        PROFILE,
                        ExecutionKind.BEGIN.symbol,
                        "dummy.properties",
                        getContext().getBatchId(),
                        getContext().getFlowId(),
                        getContext().getExecutionId()));
        assertThat(result, is(false));
    }

    /**
     * accepts - invalid arguments.
     */
    @Test
    public void accepts_invalid_args() {
        TaskExecutor executor = new InProcessWindGateProcessTaskExecutor();
        boolean result = executor.isSupported(
                context,
                command(InProcessWindGateProcessTaskExecutor.COMMAND_SUFFIX,
                        ExecutionKind.BEGIN.symbol,
                        "dummy.properties",
                        getContext().getBatchId(),
                        getContext().getFlowId(),
                        getContext().getExecutionId()));
        assertThat(result, is(false));
    }

    /**
     * execute - simple case.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        TaskExecutor executor = new InProcessWindGateProcessTaskExecutor();
        Path input = put("aaa", "bbb", "ccc");
        String script = script(gate(input));
        executor.execute(context, command(ExecutionKind.ONESHOT, script));
        assertThat(get(input), contains("aaa", "bbb", "ccc"));
    }

    private TaskInfo command(
            ExecutionKind sessionKind,
            String script,
            String... arguments) {
        return command(
                InProcessWindGateProcessTaskExecutor.COMMAND_SUFFIX,
                PROFILE,
                sessionKind.symbol,
                script,
                getContext().getBatchId(),
                getContext().getFlowId(),
                getContext().getExecutionId(),
                Stream.of(arguments).collect(Collectors.joining(",")));
    }

    private Path put(String... lines) {
        Path base = getWorkingDir();
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            Assume.assumeNoException(e);
        }
        Path file = base.resolve(String.format("%d.txt", counter.incrementAndGet()));
        try (PrintWriter writer = new PrintWriter(file.toFile(), StandardCharsets.UTF_8.name())) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return file;
    }

    private List<String> get(Path input) {
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

    private GateScript gate(Path in) {
        return new GateScript("testing", Arrays.asList(p("p", in)));
    }

    private static ProcessScript<?> p(String name, Path in) {
        Path output = outputOf(in);
        return new ProcessScript<>(
                name, "basic", Line.class,
                d("local", in),
                d("local", output));
    }

    private static Path outputOf(Path in) {
        Path output = Paths.get(in.toString().replaceFirst("\\.txt$", "-output.txt"));
        return output;
    }

    private static DriverScript d(String name, Path file) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(FileProcess.FILE.key(), file.getFileName().toString());
        map.put(StreamProcess.STREAM_SUPPORT.key(), LineStreamSupport.class.getName());
        return new DriverScript(name, map);
    }

    private String script(GateScript script) {
        try {
            Properties properties = new Properties();
            script.storeTo(properties);
            Path file = getWorkingDir().resolve("script.properties");
            try (OutputStream output = new FileOutputStream(file.toFile())) {
                properties.store(output, null);
            }
            return file.toAbsolutePath().toUri().toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
