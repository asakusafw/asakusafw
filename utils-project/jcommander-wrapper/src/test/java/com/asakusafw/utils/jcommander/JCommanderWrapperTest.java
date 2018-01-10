/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.jcommander;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.junit.Test;

import com.asakusafw.utils.jcommander.JCommanderWrapper;
import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * Test for {@link JCommanderWrapper}.
 */
public class JCommanderWrapperTest {

    /**
     * root command.
     */
    @Test
    public void simple() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        Optional<String> result = commander.parse().map(Supplier::get);
        assertThat(result, is(Optional.of("R")));
    }

    /**
     * w/ command.
     */
    @Test
    public void command() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addCommand("a", new Cmd("OK"));
        Optional<String> result = commander.parse("a").map(Supplier::get);
        assertThat(result, is(Optional.of("OK")));
    }

    /**
     * command help.
     */
    @Test
    public void command_help() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addCommand("a", new Cmd("OK"));
        Optional<String> result = commander.parse("a", "-h").map(Supplier::get);
        assertThat(result, is(Optional.empty()));
    }

    /**
     * root help.
     */
    @Test
    public void root_help() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addCommand("a", new Cmd("OK"));
        Optional<String> result = commander.parse("-h").map(Supplier::get);
        assertThat(result, is(Optional.empty()));
    }

    /**
     * root help w/ nested group.
     */
    @Test
    public void root_help_nested() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addGroup("g", new Cmd("G"), g -> {
            g.addCommand("a", new Cmd("OK"));
        });
        Optional<String> result = commander.parse("-h").map(Supplier::get);
        assertThat(result, is(Optional.empty()));
    }

    /**
     * w/ group.
     */
    @Test
    public void group() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addGroup("g", new Cmd("G"), g -> {
            g.addCommand("a", new Cmd("OK"));
        });
        Optional<String> result = commander.parse("g").map(Supplier::get);
        assertThat(result, is(Optional.of("G")));
    }

    /**
     * group help.
     */
    @Test
    public void group_help() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addGroup("g", new Cmd("G"), g -> {
            g.addCommand("a", new Cmd("OK"));
        });
        Optional<String> result = commander.parse("g", "--help").map(Supplier::get);
        assertThat(result, is(Optional.empty()));
    }

    /**
     * w/ nested.
     */
    @Test
    public void group_command() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addGroup("g", new Cmd("G"), g -> {
            g.addCommand("a", new Cmd("OK"));
        });
        Optional<String> result = commander.parse("g", "a").map(Supplier::get);
        assertThat(result, is(Optional.of("OK")));
    }

    /**
     * w/ nested.
     */
    @Test
    public void group_command_help() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addGroup("g", new Cmd("G"), g -> {
            g.addCommand("a", new Cmd("OK"));
        });
        Optional<String> result = commander.parse("g", "a", "--help").map(Supplier::get);
        assertThat(result, is(Optional.empty()));
    }

    /**
     * w/ DI.
     */
    @Test
    public void inject() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Inj());
        Optional<String> result = commander.parse().map(Supplier::get);
        assertThat(result, is(Optional.of("OK")));
    }

    /**
     * w/ unknown command.
     */
    @Test
    public void unknown_command() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        try {
            commander.parse("UNKNOWN").map(Supplier::get);
            fail();
        } catch (ParameterException e) {
            JCommanderWrapper.handle(e, System.out::println);
        }
    }

    /**
     * w/ unknown command.
     */
    @Test
    public void unknown_command_with_subs() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        commander.addCommand("a", new Cmd("OK"));
        try {
            commander.parse("UNKNOWN").map(Supplier::get);
            fail();
        } catch (MissingCommandException e) {
            JCommanderWrapper.handle(e, System.out::println);
        }
    }

    /**
     * w/ unknown option.
     */
    @Test
    public void unknown_option() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Cmd("R"));
        try {
            commander.parse("--unknown").map(Supplier::get);
            fail();
        } catch (MissingCommandException e) {
            throw e;
        } catch (ParameterException e) {
            JCommanderWrapper.handle(e, System.out::println);
        }
    }

    /**
     * w/ expand.
     */
    @Test
    public void expand() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Exp());
        Optional<String> result = commander.parse("-abc").map(Supplier::get);
        assertThat(result, is(Optional.of("abc")));
    }

    /**
     * w/ expand + dynamic.
     */
    @Test
    public void expand_dynamic() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Exp());
        Optional<String> result = commander.parse("-ac", "-De=f").map(Supplier::get);
        assertThat(result, is(Optional.of("ace=f")));
    }

    /**
     * w/ expand + main arg.
     */
    @Test
    public void expand_main() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Exp());
        Optional<String> result = commander.parse("-abc", "MAIN").map(Supplier::get);
        assertThat(result, is(Optional.of("abcMAIN")));
    }

    /**
     * w/ expand + escape.
     */
    @Test
    public void expand_escape() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new Exp());
        Optional<String> result = commander.parse("-a", "--", "-bc").map(Supplier::get);
        assertThat(result, is(Optional.of("a-bc")));
    }

    /**
     * w/ expand fail.
     */
    @Test(expected = ParameterException.class)
    public void expand_fail() {
        JCommanderWrapper<Supplier<String>> commander = new JCommanderWrapper<>("PG", new ExpPrevent());
        commander.parse("-aabb", "HELLO");
    }

    @Parameters(commandDescription = "CMD")
    static class Cmd implements Supplier<String> {

        @Parameter(names = { "-h", "--help" }, description = "displays help", help = true)
        boolean help = false;

        private final String message;

        Cmd(String message) {
            this.message = message;
        }

        @Override
        public String get() {
            return message;
        }
    }

    @Parameters(commandDescription = "INJ")
    static class Inj implements Supplier<String> {

        @Inject
        private JCommander commander;

        @Override
        public String get() {
            assertThat(commander, is(notNullValue()));
            return "OK";
        }
    }

    @Parameters(commandDescription = "EXP")
    static class Exp implements Supplier<String> {

        @Parameter(names = { "-a" })
        boolean a;

        @Parameter(names = { "-b" })
        boolean b;

        @Parameter(names = { "-c" })
        boolean c;

        @DynamicParameter(names = { "-D", "--dyn" })
        Map<String, String> dynamic = new TreeMap<>();

        @Parameter
        String message;

        @Override
        public String get() {
            StringBuilder buf = new StringBuilder();
            if (a) {
                buf.append("a");
            }
            if (b) {
                buf.append("b");
            }
            if (c) {
                buf.append("c");
            }
            if (dynamic.isEmpty() == false) {
                new TreeMap<>(dynamic).forEach((k, v) -> buf.append(k + "=" + v));
            }
            if (message != null) {
                buf.append(message);
            }
            return buf.toString();
        }
    }

    @Parameters(commandDescription = "EXPP")
    static class ExpPrevent implements Supplier<String> {

        @Parameter(names = { "-aa" })
        boolean a;

        @Parameter(names = { "-bb" })
        boolean b;

        @Parameter(names = { "-cc" })
        boolean c;

        @Parameter
        String message;

        @Override
        public String get() {
            StringBuilder buf = new StringBuilder();
            if (a) {
                buf.append("a");
            }
            if (b) {
                buf.append("b");
            }
            if (c) {
                buf.append("c");
            }
            if (message != null) {
                buf.append(message);
            }
            return buf.toString();
        }
    }
}
