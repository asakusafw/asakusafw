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
package com.asakusafw.dmdl.directio.hive.common;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.junit.Test;

import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.PropertyDescriptor;
import com.asakusafw.dmdl.directio.hive.orc.OrcFileEmitter;
import com.asakusafw.info.hive.TableInfo;
import com.asakusafw.info.hive.annotation.HiveField;
import com.asakusafw.info.hive.annotation.HiveTable;

/**
 * Test for {@link HiveDataModelEmitter}.
 */
public class HiveDataModelEmitterTest extends GeneratorTesterRoot {

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  simple : INT;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        assertThat(descriptor.createDataModelObject(), instanceOf(loader.modelType("Model")));
        assertThat(descriptor.getDataModelComment(), is(nullValue()));
        assertThat(descriptor.getPropertyDescriptors(), hasSize(1));

        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getFieldName(), is("simple"));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.intTypeInfo));
        assertThat(property.getFieldComment(), is(nullValue()));

        HiveTable table = descriptor.getDataModelClass().getAnnotation(HiveTable.class);
        assertThat(table, is(notNullValue()));
        Class<? extends TableInfo.Provider>[] infos = table.value();
        assertThat(infos, arrayWithSize(1));
        TableInfo.Provider info = infos[0].newInstance();
        assertThat(info.getSchema().getName(), is("model"));

        Method simple = descriptor.getDataModelClass().getMethod("getSimpleOption");
        HiveField field = simple.getAnnotation(HiveField.class);
        assertThat(field, is(notNullValue()));
        assertThat(field.name(), is("simple"));
        assertThat(field.type(), is("int"));
        assertThat(field.ignore(), is(false));
    }

    /**
     * w/o attribute.
     */
    @Test
    public void wo_attribute() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "model = {",
                "  simple : INT;",
                "};"
        });
        assertThat(loader.exists(OrcFileEmitter.CATEGORY, "ModelDescriptorFactory"), is(false));
    }

    /**
     * simple case.
     */
    @Test
    public void types() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "    c_int : INT;",
                "    c_text : TEXT;",
                "    c_boolean : BOOLEAN;",
                "    c_byte : BYTE;",
                "    c_short : SHORT;",
                "    c_long : LONG;",
                "    c_float : FLOAT;",
                "    c_double : DOUBLE;",
                "    c_decimal : DECIMAL;",
                "    c_date : DATE;",
                "    c_datetime : DATETIME;",
                "};",
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        assertThat(descriptor.createDataModelObject(), instanceOf(loader.modelType("Model")));
        assertThat(descriptor.getDataModelComment(), is(nullValue()));
        assertThat(descriptor.getPropertyDescriptors(), hasSize(11));
    }

    /**
     * with comment.
     */
    @Test
    public void w_comment() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "'c1'",
                "@directio.hive.orc",
                "model = {",
                "  'c2'",
                "  simple : INT;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        assertThat(descriptor.getDataModelComment(), is("c1"));
        assertThat(descriptor.getPropertyDescriptors(), hasSize(1));

        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getInspector().getTypeName(), is("int"));
        assertThat(property.getFieldComment(), is("c2"));
    }

    /**
     * with field info.
     * @throws Exception if failed
     */
    @Test
    public void w_field() throws Exception {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.field(name = 'f')",
                "  simple : INT;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("f");
        assertThat(property, is(notNullValue()));
        assertThat(property.getFieldName(), is("f"));
        assertThat(property.getInspector().getTypeName(), is("int"));

        Method simple = descriptor.getDataModelClass().getMethod("getSimpleOption");
        HiveField field = simple.getAnnotation(HiveField.class);
        assertThat(field, is(notNullValue()));
        assertThat(field.name(), is("f"));
    }

    /**
     * decimal with string info.
     */
    @Test
    public void decimal_w_string() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.string",
                "  simple : DECIMAL;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.stringTypeInfo));
    }

    /**
     * date with string info.
     */
    @Test
    public void date_w_string() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.string",
                "  simple : DATE;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.stringTypeInfo));
    }

    /**
     * date-time with string info.
     */
    @Test
    public void datetime_w_string() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.string",
                "  simple : DATETIME;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.stringTypeInfo));
    }

    /**
     * with timestamp info.
     */
    @Test
    public void w_timestamp() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.timestamp",
                "  simple : DATE;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.timestampTypeInfo));
    }

    /**
     * with detail decimal info.
     */
    @Test
    public void w_decimal_detail() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.decimal(precision = 10, scale = 2)",
                "  simple : DECIMAL;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.getDecimalTypeInfo(10, 2)));
    }

    /**
     * with char info.
     */
    @Test
    public void w_char() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.char(length = 10)",
                "  simple : TEXT;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.getCharTypeInfo(10)));
    }

    /**
     * with varchar info.
     */
    @Test
    public void w_varchar() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.varchar(length = 10)",
                "  simple : TEXT;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        PropertyDescriptor property = descriptor.findPropertyDescriptor("simple");
        assertThat(property, is(notNullValue()));
        assertThat(property.getTypeInfo(), is((TypeInfo) TypeInfoFactory.getVarcharTypeInfo(10)));
    }

    /**
     * with ignored properties.
     * @throws Exception if failed
     */
    @Test
    public void w_ignore() throws Exception {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  @directio.hive.ignore",
                "  ignored : TEXT;",
                "  simple : TEXT;",
                "};"
        });
        DataModelDescriptor descriptor = descriptor(loader, "ModelDescriptorFactory");
        assertThat(descriptor.getPropertyDescriptors(), hasSize(1));
        assertThat(descriptor.findPropertyDescriptor("simple"), is(notNullValue()));

        Method simple = descriptor.getDataModelClass().getMethod("getIgnoredOption");
        HiveField field = simple.getAnnotation(HiveField.class);
        assertThat(field, is(notNullValue()));
        assertThat(field.ignore(), is(true));
    }

    private DataModelDescriptor descriptor(ModelLoader loader, String simpleName) {
        try {
            Class<?> aClass = loader.load(HiveDataModelEmitter.CATEGORY, simpleName);
            return (DataModelDescriptor) aClass.getMethod(HiveDataModelEmitter.NAME_GETTER_METHOD).invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
