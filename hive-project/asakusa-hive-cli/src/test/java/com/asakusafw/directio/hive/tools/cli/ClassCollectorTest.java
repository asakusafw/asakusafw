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
package com.asakusafw.directio.hive.tools.cli;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link ClassCollector}.
 */
public class ClassCollectorTest {

    private static final File ENTRY = new File("target/test-classes");

    /**
     * Check current test-classpath is a directory.
     */
    @BeforeClass
    public static void checkClasspath() {
        Assume.assumeTrue(ENTRY.exists());
    }

    /**
     * Collect without selection.
     */
    @Test
    public void find() {
        ClassCollector collector = new ClassCollector(getClass().getClassLoader(), aClass -> true);
        collector.inspect(ENTRY);
        assertThat(collector.getClasses().contains(getClass()), is(true));
        assertThat(collector.getClasses().contains(Dummy.class), is(true));
    }

    /**
     * Collect with selection.
     */
    @Test
    public void selector() {
        ClassCollector collector = new ClassCollector(getClass().getClassLoader(), aClass -> aClass == Dummy.class);
        collector.inspect(ENTRY);
        assertThat(collector.getClasses().contains(getClass()), is(false));
        assertThat(collector.getClasses().contains(Dummy.class), is(true));
    }

    /**
     * Collect with nothing.
     */
    @Test
    public void nothing() {
        ClassCollector collector = new ClassCollector(getClass().getClassLoader(), aClass -> false);
        collector.inspect(ENTRY);
        assertThat(collector.getClasses().contains(getClass()), is(false));
        assertThat(collector.getClasses().contains(Dummy.class), is(false));
    }

    /**
     * A dummy class for collection.
     */
    public static final class Dummy {
        // no members
    }
}
