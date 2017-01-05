/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.junit.Test;

import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.directio.hive.serde.mock.MockSimple;
import com.asakusafw.directio.hive.serde.mock.MockTypes;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Test for {@link DataModelDriver}.
 */
@SuppressWarnings("deprecation")
public class DataModelDriverTest {

    /**
     * Simple test case.
     */
    @Test
    public void simple() {
        DataModelDescriptor descriptor = FieldPropertyDescriptor.extract(MockSimple.class);
        MockSimple obj = (MockSimple) descriptor.createDataModelObject();
        obj.number.modify(12345);
        obj.string.modify("Hello, world!");

        DataModelInspector inspector = new DataModelInspector(descriptor);
        assertThat(inspector.getTypeName(), is(MockSimple.class.getName()));
        assertThat(inspector.getAllStructFieldRefs(), hasSize(2));
        assertThat(inspector.getStructFieldsDataAsList(obj), is(Arrays.asList(new Object[] {
                obj.number,
                obj.string,
        })));
        assertThat(getJavaField(inspector, obj, "number"), equalTo((Object) 12345));
        assertThat(getJavaField(inspector, obj, "string"), equalTo((Object) "Hello, world!"));

        DataModelDriver driver = new DataModelDriver(descriptor, inspector);
        MockSimple copy = new MockSimple();
        driver.set(copy, obj);

        assertThat(copy.number, equalTo(obj.number));
        assertThat(copy.string, equalTo(obj.string));
    }

    /**
     * test case for column name mangled object.
     */
    @Test
    public void mangled() {
        DataModelDescriptor descriptor = FieldPropertyDescriptor.extract(MockSimple.class);
        MockSimple obj = (MockSimple) descriptor.createDataModelObject();
        obj.number.modify(12345);
        obj.string.modify("Hello, world!");

        List<PropertyDescriptor> properties = new ArrayList<>();
        for (PropertyDescriptor property : descriptor.getPropertyDescriptors()) {
            properties.add(new PropertyDescriptor(String.format("_col%d", properties.size()), property) {
                @Override
                public ValueOption<?> extract(Object dataModel) {
                    return property.extract(dataModel);
                }
            });
        }
        DataModelDescriptor mangled = new DataModelDescriptor(descriptor.getDataModelClass(), properties);

        DataModelMapping config = new DataModelMapping();
        config.setFieldMappingStrategy(FieldMappingStrategy.NAME);
        config.setOnMissingSource(ExceptionHandlingStrategy.FAIL);
        config.setOnMissingTarget(ExceptionHandlingStrategy.IGNORE);
        config.setOnIncompatibleType(ExceptionHandlingStrategy.IGNORE);
        try {
            DataModelDriver driver = new DataModelDriver(descriptor, new DataModelInspector(mangled), config);
            MockSimple copy = new MockSimple();
            driver.set(copy, obj);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // ok.
        }

        config.setFieldMappingStrategy(FieldMappingStrategy.POSITION);
        DataModelDriver driver = new DataModelDriver(descriptor, new DataModelInspector(mangled), config);
        MockSimple copy = new MockSimple();
        driver.set(copy, obj);

        assertThat(copy.number, equalTo(obj.number));
        assertThat(copy.string, equalTo(obj.string));
    }

