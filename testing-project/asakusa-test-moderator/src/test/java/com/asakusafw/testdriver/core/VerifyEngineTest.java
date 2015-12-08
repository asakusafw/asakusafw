/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link VerifyEngine}.
 * @since 0.2.0
 */
public class VerifyEngineTest {

    /**
     * single perfectmatch.
     * @throws Exception if failed
     */
    @Test
    public void single() throws Exception {
        VerifyEngine engine = new VerifyEngine(new Rule());
        engine.addExpected(source("hello:world"));

        List<Difference> d1 = engine.inspectInput(source("hello:world"));
        assertThat(d1.size(), is(0));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(0));
    }

    /**
     * single value mismatch.
     * @throws Exception if failed
     */
    @Test
    public void mismatch_value() throws Exception {
        VerifyEngine engine = new VerifyEngine(new Rule());
        engine.addExpected(source("hello:world!"));

        List<Difference> d1 = engine.inspectInput(source("hello:world"));
        assertThat(d1.size(), is(1));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(0));
    }

    /**
     * single key mismatch.
     * @throws Exception if failed
     */
    @Test
    public void mismatch_key() throws Exception {
        VerifyEngine engine = new VerifyEngine(new Rule());
        engine.addExpected(source("hello!:world"));

        List<Difference> d1 = engine.inspectInput(source("hello:world"));
        assertThat(d1.size(), is(1));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(1));
    }

    /**
     * duplicated.
     * @throws Exception if failed
     */
    @Test
    public void duplicate() throws Exception {
        VerifyEngine engine = new VerifyEngine(new Rule());
        engine.addExpected(source("hello:world"));

        List<Difference> d1 = engine.inspectInput(source("hello:world1", "hello:world2"));
        assertThat(d1.size(), is(greaterThan(0)));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(0));
    }

    /**
     * duplicated.
     * @throws Exception if failed
     */
    @Test
    public void duplicate_calendar() throws Exception {
        VerifyEngine engine = new VerifyEngine(new CalendarRule());
        engine.addExpected(CalendarRule.calendars("2013-01-01"));

        List<Difference> d1 = engine.inspectInput(CalendarRule.calendars("2013-01-01", "2013-01-02"));
        assertThat(d1.size(), is(greaterThan(0)));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(0));
    }

    DataModelSource source(String... values) {
        return new IteratorDataModelSource(
                ValueDefinition.of(String.class),
                Arrays.asList(values).iterator());
    }

    static class Rule implements VerifyRule {

        private final DataModelDefinition<String> def = ValueDefinition.of(String.class);

        @Override
        public Object getKey(DataModelReflection target) {
            String string = def.toObject(target);
            String[] split = string.split(":", 2);
            return split[0];
        }

        @Override
        public Object verify(DataModelReflection expected, DataModelReflection actual) {
            if (expected == null || actual == null) {
                return "invalid";
            }
            String ex = def.toObject(expected).split(":", 2)[1];
            String ac = def.toObject(actual).split(":", 2)[1];
            return ex.equals(ac) ? null : "mismatch";
        }
    }

    static class CalendarRule implements VerifyRule {

        private static final DataModelDefinition<Calendar> DEF = ValueDefinition.of(Calendar.class);

        @Override
        public Object getKey(DataModelReflection target) {
            Calendar calendar = DEF.toObject(target);
            return calendar.get(Calendar.YEAR);
        }

        @Override
        public Object verify(DataModelReflection expected, DataModelReflection actual) {
            if (expected == null || actual == null) {
                return "invalid";
            }
            return expected.equals(actual) ? null : "mismatch";
        }

        static DataModelSource calendars(String... values) {
            List<Calendar> calendars = new ArrayList<>();
            for (String value : values) {
                try {
                    java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendars.add(calendar);
                } catch (ParseException e) {
                    throw new AssertionError(e);
                }
            }
            return new IteratorDataModelSource(
                    DEF,
                    calendars.iterator());
        }
    }
}
