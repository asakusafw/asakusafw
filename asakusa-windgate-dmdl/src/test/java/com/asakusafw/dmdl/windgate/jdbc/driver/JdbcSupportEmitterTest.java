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
package com.asakusafw.dmdl.windgate.jdbc.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.driver.ObjectDriver;
import com.asakusafw.dmdl.windgate.common.driver.GeneratorTesterRoot;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelPreparedStatement;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelResultSet;

/**
 * Test for {@link JdbcSupportEmitter}.
 */
public class JdbcSupportEmitterTest extends GeneratorTesterRoot {

    /**
     * Test database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("testing");

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new JdbcSupportEmitter());
        emitDrivers.add(new ObjectDriver());
    }

    /**
     * A simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        ModelLoader loaded = generateJava("simple");
        ModelWrapper model = loaded.newModel("Simple");
        DataModelJdbcSupport<?> support = (DataModelJdbcSupport<?>) loaded.newObject("jdbc", "SimpleJdbcSupport");

        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        assertThat(support.isSupported(list("VALUE")), is(true));
        assertThat(support.isSupported(list("VALUE", "VALUE")), is(false));
        assertThat(support.isSupported(this.<String>list()), is(false));
        assertThat(support.isSupported(list("INVALID")), is(false));
        assertThat(support.isSupported(list("VALUE", "INVALID")), is(false));

        DataModelJdbcSupport<Object> unsafe = unsafe(support);
        h2.executeFile("simple.sql");
        Connection conn = h2.open();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO SIMPLE (VALUE) VALUES (?)");
            DataModelPreparedStatement<Object> p = unsafe.createPreparedStatementSupport(ps, list("VALUE"));
            model.set("value", new Text("Hello, world!"));
            p.setParameters(model.unwrap());
            ps.executeUpdate();
            ps.close();

            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT VALUE FROM SIMPLE");
            DataModelResultSet<Object> r = unsafe.createResultSetSupport(rs, list("VALUE"));
            assertThat(r.next(model.unwrap()), is(true));
            assertThat(model.get("value"), is((Object) new Text("Hello, world!")));
            assertThat(r.next(model.unwrap()), is(false));
        } finally {
            conn.close();
        }
    }

    /**
     * All types.
     * @throws Exception if failed
     */
    @Test
    public void types() throws Exception {
        ModelLoader loaded = generateJava("types");
        ModelWrapper model = loaded.newModel("Types");
        DataModelJdbcSupport<?> support = (DataModelJdbcSupport<?>) loaded.newObject("jdbc", "TypesJdbcSupport");
        assertThat(support.getSupportedType(), is((Object) model.unwrap().getClass()));

        List<String> list = list(new String[] {
                "C_INT",
                "C_TEXT",
                "C_BOOLEAN",
                "C_BYTE",
                "C_SHORT",
                "C_LONG",
                "C_FLOAT",
                "C_DOUBLE",
                "C_DECIMAL",
                "C_DATE",
                "C_DATETIME",
        });
        assertThat(support.isSupported(list), is(true));

        DataModelJdbcSupport<Object> unsafe = unsafe(support);
        h2.executeFile("types.sql");
        Connection conn = h2.open();
        try {
            PreparedStatement ps = conn.prepareStatement(MessageFormat.format(
                    "INSERT INTO TYPES ({0}) VALUES ({1})",
                    join(list),
                    join(Collections.nCopies(list.size(), "?"))));
            DataModelPreparedStatement<Object> p = unsafe.createPreparedStatementSupport(ps, list);

            // set nulls
            ModelWrapper nulls = loaded.newModel("Types");
            p.setParameters(nulls.unwrap());
            ps.executeUpdate();

            // text only
            ModelWrapper text = loaded.newModel("Types");
            text.set("c_text", new Text("Hello, world!"));
            p.setParameters(text.unwrap());
            ps.executeUpdate();

            // all types
            ModelWrapper all = loaded.newModel("Types");
            all.set("c_int", 100);
            all.set("c_text", new Text("Hello, DMDL world!"));
            all.set("c_boolean", true);
            all.set("c_byte", (byte) 64);
            all.set("c_short", (short) 1023);
            all.set("c_long", 100000L);
            all.set("c_float", 1.5f);
            all.set("c_double", 2.5f);
            all.set("c_decimal", new BigDecimal("3.1415"));
            all.set("c_date", new Date(2011, 9, 1));
            all.set("c_datetime", new DateTime(2011, 12, 31, 23, 59, 59));
            p.setParameters(all.unwrap());
            ps.executeUpdate();

            ps.close();

            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(MessageFormat.format(
                    "SELECT {0} FROM TYPES ORDER BY ORDINAL",
                    join(list)));
            DataModelResultSet<Object> r = unsafe.createResultSetSupport(rs, list);
            ModelWrapper buffer = loaded.newModel("Types");

            assertThat(r.next(buffer.unwrap()), is(true));
            assertThat(buffer.unwrap(), is(nulls.unwrap()));

            assertThat(r.next(buffer.unwrap()), is(true));
            assertThat(buffer.unwrap(), is(text.unwrap()));

            assertThat(r.next(buffer.unwrap()), is(true));
            assertThat(buffer.unwrap(), is(all.unwrap()));

            assertThat(r.next(buffer.unwrap()), is(false));
        } finally {
            conn.close();
        }
    }

    /**
     * Compile with no attributes.
     * @throws Exception if failed
     */
    @Test
    public void no_attributes() throws Exception {
        ModelLoader loaded = generateJava("no_attributes");
        assertThat(loaded.exists("jdbc", "NoAttributesJdbcSupport"), is(false));
    }

    private String join(List<String> list) {
        StringBuilder buf = new StringBuilder();
        buf.append(list.get(0));
        for (int i = 1, n = list.size(); i < n; i++) {
            buf.append(", ");
            buf.append(list.get(i));
        }
        return buf.toString();
    }

    @SuppressWarnings("unchecked")
    private DataModelJdbcSupport<Object> unsafe(DataModelJdbcSupport<?> support) {
        return (DataModelJdbcSupport<Object>) support;
    }

    private <T> List<T> list(T... values) {
        return Arrays.asList(values);
    }
}