    /**
     * All property types.
     */
    @Test
    public void types() {
        DataModelDescriptor descriptor = FieldPropertyDescriptor.extract(MockTypes.class);
        MockTypes obj = (MockTypes) descriptor.createDataModelObject();
        obj.booleanOption.modify(true);
        obj.byteOption.modify((byte) 1);
        obj.shortOption.modify((short) 2);
        obj.intOption.modify(3);
        obj.longOption.modify(4L);
        obj.floatOption.modify(5f);
        obj.doubleOption.modify(6d);
        obj.dateOption.modify(new Date(2014, 6, 1));
        obj.dateTimeOption.modify(new DateTime(2014, 6, 1, 2, 3, 4));
        obj.stringOption.modify("Hello, world!");
        obj.decimalOption.modify(new BigDecimal("7.8"));

        DataModelInspector inspector = new DataModelInspector(descriptor);
        assertThat(inspector.getTypeName(), is(MockTypes.class.getName()));
        assertThat(inspector.getAllStructFieldRefs(), hasSize(11));
        assertThat(inspector.getStructFieldsDataAsList(obj), is(Arrays.asList(new Object[] {
                obj.booleanOption,
                obj.byteOption,
                obj.shortOption,
                obj.intOption,
                obj.longOption,
                obj.floatOption,
                obj.doubleOption,
                obj.dateOption,
                obj.dateTimeOption,
                obj.stringOption,
                obj.decimalOption,
        })));

        assertThat(getJavaField(inspector, obj, "booleanOption"), equalTo((Object) obj.booleanOption.get()));
        assertThat(getJavaField(inspector, obj, "byteOption"), equalTo((Object) obj.byteOption.get()));
        assertThat(getJavaField(inspector, obj, "shortOption"), equalTo((Object) obj.shortOption.get()));
        assertThat(getJavaField(inspector, obj, "intOption"), equalTo((Object) obj.intOption.get()));
        assertThat(getJavaField(inspector, obj, "longOption"), equalTo((Object) obj.longOption.get()));
        assertThat(getJavaField(inspector, obj, "floatOption"), equalTo((Object) obj.floatOption.get()));
        assertThat(getJavaField(inspector, obj, "doubleOption"), equalTo((Object) obj.doubleOption.get()));

        Calendar calendar = Calendar.getInstance();
        DateUtil.setDayToCalendar(obj.dateOption.get().getElapsedDays(), calendar);
        assertThat(getJavaField(inspector, obj, "dateOption"),
                equalTo((Object) new java.sql.Date(calendar.getTimeInMillis())));
        DateUtil.setSecondToCalendar(obj.dateTimeOption.get().getElapsedSeconds(), calendar);
        assertThat(getJavaField(inspector, obj, "dateTimeOption"),
                equalTo((Object) new java.sql.Timestamp(calendar.getTimeInMillis())));
        assertThat(getJavaField(inspector, obj, "stringOption"),
                equalTo((Object) obj.stringOption.getAsString()));
        assertThat(((HiveDecimal) getJavaField(inspector, obj, "decimalOption")),
                equalTo(HiveDecimal.create(obj.decimalOption.get())));

        DataModelDriver driver = new DataModelDriver(descriptor, inspector);
        MockTypes copy = new MockTypes();
        driver.set(copy, obj);

        assertThat(copy.booleanOption, equalTo(obj.booleanOption));
        assertThat(copy.byteOption, equalTo(obj.byteOption));
        assertThat(copy.shortOption, equalTo(obj.shortOption));
        assertThat(copy.intOption, equalTo(obj.intOption));
        assertThat(copy.longOption, equalTo(obj.longOption));
        assertThat(copy.floatOption, equalTo(obj.floatOption));
        assertThat(copy.doubleOption, equalTo(obj.doubleOption));
        assertThat(copy.dateOption, equalTo(obj.dateOption));
        assertThat(copy.dateTimeOption, equalTo(obj.dateTimeOption));
        assertThat(copy.stringOption, equalTo(obj.stringOption));
        assertThat(copy.decimalOption, equalTo(obj.decimalOption));
    }

    /**
     * All property types with null.
     */
    @Test
    public void nulls() {
        DataModelDescriptor descriptor = FieldPropertyDescriptor.extract(MockTypes.class);
        MockTypes obj = (MockTypes) descriptor.createDataModelObject();

        DataModelInspector inspector = new DataModelInspector(descriptor);
        assertThat(inspector.getTypeName(), is(MockTypes.class.getName()));
        assertThat(inspector.getAllStructFieldRefs(), hasSize(11));
        assertThat(inspector.getStructFieldsDataAsList(obj), is(Arrays.asList(new Object[11])));

        assertThat(getJavaField(inspector, obj, "booleanOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "byteOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "shortOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "intOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "longOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "floatOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "doubleOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "decimalOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "stringOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "dateOption"), is(nullValue()));
        assertThat(getJavaField(inspector, obj, "dateTimeOption"), is(nullValue()));

        DataModelDriver driver = new DataModelDriver(descriptor, inspector);
        MockTypes copy = new MockTypes();
        copy.booleanOption.modify(true);
        copy.byteOption.modify((byte) 1);
        copy.shortOption.modify((short) 2);
        copy.intOption.modify(3);
        copy.longOption.modify(4L);
        copy.floatOption.modify(5f);
        copy.doubleOption.modify(6d);
        copy.dateOption.modify(new Date(2014, 6, 1));
        copy.dateTimeOption.modify(new DateTime(2014, 6, 1, 2, 3, 4));
        copy.stringOption.modify("Hello, world!");
        copy.decimalOption.modify(new BigDecimal("7.8"));

        driver.set(copy, obj);

        assertThat(copy.booleanOption.isNull(), is(true));
        assertThat(copy.byteOption.isNull(), is(true));
        assertThat(copy.shortOption.isNull(), is(true));
        assertThat(copy.intOption.isNull(), is(true));
        assertThat(copy.longOption.isNull(), is(true));
        assertThat(copy.floatOption.isNull(), is(true));
        assertThat(copy.doubleOption.isNull(), is(true));
        assertThat(copy.dateOption.isNull(), is(true));
        assertThat(copy.dateTimeOption.isNull(), is(true));
        assertThat(copy.stringOption.isNull(), is(true));
        assertThat(copy.decimalOption.isNull(), is(true));
    }

    private Object getJavaField(DataModelInspector inspector, Object object, String name) {
        StructField ref = inspector.getStructFieldRef(name);
        Object field = inspector.getStructFieldData(object, ref);
        PrimitiveObjectInspector primitive = (PrimitiveObjectInspector) ref.getFieldObjectInspector();
        Object result = primitive.getPrimitiveJavaObject(field);
        if (result == null) {
            assertThat(primitive.getPrimitiveWritableObject(field), is(nullValue()));
        } else {
            assertThat(
                    primitive.getPrimitiveWritableObject(field),
                    instanceOf(primitive.getPrimitiveWritableClass()));
        }
        assertThat(primitive.copyObject(field), equalTo(field));
        if (result != null) {
            assertThat(primitive.copyObject(field), is(not(sameInstance(field))));
        }
        return result;
    }
}
