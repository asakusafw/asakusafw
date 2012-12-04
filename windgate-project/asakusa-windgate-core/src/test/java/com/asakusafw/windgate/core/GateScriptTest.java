/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.windgate.core;

import static com.asakusafw.windgate.core.DriverScript.*;
import static com.asakusafw.windgate.core.ProcessScript.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

/**
 * Test for {@link GateScript}.
 */
public class GateScriptTest {

    /**
     * Test method for {@link GateScript#loadFrom(String, java.util.Properties, java.lang.ClassLoader)}.
     */
    @Test
    public void loadFrom() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        assertThat(script.getProcesses().size(), is(1));

        ProcessScript<?> test = find(script, "test");
        assertThat(test.getName(), is("test"));
        assertThat(test.getProcessType(), is("plain"));
        assertThat(test.getDataClass(), is((Object) String.class));
        assertThat(test.getSourceScript().getResourceName(), is("ts"));
        assertThat(test.getSourceScript().getConfiguration().size(), is(0));
        assertThat(test.getDrainScript().getResourceName(), is("td"));
        assertThat(test.getDrainScript().getConfiguration().size(), is(0));
    }

    /**
     * Loads source driver conf.
     */
    @Test
    public void loadFrom_sourceConf() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        p.setProperty(k("test", PREFIX_SOURCE, "aaa"), "bbb");
        p.setProperty(k("test", PREFIX_SOURCE, "bbb"), "ccc");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        assertThat(script.getProcesses().size(), is(1));

        ProcessScript<?> test = find(script, "test");
        assertThat(test.getName(), is("test"));
        assertThat(test.getProcessType(), is("plain"));
        assertThat(test.getDataClass(), is((Object) String.class));
        assertThat(test.getSourceScript().getResourceName(), is("ts"));
        assertThat(test.getSourceScript().getConfiguration().size(), is(2));
        assertThat(test.getSourceScript().getConfiguration().get("aaa"), is("bbb"));
        assertThat(test.getSourceScript().getConfiguration().get("bbb"), is("ccc"));
        assertThat(test.getDrainScript().getResourceName(), is("td"));
        assertThat(test.getDrainScript().getConfiguration().size(), is(0));
    }

    /**
     * Loads drain driver conf.
     */
    @Test
    public void loadFrom_drainConf() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        p.setProperty(k("test", PREFIX_DRAIN, "aaa"), "bbb");
        p.setProperty(k("test", PREFIX_DRAIN, "bbb"), "ccc");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        assertThat(script.getProcesses().size(), is(1));

        ProcessScript<?> test = find(script, "test");
        assertThat(test.getName(), is("test"));
        assertThat(test.getProcessType(), is("plain"));
        assertThat(test.getDataClass(), is((Object) String.class));
        assertThat(test.getSourceScript().getResourceName(), is("ts"));
        assertThat(test.getSourceScript().getConfiguration().size(), is(0));
        assertThat(test.getDrainScript().getResourceName(), is("td"));
        assertThat(test.getDrainScript().getConfiguration().size(), is(2));
        assertThat(test.getDrainScript().getConfiguration().get("aaa"), is("bbb"));
        assertThat(test.getDrainScript().getConfiguration().get("bbb"), is("ccc"));
    }

    /**
     * Loads multiple processes.
     */
    @Test
    public void loadFrom_multiple() {
        Properties p = new Properties();
        p.setProperty(k("test1", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test1", KEY_PROCESS_TYPE), "plain1");
        p.setProperty(k("test1", PREFIX_SOURCE), "ts1");
        p.setProperty(k("test1", PREFIX_DRAIN), "td1");

        p.setProperty(k("test2", KEY_DATA_CLASS), Integer.class.getName());
        p.setProperty(k("test2", KEY_PROCESS_TYPE), "plain2");
        p.setProperty(k("test2", PREFIX_SOURCE), "ts2");
        p.setProperty(k("test2", PREFIX_DRAIN), "td2");

        p.setProperty(k("test3", KEY_DATA_CLASS), Long.class.getName());
        p.setProperty(k("test3", KEY_PROCESS_TYPE), "plain3");
        p.setProperty(k("test3", PREFIX_SOURCE), "ts3");
        p.setProperty(k("test3", PREFIX_DRAIN), "td3");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        assertThat(script.getProcesses().size(), is(3));

        ProcessScript<?> test1 = find(script, "test1");
        assertThat(test1.getName(), is("test1"));
        assertThat(test1.getProcessType(), is("plain1"));
        assertThat(test1.getDataClass(), is((Object) String.class));
        assertThat(test1.getSourceScript().getResourceName(), is("ts1"));
        assertThat(test1.getSourceScript().getConfiguration().size(), is(0));
        assertThat(test1.getDrainScript().getResourceName(), is("td1"));
        assertThat(test1.getDrainScript().getConfiguration().size(), is(0));

        ProcessScript<?> test2 = find(script, "test2");
        assertThat(test2.getName(), is("test2"));
        assertThat(test2.getProcessType(), is("plain2"));
        assertThat(test2.getDataClass(), is((Object) Integer.class));
        assertThat(test2.getSourceScript().getResourceName(), is("ts2"));
        assertThat(test2.getSourceScript().getConfiguration().size(), is(0));
        assertThat(test2.getDrainScript().getResourceName(), is("td2"));
        assertThat(test2.getDrainScript().getConfiguration().size(), is(0));

        ProcessScript<?> test3 = find(script, "test3");
        assertThat(test3.getName(), is("test3"));
        assertThat(test3.getProcessType(), is("plain3"));
        assertThat(test3.getDataClass(), is((Object) Long.class));
        assertThat(test3.getSourceScript().getResourceName(), is("ts3"));
        assertThat(test3.getSourceScript().getConfiguration().size(), is(0));
        assertThat(test3.getDrainScript().getResourceName(), is("td3"));
        assertThat(test3.getDrainScript().getConfiguration().size(), is(0));
    }

    /**
     * Attempts to load properties without process type.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_missing_processtype() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        GateScript.loadFrom("testing", p, getClass().getClassLoader());
    }

    /**
     * Attempts to load properties without data class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_missing_dataclass() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        GateScript.loadFrom("testing", p, getClass().getClassLoader());
    }

    /**
     * Attempts to load properties without source.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_missing_source() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        GateScript.loadFrom("testing", p, getClass().getClassLoader());
    }

    /**
     * Attempts to load properties without drain.
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_missing_drain() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");

        GateScript.loadFrom("testing", p, getClass().getClassLoader());
    }

    /**
     * Attempts to load properties with invalid .
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_dataclass() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), "INVALID-DATA-CLASS");
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        GateScript.loadFrom("testing", p, getClass().getClassLoader());
    }

    /**
     * Attempts to load properties with invalid .
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_prefix() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");
        p.setProperty(k("test", "INVALID"), "invalid");

        GateScript.loadFrom("testing", p, getClass().getClassLoader());
    }

    /**
     * Attempts to load properties with invalid .
     */
    @Test(expected = IllegalArgumentException.class)
    public void loadFrom_invalid_key() {
        Properties p = new Properties();
        p.setProperty(k("test"), "INVALID");
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_DRAIN), "td");

        GateScript.loadFrom("testing", p, getClass().getClassLoader());
    }

    /**
     * Test method for {@link GateScript#storeTo(java.util.Properties)}.
     */
    @Test
    public void storeTo() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_SOURCE, "aaa"), "bbb");
        p.setProperty(k("test", PREFIX_DRAIN), "td");
        p.setProperty(k("test", PREFIX_DRAIN, "bbb"), "ccc");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        Properties target = new Properties();
        script.storeTo(target);
        assertThat(target, is(p));
    }

    /**
     * Stores multiple processes.
     */
    @Test
    public void storeTo_multiple() {
        Properties p = new Properties();
        p.setProperty(k("test1", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test1", KEY_PROCESS_TYPE), "plain1");
        p.setProperty(k("test1", PREFIX_SOURCE), "ts1");
        p.setProperty(k("test1", PREFIX_DRAIN), "td1");

        p.setProperty(k("test2", KEY_DATA_CLASS), Integer.class.getName());
        p.setProperty(k("test2", KEY_PROCESS_TYPE), "plain2");
        p.setProperty(k("test2", PREFIX_SOURCE), "ts2");
        p.setProperty(k("test2", PREFIX_DRAIN), "td2");

        p.setProperty(k("test3", KEY_DATA_CLASS), Long.class.getName());
        p.setProperty(k("test3", KEY_PROCESS_TYPE), "plain3");
        p.setProperty(k("test3", PREFIX_SOURCE), "ts3");
        p.setProperty(k("test3", PREFIX_DRAIN), "td3");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        Properties target = new Properties();
        script.storeTo(target);
        assertThat(target, is(p));
    }

    /**
     * Attempts to dump properties but conflict the target properties.
     */
    @Test(expected = IllegalArgumentException.class)
    public void storeTo_conflict() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_SOURCE, "aaa"), "bbb");
        p.setProperty(k("test", PREFIX_DRAIN), "td");
        p.setProperty(k("test", PREFIX_DRAIN, "bbb"), "ccc");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        Properties target = new Properties();
        target.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        script.storeTo(target);
    }

    /**
     * Target properties are not empty but not conflict for the any processes.
     */
    @Test
    public void storeTo_orthogonal() {
        Properties p = new Properties();
        p.setProperty(k("test", KEY_DATA_CLASS), String.class.getName());
        p.setProperty(k("test", KEY_PROCESS_TYPE), "plain");
        p.setProperty(k("test", PREFIX_SOURCE), "ts");
        p.setProperty(k("test", PREFIX_SOURCE, "aaa"), "bbb");
        p.setProperty(k("test", PREFIX_DRAIN), "td");
        p.setProperty(k("test", PREFIX_DRAIN, "bbb"), "ccc");

        GateScript script = GateScript.loadFrom("testing", p, getClass().getClassLoader());
        Properties target = new Properties();
        target.setProperty(k("orthogonal", KEY_DATA_CLASS), String.class.getName());
        script.storeTo(target);
    }

    private String k(String first, String...rest) {
        StringBuilder buf = new StringBuilder(first);
        for (String s : rest) {
            buf.append(GateScript.QUALIFIER);
            buf.append(s);
        }
        return buf.toString();
    }

    private ProcessScript<?> find(GateScript script, String name) {
        for (ProcessScript<?> proc : script.getProcesses()) {
            if (proc.getName().equals(name)) {
                return proc;
            }
        }
        throw new AssertionError(name);
    }
}
