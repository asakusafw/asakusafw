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
package com.asakusafw.utils.gradle;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test for command path.
 */
public class CommandPathTest {

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * test for Linux shell.
     */
    @Test
    public void linux_sh() {
        Path sh = assumeLinuxSh();
        CommandPath.launch(
                sh, Arrays.asList("-c", "touch result"),
                temporary.getRoot().toPath(),
                System.getenv());
        assertThat(Files.exists(temporary.getRoot().toPath().resolve("result")), is(true));
    }

    /**
     * test for Linux scripting.
     * @throws Exception if failed
     */
    @Test
    public void linux_script() throws Exception {
        Path sh = assumeLinuxSh();

        Path base = temporary.getRoot().toPath();
        Path script = base.resolve("script");
        Files.write(script, Arrays.asList(new String[] {
                "#!" + sh,
                "echo 'Hello, world!' > 'result'",
                "exit 0",
        }));
        Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwx------"));

        CommandPath path = new CommandPath(Arrays.asList(base));
        CommandPath.launch(
                path.find("script").get(), Collections.emptyList(),
                temporary.getRoot().toPath(),
                System.getenv());

        assertThat(Files.readAllLines(base.resolve("result")), contains("Hello, world!"));
    }

    /**
     * test for Windows CMD.
     * @throws Exception if failed
     */
    @Test
    public void windows_cmd() throws Exception {
        Path cmd = assumeWindowsCmd();

        Path base = temporary.getRoot().toPath();
        Path data = base.resolve("data");
        Files.write(data, Arrays.asList("Hello, world!"));
        CommandPath.launch(
                cmd, Arrays.asList("/c", "copy data result"),
                temporary.getRoot().toPath(),
                System.getenv());

        assertThat(Files.exists(temporary.getRoot().toPath().resolve("result")), is(true));
    }

    /**
     * test for Windows batch.
     * @throws Exception if failed
     */
    @Test
    public void windows_batch_extension() throws Exception {
        assumeWindowsCmd();

        Path base = temporary.getRoot().toPath();
        Path script = base.resolve("script.cmd");
        Files.write(script, Arrays.asList(new String[] {
                "echo Hello, world! > result",
        }));

        CommandPath path = new CommandPath(Arrays.asList(base));
        CommandPath.launch(
                path.find("script").get(), Collections.emptyList(),
                temporary.getRoot().toPath(),
                System.getenv());

        assertThat(Files.exists(temporary.getRoot().toPath().resolve("result")), is(true));
    }

    private static Path assumeLinuxSh() {
        CommandPath path = CommandPath.system(System.getenv());
        return path.find("sh")
                .map(Path::toAbsolutePath)
                .filter(it -> Optional.ofNullable(it.getFileName())
                        .map(Path::toString)
                        .filter(name -> name.contains(".") == false) // skip "sh.exe"
                        .isPresent())
                .orElseThrow(() -> new AssumptionViolatedException("should /bin/sh is available"));
    }

    private static Path assumeWindowsCmd() {
        CommandPath path = CommandPath.system(System.getenv());
        return path.find("cmd.exe")
                .orElseThrow(() -> new AssumptionViolatedException("should cmd.exe is available"));
    }
}
