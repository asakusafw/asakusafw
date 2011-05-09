/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.SpiVerifyRuleProvider;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.core.VerifyRule;
import com.asakusafw.testdriver.core.VerifyRuleProvider;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;
import com.asakusafw.testdriver.rule.DataModelCondition;

/**
 * Test for {@link ExcelSheetRuleProvider}.
 * @since 0.2.0
 */
public class ExcelSheetRuleProviderTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    /**
     * simple verification.
     * @throws Exception if occur
     */
    @Test
    public void simple() throws Exception {
        ExcelSheetRuleProvider provider = new ExcelSheetRuleProvider();
        VerifyRule rule = provider.get(SIMPLE, context(10), uri("simple.xls", ":0"));
        assertThat(rule, not(nullValue()));

        assertThat(rule.getKey(obj(100, "a")), equalTo(rule.getKey(obj(100, "b"))));
        assertThat(rule.getKey(obj(100, "a")), not(equalTo(rule.getKey(obj(200, "a")))));

        assertThat(rule.verify(obj(1, "a"), obj(2, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), not(nullValue()));
    }

    /**
     * simple verification via SPI.
     * @throws Exception if occur
     */
    @Test
    public void spi() throws Exception {
        VerifyRuleProvider provider = new SpiVerifyRuleProvider(ExcelSheetRuleProvider.class.getClassLoader());
        VerifyRule rule = provider.get(SIMPLE, context(10), uri("simple.xls", ":0"));
        assertThat(rule, not(nullValue()));

        assertThat(rule.getKey(obj(100, "a")), equalTo(rule.getKey(obj(100, "b"))));
        assertThat(rule.getKey(obj(100, "a")), not(equalTo(rule.getKey(obj(200, "a")))));

        assertThat(rule.verify(obj(1, "a"), obj(2, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), not(nullValue()));
    }

    /**
     * {@link DataModelCondition} - strict.
     * @throws Exception if occur
     */
    @Test
    public void strict() throws Exception {
        VerifyRule rule = rule("strict.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), not(nullValue()));
        assertThat(rule.verify(null, obj(1, "a")), not(nullValue()));
        assertThat(rule.verify(obj(1, "a"), null), not(nullValue()));
    }

    /**
     * {@link DataModelCondition} - ignore absent.
     * @throws Exception if occur
     */
    @Test
    public void ignore_absent() throws Exception {
        VerifyRule rule = rule("ignore_absent.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), not(nullValue()));
        assertThat(rule.verify(null, obj(1, "a")), not(nullValue()));
        assertThat(rule.verify(obj(1, "a"), null), is(nullValue()));
    }

    /**
     * {@link DataModelCondition} - ignore unexpected.
     * @throws Exception if occur
     */
    @Test
    public void ignore_unexpected() throws Exception {
        VerifyRule rule = rule("ignore_unexpected.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), not(nullValue()));
        assertThat(rule.verify(null, obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), null), not(nullValue()));
    }

    /**
     * {@link DataModelCondition} - intersect.
     * @throws Exception if occur
     */
    @Test
    public void intersect() throws Exception {
        VerifyRule rule = rule("intersect.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), not(nullValue()));
        assertThat(rule.verify(null, obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), null), is(nullValue()));
    }

    /**
     * {@link DataModelCondition} - ignore unexpected.
     * @throws Exception if occur
     */
    @Test
    public void skip() throws Exception {
        VerifyRule rule = rule("skip.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), is(nullValue()));
        assertThat(rule.verify(null, obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), null), is(nullValue()));
    }

    /**
     * undefined properties.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void name_unknown() throws Exception {
        rule("name_unknown.xls");
    }

    /**
     * empty property names.
     * @throws Exception if occur
     */
    @Test
    public void name_empty() throws Exception {
        VerifyRule rule = rule("name_empty.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(2, "b")), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - don't care.
     * @throws Exception if occur
     */
    @Test
    public void value_any() throws Exception {
        VerifyRule rule = rule("value_any.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(2, "b")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(null, null)), is(nullValue()));
        assertThat(rule.verify(null, obj(1, "a")), not(nullValue()));
        assertThat(rule.verify(obj(1, "a"), null), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - keys.
     * @throws Exception if occur
     */
    @Test
    public void value_keys() throws Exception {
        VerifyRule rule = rule("value_keys.xls");
        assertThat(rule.getKey(obj(1, "a")), equalTo(rule.getKey(obj(1, "a"))));
        assertThat(rule.getKey(obj(2, "b")), equalTo(rule.getKey(obj(2, "b"))));
        assertThat(rule.getKey(obj(1, "a")), not(equalTo(rule.getKey(obj(2, "a")))));
        assertThat(rule.getKey(obj(1, "a")), not(equalTo(rule.getKey(obj(1, "b")))));

        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(2, "b")), is(nullValue()));
        assertThat(rule.verify(null, obj(1, "a")), not(nullValue()));
        assertThat(rule.verify(obj(1, "a"), null), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - equals.
     * @throws Exception if occur
     */
    @Test
    public void value_equal() throws Exception {
        VerifyRule rule = rule("value_equal.xls");
        assertThat(rule.verify(obj(1, "a"), obj(1, "a")), is(nullValue()));
        assertThat(rule.verify(obj(2, "b"), obj(2, "b")), is(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(1, "b")), not(nullValue()));
        assertThat(rule.verify(obj(1, "a"), obj(2, "a")), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - contain.
     * @throws Exception if occur
     */
    @Test
    public void value_contain() throws Exception {
        VerifyRule rule = rule("value_contain.xls");
        assertThat(rule.verify(obj(0, "ab"), obj(0, "ab")), is(nullValue()));
        assertThat(rule.verify(obj(0, "ab"), obj(0, "aba")), is(nullValue()));
        assertThat(rule.verify(obj(0, "ab"), obj(0, "aca")), not(nullValue()));
        assertThat(rule.verify(obj(0, "ab"), obj(0, "a")), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - contain for not a string property.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void value_contain_error() throws Exception {
        rule("value_contain_error.xls");
    }

    /**
     * {@link ValueConditionKind} - today.
     * @throws Exception if occur
     */
    @Test
    public void value_today() throws Exception {
        // 2011/03/31 23:00:00 -> 23:30:00
        VerifyContext context = context(30);
        VerifyRule rule = rule("value_today.xls", context);

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2011, 2, 30);
        assertThat(rule.verify(obj(0, ""), date(calendar)), not(nullValue()));
        calendar.set(2011, 2, 31);
        assertThat(rule.verify(obj(0, ""), date(calendar)), is(nullValue()));
        calendar.set(2011, 3, 1);
        assertThat(rule.verify(obj(0, ""), date(calendar)), not(nullValue()));
        calendar.set(2011, 3, 2);
        assertThat(rule.verify(obj(0, ""), date(calendar)), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - today (but test was started yesterday).
     * @throws Exception if occur
     */
    @Test
    public void value_today_started_yesterday() throws Exception {
        // 2011/03/31 23:00:00 -> 2011/04/01 0:30:00
        VerifyContext context = context(90);
        VerifyRule rule = rule("value_today.xls", context);

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2011, 2, 30);
        assertThat(rule.verify(obj(0, ""), date(calendar)), not(nullValue()));
        calendar.set(2011, 2, 31);
        assertThat(rule.verify(obj(0, ""), date(calendar)), is(nullValue()));
        calendar.set(2011, 3, 1);
        assertThat(rule.verify(obj(0, ""), date(calendar)), is(nullValue()));
        calendar.set(2011, 3, 2);
        assertThat(rule.verify(obj(0, ""), date(calendar)), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - today for not a date/datetime property.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void value_today_error() throws Exception {
        rule("value_today_error.xls");
    }

    /**
     * {@link ValueConditionKind} - now.
     * @throws Exception if occur
     */
    @Test
    public void value_now() throws Exception {
        // 2011/03/31 23:00:00 -> 23:30:00
        VerifyContext context = context(30);
        VerifyRule rule = rule("value_now.xls", context);

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2011, 2, 31, 0, 0, 0);
        assertThat(rule.verify(obj(0, ""), datetime(calendar)), not(nullValue()));
        calendar.set(2011, 2, 31, 22, 59, 59);
        assertThat(rule.verify(obj(0, ""), datetime(calendar)), not(nullValue()));
        calendar.set(2011, 2, 31, 23,  0,  0);
        assertThat(rule.verify(obj(0, ""), datetime(calendar)), is(nullValue()));
        calendar.set(2011, 2, 31, 23, 15,  0);
        assertThat(rule.verify(obj(0, ""), datetime(calendar)), is(nullValue()));
        calendar.set(2011, 2, 31, 23, 30, 00);
        assertThat(rule.verify(obj(0, ""), datetime(calendar)), is(nullValue()));
        calendar.set(2011, 2, 31, 23, 30, 01);
        assertThat(rule.verify(obj(0, ""), datetime(calendar)), not(nullValue()));
        calendar.set(2011, 2, 31, 23, 45, 00);
        assertThat(rule.verify(obj(0, ""), datetime(calendar)), not(nullValue()));
    }

    /**
     * {@link ValueConditionKind} - now for not a datetime property.
     * @throws Exception if occur
     */
    @Test(expected = IOException.class)
    public void value_now_error() throws Exception {
        rule("value_now_error.xls");
    }

    /**
     * {@link NullityConditionKind} - normal checking.
     * @throws Exception if occur
     */
    @Test
    public void nullity_normal() throws Exception {
        VerifyRule rule = rule("nullity_normal.xls");
        assertThat(rule.verify(obj(0, "a"), obj(0, "a")), is(nullValue()));
        assertThat(rule.verify(obj(null, "a"), obj(null, "a")), is(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(0, null)), is(nullValue()));
        assertThat(rule.verify(obj(0, "a"), obj(null, "a")), not(nullValue()));
        assertThat(rule.verify(obj(0, "a"), obj(0, null)), is(nullValue()));
    }

    /**
     * {@link NullityConditionKind} - accept absent.
     * @throws Exception if occur
     */
    @Test
    public void nullity_AA() throws Exception {
        VerifyRule rule = rule("nullity_AA.xls");
        assertThat(rule.verify(obj(0, "a"), obj(0, "a")), not(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(0, "a")), not(nullValue()));
        assertThat(rule.verify(obj(0, "a"), obj(0, null)), is(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(0, null)), is(nullValue()));
    }

    /**
     * {@link NullityConditionKind} - accept present.
     * @throws Exception if occur
     */
    @Test
    public void nullity_AP() throws Exception {
        VerifyRule rule = rule("nullity_AP.xls");
        assertThat(rule.verify(obj(0, "a"), obj(0, "a")), is(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(0, "a")), is(nullValue()));
        assertThat(rule.verify(obj(0, "a"), obj(0, null)), not(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(0, null)), not(nullValue()));
    }

    /**
     * {@link NullityConditionKind} - deny present.
     * @throws Exception if occur
     */
    @Test
    public void nullity_DP() throws Exception {
        VerifyRule rule = rule("nullity_DP.xls");
        assertThat(rule.verify(obj(0, "a"), obj(null, "a")), not(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(null, "a")), not(nullValue()));
        assertThat(rule.verify(obj(0, "a"), obj(null, null)), is(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(null, null)), is(nullValue()));

        assertThat(rule.verify(obj(0, null), obj(0, null)), not(nullValue()));
    }

    /**
     * {@link NullityConditionKind} - deny absent.
     * @throws Exception if occur
     */
    @Test
    public void nullity_DA() throws Exception {
        VerifyRule rule = rule("nullity_DA.xls");
        assertThat(rule.verify(obj(0, "a"), obj(0, "a")), is(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(0, "a")), is(nullValue()));
        assertThat(rule.verify(obj(0, "a"), obj(0, null)), not(nullValue()));
        assertThat(rule.verify(obj(0, null), obj(0, null)), not(nullValue()));

        assertThat(rule.verify(obj(null, "a"), obj(null, "a")), not(nullValue()));
    }

    private DataModelReflection obj(Integer number, String text) {
        Simple simple = new Simple();
        simple.number = number;
        simple.text = text;
        return SIMPLE.toReflection(simple);
    }

    private DataModelReflection date(Calendar date) {
        Simple simple = new Simple();
        simple.dateValue = date;
        return SIMPLE.toReflection(simple);
    }

    private DataModelReflection datetime(Calendar dateTime) {
        Simple simple = new Simple();
        simple.datetimeValue = dateTime;
        return SIMPLE.toReflection(simple);
    }

    private VerifyContext context(int elapsedMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2011, 2, 31, 23, 0, 0);
        VerifyContext result = new VerifyContext(calendar.getTime());

        calendar.add(Calendar.MINUTE, elapsedMinutes);
        result.setTestFinished(calendar.getTime());
        return result;
    }

    private URI uri(String file, String fragment) throws Exception {
        URL url = getClass().getResource("verify/" + file);
        assertThat(file, url, not(nullValue()));
        URI resource = url.toURI();
        URI uri = new URI(
                resource.getScheme(),
                resource.getUserInfo(),
                resource.getHost(),
                resource.getPort(),
                resource.getPath(),
                resource.getQuery(),
                fragment);
        return uri;
    }

    private VerifyRule rule(String name) throws Exception {
        return rule(name, context(10));
    }

    private VerifyRule rule(String name, VerifyContext context) throws Exception {
        assert name != null;
        ExcelSheetRuleProvider provider = new ExcelSheetRuleProvider();
        VerifyRule rule = provider.get(SIMPLE, context, uri(name, ":0"));
        assertThat(rule, not(nullValue()));
        return rule;
    }
}
