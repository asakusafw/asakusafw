/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.testdriver.JobFlowTester;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.testdriver.windgate.emulation.testing.jobflow.LineJobflow;
import com.asakusafw.testdriver.windgate.emulation.testing.model.Line;
import com.asakusafw.windgate.core.resource.ResourceProfile;

/**
 * Semi-integration test for this project.
 */
public class WindGateCommandEmulatorTest {

    /**
     * Deployer.
     */
    @Rule
    public final FrameworkDeployer deployer = new FrameworkDeployer(false);

    /**
     * Simple scenario.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        // see src/test/dist/windgate/*
        deployer.copy(new File("src/test/dist"), deployer.getHome());

        JobFlowTester tester = new JobFlowTester(getClass());
        tester.setFrameworkHomePath(deployer.getHome());

        tester.input("line", Line.class).prepare(Arrays.asList(new Line("Hello, world!")));
        tester.output("line", Line.class).verify(Arrays.asList(new Line("Hello, world!")), new ModelVerifier<Line>() {
            @Override
            public Object getKey(Line target) {
                return target.getValueOption();
            }
            @Override
            public Object verify(Line expected, Line actual) {
                if (expected == null || actual == null) {
                    return "missmatch";
                }
                if (expected.getValueOption().equals(actual.getValueOption())) {
                    return null;
                }
                return "INVALID";
            }
        });

        final AtomicBoolean call = new AtomicBoolean();
        MockResourceProvider.callback(new MockResourceProvider.Callback() {
            @Override
            public void run(ResourceProfile profile) throws IOException {
                // Only if this application does not contain plug-in resources
                if (getClass().getClassLoader().getResource("testing") == null) {
                    // but the service context must have plug-in resources.
                    URL resource = profile.getContext().getClassLoader().getResource("testing");
                    assertThat("check plugin loading", resource, is(notNullValue()));
                }
                call.set(true);
            }
        });

        tester.runTest(LineJobflow.class);

        assertThat("check mock resource", call.get(), is(true));

        File base = new File(deployer.getHome(), String.format("tmp/windgate/%s", System.getProperty("user.name")));
        assertThat("check input file", new File(base, "input.txt").isFile(), is(true));
        assertThat("check output file", new File(base, "output.txt").isFile(), is(true));
    }
}
