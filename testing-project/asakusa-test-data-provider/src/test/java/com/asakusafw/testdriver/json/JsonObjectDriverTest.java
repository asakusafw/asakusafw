/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver.json;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;
import com.google.gson.JsonObject;

/**
 * Test for {@link JsonObjectDriver}.
 * @since 0.2.0
 */
public class JsonObjectDriverTest {

    /**
     * simple.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        DataModelDefinition<Simple> def = new SimpleDataModelDefinition<>(Simple.class);
        JsonObject json = new JsonObject();
        json.addProperty("number", 100);
        json.addProperty("text", "Hello, world!");

        DataModelReflection ref = JsonObjectDriver.convert(def, json);
        Simple object = def.toObject(ref);

        assertThat(object.number, is(100));
        assertThat(object.text, is("Hello, world!"));
    }

    /**
     * property is not defined.
     * @throws Exception if failed
     */
    @Test
    public void invalid_property() throws Exception {
        DataModelDefinition<Simple> def = new SimpleDataModelDefinition<>(Simple.class);
        JsonObject json = new JsonObject();
        json.addProperty("invalid", 100);
        json.addProperty("text", "Hello, world!");

        DataModelReflection ref = JsonObjectDriver.convert(def, json);
        Simple object = def.toObject(ref);

        assertThat(object.number, is(nullValue()));
        assertThat(object.text, is("Hello, world!"));
    }

    /**
     * inconsistent types.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void inconsistent_type() throws Exception {
        DataModelDefinition<Simple> def = new SimpleDataModelDefinition<>(Simple.class);
        JsonObject json = new JsonObject();
        json.addProperty("number", "QQQ");
        json.addProperty("text", "Hello, world!");

        JsonObjectDriver.convert(def, json);
    }

    /**
     * variety of property types.
     * @throws Exception if failed
     */
    @Test
    public void types() throws Exception {
        DataModelDefinition<Simple> def = new SimpleDataModelDefinition<>(Simple.class);
        JsonObject json = new JsonObject();
        json.addProperty("boolean_value", true);
        json.addProperty("byte_value", (byte) 100);
        json.addProperty("short_value", (short) 200);
        json.addProperty("long_value", 300L);
        json.addProperty("big_integer_value", new BigInteger("123456"));
        json.addProperty("float_value", 1.0f);
        json.addProperty("double_value", 1.5d);
        json.addProperty("big_decimal_value", new BigDecimal("12.3456"));
        json.addProperty("date_value", "2012-01-02");
        json.addProperty("datetime_value", "2011-12-31 10:20:30");

        DataModelReflection ref = JsonObjectDriver.convert(def, json);
        Simple object = def.toObject(ref);

        assertThat(object.booleanValue, is(true));
        assertThat(object.byteValue, is((byte) 100));
        assertThat(object.shortValue, is((short) 200));
        assertThat(object.longValue, is(300L));
        assertThat(object.bigIntegerValue, is(new BigInteger("123456")));
        assertThat(object.floatValue, is(1.0f));
        assertThat(object.doubleValue, is(1.5d));
        assertThat(object.bigDecimalValue, is(new BigDecimal("12.3456")));
        assertThat(object.dateValue, is(calendar("yyyy-MM-dd", "2012-01-02")));
        assertThat(object.datetimeValue, is(calendar("yyyy-MM-dd HH:mm:ss", "2011-12-31 10:20:30")));
    }

    private Matcher<Calendar> calendar(final String format, final String value) {
        return new BaseMatcher<Calendar>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof Calendar) {
                    Calendar c = (Calendar) object;
                    String actual = new SimpleDateFormat(format).format(c.getTime());
                    return value.equals(actual);
                }
                return false;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(value);
            }
        };
    }
}
