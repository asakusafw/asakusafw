/**
 * Copyright 2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel.extension;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.excel.ExcelRuleExtractor.FormatException;
import com.asakusafw.testdriver.rule.ValuePredicate;

/**
 * Test for {@link ExcelSheetApproximateRule}.
 */
public class ExcelSheetApproximateRuleTest {

    /**
     * simple case for integral value.
     * @throws Exception if failed
     */
    @Test
    public void simple_int() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.INT, "~10");
        assertNg(pred, 50, 30);
        assertNg(pred, 50, 39);
        assertOk(pred, 50, 40);
        assertOk(pred, 50, 45);
        assertOk(pred, 50, 50);
        assertOk(pred, 50, 55);
        assertOk(pred, 50, 60);
        assertNg(pred, 50, 61);
        assertNg(pred, 50, 70);
    }

    /**
     * simple case for integral value.
     * @throws Exception if failed
     */
    @Test
    public void simple_int_plus() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.INT, "~+10");
        assertNg(pred, 50, 45);
        assertNg(pred, 50, 49);
        assertOk(pred, 50, 50);
        assertOk(pred, 50, 55);
        assertOk(pred, 50, 60);
        assertNg(pred, 50, 61);
        assertNg(pred, 50, 70);
    }

    /**
     * simple case for integral value.
     * @throws Exception if failed
     */
    @Test
    public void simple_int_minus() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.INT, "~-10");
        assertNg(pred, 50, 30);
        assertNg(pred, 50, 39);
        assertOk(pred, 50, 40);
        assertOk(pred, 50, 45);
        assertOk(pred, 50, 50);
        assertNg(pred, 50, 51);
        assertNg(pred, 50, 55);
    }

    /**
     * invalid case for integral value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_int_invalid() throws Exception {
        parse(PropertyType.INT, "~INVALID");
    }

    /**
     * simple case for float value.
     * @throws Exception if failed
     */
    @Test
    public void simple_float() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.FLOAT, "~2.5");
        assertNg(pred, 15.0, 10.0);
        assertNg(pred, 15.0, 12.4);
        assertOk(pred, 15.0, 12.6);
        assertOk(pred, 15.0, 14.0);
        assertOk(pred, 15.0, 15.0);
        assertOk(pred, 15.0, 16.0);
        assertOk(pred, 15.0, 17.4);
        assertNg(pred, 15.0, 17.6);
        assertNg(pred, 15.0, 20.0);
    }

    /**
     * simple case for float value.
     * @throws Exception if failed
     */
    @Test
    public void simple_float_plus() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.FLOAT, "~+2.5");
        assertNg(pred, 15.0, 13.0);
        assertNg(pred, 15.0, 14.9);
        assertOk(pred, 15.0, 15.0);
        assertOk(pred, 15.0, 16.0);
        assertOk(pred, 15.0, 17.4);
        assertNg(pred, 15.0, 17.6);
        assertNg(pred, 15.0, 20.0);
    }

    /**
     * simple case for float value.
     * @throws Exception if failed
     */
    @Test
    public void simple_float_minus() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.FLOAT, "~-2.5");
        assertNg(pred, 15.0, 10.0);
        assertNg(pred, 15.0, 12.4);
        assertOk(pred, 15.0, 12.6);
        assertOk(pred, 15.0, 14.0);
        assertOk(pred, 15.0, 15.0);
        assertNg(pred, 15.0, 15.1);
        assertNg(pred, 15.0, 17.0);
    }

    /**
     * invalid case for float value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_float_invalid() throws Exception {
        parse(PropertyType.FLOAT, "~INVALID");
    }


    /**
     * simple case for decimal value.
     * @throws Exception if failed
     */
    @Test
    public void simple_decimal() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.DECIMAL, "~2.5");
        assertNg(pred, dec("15.0"), dec("10.0"));
        assertNg(pred, dec("15.0"), dec("12.4"));
        assertOk(pred, dec("15.0"), dec("12.5"));
        assertOk(pred, dec("15.0"), dec("14.0"));
        assertOk(pred, dec("15.0"), dec("15.0"));
        assertOk(pred, dec("15.0"), dec("16.0"));
        assertOk(pred, dec("15.0"), dec("17.5"));
        assertNg(pred, dec("15.0"), dec("17.6"));
        assertNg(pred, dec("15.0"), dec("20.0"));
    }

    /**
     * simple case for decimal value.
     * @throws Exception if failed
     */
    @Test
    public void simple_decimal_plus() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.DECIMAL, "~+2.5");
        assertNg(pred, dec("15.0"), dec("13.0"));
        assertNg(pred, dec("15.0"), dec("14.9"));
        assertOk(pred, dec("15.0"), dec("15.0"));
        assertOk(pred, dec("15.0"), dec("16.0"));
        assertOk(pred, dec("15.0"), dec("17.5"));
        assertNg(pred, dec("15.0"), dec("17.6"));
        assertNg(pred, dec("15.0"), dec("20.0"));
    }

    /**
     * simple case for decimal value.
     * @throws Exception if failed
     */
    @Test
    public void simple_decimal_minus() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.DECIMAL, "~-2.5");
        assertNg(pred, dec("15.0"), dec("10.0"));
        assertNg(pred, dec("15.0"), dec("12.4"));
        assertOk(pred, dec("15.0"), dec("12.5"));
        assertOk(pred, dec("15.0"), dec("14.0"));
        assertOk(pred, dec("15.0"), dec("15.0"));
        assertNg(pred, dec("15.0"), dec("15.1"));
        assertNg(pred, dec("15.0"), dec("17.0"));
    }

    /**
     * invalid case for decimal value.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void simple_decimal_invalid() throws Exception {
        parse(PropertyType.DECIMAL, "~INVALID");
    }

    /**
     * unsupported expression.
     * @throws Exception if failed
     */
    @Test
    public void unsupported() throws Exception {
        ValuePredicate<Number> pred = parse(PropertyType.INT, "UNSUPPORTED");
        assertThat(pred, is(nullValue()));
    }

    /**
     * invalid expression.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void invalid() throws Exception {
        parse(PropertyType.INT, "~");
    }

    /**
     * inconsistent type.
     * @throws Exception if failed
     */
    @Test(expected = FormatException.class)
    public void inconsistent_type() throws Exception {
        parse(PropertyType.BOOLEAN, "~1");
    }

    @SuppressWarnings("unchecked")
    private static ValuePredicate<Number> parse(PropertyType type, String expr) throws FormatException {
        VerifyContext context = new VerifyContext(new TestContext.Empty());
        ExcelSheetApproximateRule rule = new ExcelSheetApproximateRule();
        return (ValuePredicate<Number>) rule.resolve(context, PropertyName.newInstance("testing"), type, expr);
    }

    private static void assertOk(ValuePredicate<Number> pred, Number expected, Number actual) {
        assertThat(pred.describeExpected(expected, actual), pred.accepts(expected, actual), is(true));
    }

    private static <T> void assertNg(ValuePredicate<Number> pred, Number expected, Number actual) {
        assertThat(pred.describeExpected(expected, actual), pred.accepts(expected, actual), is(false));
    }

    private Number dec(String string) {
        return new BigDecimal(string);
    }
}
