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
package com.asakusafw.testdriver.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.testing.model.Naming;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.testdriver.testing.model.Variety;

/**
 * Test for {@link DefaultDataModelDefinition}.
 * @since 0.2.0
 */
public class DefaultDataModelDefinitionTest {

    /**
     * simple model class.
     */
    @Test
    public void simple() {
        DefaultDataModelDefinition<Simple> def = new DefaultDataModelDefinition<Simple>(Simple.class);
        assertThat(def.getModelClass(), equalTo(Simple.class));
    }

    /**
     * naming.
     */
    @Test
    public void naming() {
        DefaultDataModelDefinition<Naming> def = new DefaultDataModelDefinition<Naming>(Naming.class);
        assertThat(def.getType(p("a")), not(nullValue()));
        assertThat(def.getType(p("very_very_very_long_name")), not(nullValue()));
    }

    /**
     * Test method for {@link DefaultDataModelDefinition#getType(com.asakusafw.testdriver.core.PropertyName)}.
     */
    @Test
    public void getType() {
        DefaultDataModelDefinition<Variety> def = new DefaultDataModelDefinition<Variety>(Variety.class);
        assertThat(def.getType(p("missing")), is(nullValue()));
        assertThat(def.getType(p("p_int")), is((Object) Integer.class));
        assertThat(def.getType(p("p_long")), is((Object) Long.class));
        assertThat(def.getType(p("p_byte")), is((Object) Byte.class));
        assertThat(def.getType(p("p_short")), is((Object) Short.class));
        assertThat(def.getType(p("p_decimal")), is((Object) BigDecimal.class));
        assertThat(def.getType(p("p_text")), is((Object) String.class));
        assertThat(def.getType(p("p_boolean")), is((Object) Boolean.class));
        assertThat(def.getType(p("p_date")), is((Object) Calendar.class));
        assertThat(def.getType(p("p_datetime")), is((Object) Calendar.class));
    }

    /**
     * Test method for {@link DefaultDataModelDefinition#toReflection(java.lang.Object)}.
     */
    @Test
    public void toReflection() {
        DefaultDataModelDefinition<Variety> def = new DefaultDataModelDefinition<Variety>(Variety.class);
        Variety object = new Variety();
        object.setPInt(100);
        object.setPLong(200);
        object.setPByte((byte) 127);
        object.setPShort((short) 300);
        object.setPDecimal(new BigDecimal("3.141592"));
        object.setPTextAsString("Hello, world!");
        object.setPBoolean(true);
        object.setPDate(new Date(2011, 5, 2));
        object.setPDatetime(new DateTime(2011, 12, 31, 23, 59, 59));

        DataModelReflection ref = def.toReflection(object);
        assertThat(ref.getValue(p("p_int")), is((Object) 100));
        assertThat(ref.getValue(p("p_long")), is((Object) 200L));
        assertThat(ref.getValue(p("p_byte")), is((Object) (byte) 127));
        assertThat(ref.getValue(p("p_short")), is((Object) (short) 300));
        assertThat(ref.getValue(p("p_decimal")), is((Object) new BigDecimal("3.141592")));
        assertThat(ref.getValue(p("p_text")), is((Object) "Hello, world!"));
        assertThat(ref.getValue(p("p_boolean")), is((Object) true));
        Calendar date = (Calendar) ref.getValue(p("p_date"));
        assertThat(new SimpleDateFormat("yyyy/MM/dd").format(date.getTime()), is("2011/05/02"));
        Calendar datetime = (Calendar) ref.getValue(p("p_datetime"));
        assertThat(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(datetime.getTime()), is("2011/12/31 23:59:59"));
    }

    /**
     * Test method for {@link DefaultDataModelDefinition#toObject(com.asakusafw.testdriver.core.DataModelReflection)}.
     */
    @Test
    public void toObject() {
        DefaultDataModelDefinition<Variety> def = new DefaultDataModelDefinition<Variety>(Variety.class);
        Variety object = new Variety();
        object.setPInt(100);
        object.setPLong(200);
        object.setPByte((byte) 127);
        object.setPShort((short) 300);
        object.setPDecimal(new BigDecimal("3.141592"));
        object.setPTextAsString("Hello, world!");
        object.setPBoolean(true);
        object.setPDate(new Date(2011, 5, 2));
        object.setPDatetime(new DateTime(2011, 12, 31, 23, 59, 59));

        DataModelReflection ref = def.toReflection(object);
        Variety restored = def.toObject(ref);
        assertThat(restored, not(sameInstance(object)));
        assertThat(restored, equalTo(object));
    }

    private PropertyName p(String snake_name) {
        return PropertyName.newInstance(snake_name.split("_"));
    }
}
