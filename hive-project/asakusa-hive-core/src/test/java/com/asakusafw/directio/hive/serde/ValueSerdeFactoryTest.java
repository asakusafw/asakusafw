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
package com.asakusafw.directio.hive.serde;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.apache.hadoop.hive.common.type.HiveChar;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.common.type.HiveVarchar;
import org.apache.hadoop.hive.serde2.io.HiveCharWritable;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.hadoop.hive.serde2.io.HiveVarcharWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveCharObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveDecimalObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveVarcharObjectInspector;
import org.junit.Test;

import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Test for {@link ValueSerdeFactory}.
 */
public class ValueSerdeFactoryTest {

    /**
     * constants.
     */
    @Test
    public void constants() {
        for (ValueSerdeFactory serde : ValueSerdeFactory.values()) {
            serde.getDriver(serde.getInspector());
        }
    }

    /**
     * char.
     */
    @Test
    public void getChar() {
        ValueSerde serde = ValueSerdeFactory.getChar(10);
        HiveCharObjectInspector inspector = (HiveCharObjectInspector) serde.getInspector();

        StringOption option = new StringOption("hello");
        HiveChar value = new HiveChar("hello", 10);

        assertThat(inspector.copyObject(option), is((Object) option));
        assertThat(inspector.copyObject(option), is(not(sameInstance((Object) option))));
        assertThat(inspector.copyObject(null), is(nullValue()));
        assertThat(inspector.getPrimitiveJavaObject(option), is(value));
        assertThat(inspector.getPrimitiveJavaObject(null), is(nullValue()));
        assertThat(inspector.getPrimitiveWritableObject(option), is(new HiveCharWritable(value)));
        assertThat(inspector.getPrimitiveWritableObject(null), is(nullValue()));

        ValueDriver driver = serde.getDriver(inspector);
        StringOption copy = new StringOption();

        driver.set(copy, option);
        assertThat(copy, is(option));
        driver.set(copy, null);
        assertThat(copy.isNull(), is(true));
    }

    /**
     * varchar.
     */
    @Test
    public void getVarchar() {
        ValueSerde serde = ValueSerdeFactory.getVarchar(10);
        HiveVarcharObjectInspector inspector = (HiveVarcharObjectInspector) serde.getInspector();

        StringOption option = new StringOption("hello");
        HiveVarchar value = new HiveVarchar("hello", 10);

        assertThat(inspector.copyObject(option), is((Object) option));
        assertThat(inspector.copyObject(option), is(not(sameInstance((Object) option))));
        assertThat(inspector.copyObject(null), is(nullValue()));
        // Note: HiveVarchar.equals(Object) is not defined, but equals(HiveVarchar) is defined
        assertThat(inspector.getPrimitiveJavaObject(option).equals(value), is(true));
        assertThat(inspector.getPrimitiveJavaObject(null), is(nullValue()));
        assertThat(inspector.getPrimitiveWritableObject(option), is(new HiveVarcharWritable(value)));
        assertThat(inspector.getPrimitiveWritableObject(null), is(nullValue()));

        ValueDriver driver = serde.getDriver(inspector);
        StringOption copy = new StringOption();

        driver.set(copy, option);
        assertThat(copy, is(option));
        driver.set(copy, null);
        assertThat(copy.isNull(), is(true));
    }

    /**
     * qualified decimal.
     */
    @Test
    public void getDecimal() {
        ValueSerde serde = ValueSerdeFactory.getDecimal(10, 2);
        HiveDecimalObjectInspector inspector = (HiveDecimalObjectInspector) serde.getInspector();

        DecimalOption option = new DecimalOption(new BigDecimal("123.45"));
        HiveDecimal value = HiveDecimal.create(new BigDecimal("123.45"));

        assertThat(inspector.copyObject(option), is((Object) option));
        assertThat(inspector.copyObject(option), is(not(sameInstance((Object) option))));
        assertThat(inspector.copyObject(null), is(nullValue()));
        assertThat(inspector.getPrimitiveJavaObject(option), is(value));
        assertThat(inspector.getPrimitiveJavaObject(null), is(nullValue()));
        assertThat(inspector.getPrimitiveWritableObject(option), is(new HiveDecimalWritable(value)));
        assertThat(inspector.getPrimitiveWritableObject(null), is(nullValue()));

        ValueDriver driver = serde.getDriver(inspector);
        DecimalOption copy = new DecimalOption();

        driver.set(copy, option);
        assertThat(copy, is(option));
        driver.set(copy, null);
        assertThat(copy.isNull(), is(true));
    }
}
