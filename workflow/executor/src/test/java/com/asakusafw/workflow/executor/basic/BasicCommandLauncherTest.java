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
package com.asakusafw.workflow.executor.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.workflow.executor.CommandLauncher;

/**
 * Test for {@link BasicCommandLauncher}.
 */
public class BasicCommandLauncherTest {

    /**
     * temporary folder.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * simple case.
     * @throws Exception if failed.
     */
    @Test
    public void simple() throws Exception {
        Path sh = assumeLinuxSh();
        int exit = launcher().launch(sh, "-c", "touch result");
        assertThat(exit, is(0));
        assertThat(Files.exists(temporary.getRoot().toPath().resolve("result")), is(true));
    }

    /**
     * w/ stdout.
     * @throws Exception if failed.
     */
    @Test
    public void echo() throws Exception {
        Path sh = assumeLinuxSh();
        int exit = launcher().launch(sh, "-c", "echo 'Hello, world!'");
        assertThat(exit, is(0));
    }

    /**
     * w/ abnormal exit.
     * @throws Exception if failed.
     */
    @Test
    public void abend() throws Exception {
        Path sh = assumeLinuxSh();
        int exit = launcher().launch(sh, "-c", "exit 1");
        assertThat(exit, is(not(0)));
    }

    private CommandLauncher launcher() {
        return new BasicCommandLauncher(temporary.getRoot().toPath(), System.getenv());
    }

    private static Path assumeLinuxSh() {
        return Optional.of(Paths.get("/bin/sh"))
                .map(Path::toAbsolutePath)
                .filter(Files::isExecutable)
                .orElseThrow(() -> new AssumptionViolatedException("/bin/sh shoule be available"));
    }
}
