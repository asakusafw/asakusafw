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
package com.asakusafw.dmdl.thundergate.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.apache.hadoop.io.Text;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.CompositeDataModelDriver;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.thundergate.Configuration;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.dmdl.thundergate.ModelMatcher;
import com.asakusafw.dmdl.thundergate.model.Attribute;
import com.asakusafw.dmdl.thundergate.model.BasicType;
import com.asakusafw.dmdl.thundergate.model.JoinedModelDescription;
import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.dmdl.thundergate.model.StringType;
import com.asakusafw.dmdl.thundergate.model.SummarizedModelDescription;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.dmdl.thundergate.util.JoinedModelBuilder;
import com.asakusafw.dmdl.thundergate.util.SummarizedModelBuilder;
import com.asakusafw.dmdl.thundergate.util.TableModelBuilder;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Test for {@link ThunderGateModelEmitter}.
 */
public class ThunderGateModelEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CompositeDataModelDriver(getClass().getClassLoader()));
    }

    private Configuration config() {
        Configuration config = new Configuration();
        config.setJdbcDriver("dummy");
        config.setJdbcUrl("dummy");
        config.setJdbcUser("dummy");
        config.setJdbcPassword("dummy");
        config.setDatabaseName("dummy");
        config.setMatcher(ModelMatcher.NOTHING);
        config.setOutput(folder.getRoot());
        config.setEncoding(Charset.forName("UTF-8"));
        return config;
    }

    /**
     * simple table.
     * @throws Exception if test was failed
     */
    @Test
    public void table() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("comment", "VALUE", new StringType(255))
            .toDescription();

        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config());
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);

        ModelWrapper object = loader.newModel("Simple");

        object.set("value", new Text("Hello, world!"));
        assertThat(object.get("value"), eq(new Text("Hello, world!")));
    }

    /**
     * simple join.
     * @throws Exception if test was failed
     */
    @Test
    public void join() throws Exception {
        TableModelDescription left = new TableModelBuilder("LEFT")
            .add(null, "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "RIGHT_ID", PropertyTypeKind.LONG)
            .add(null, "VALUE", new StringType(255))
            .toDescription();
        TableModelDescription right = new TableModelBuilder("RIGHT")
            .add(null, "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "VALUE", new StringType(255))
            .toDescription();
        JoinedModelDescription join = new JoinedModelBuilder("SIMPLE", left, "l", right, "r")
            .on("l.RIGHT_ID", "r.SID")
            .add("ID", "r.SID")
            .add("LEFT", "l.VALUE")
            .add("RIGHT", "r.VALUE")
            .toDescription();

        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config());
        emitter.emit(left);
        emitter.emit(right);
        emitter.emit(join);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_VIEW);

        ModelWrapper object = loader.newModel("Simple");

        object.set("id", 127L);
        assertThat(object.get("id"), eq(127L));
        object.set("left", new Text("Hello, left!"));
        assertThat(object.get("left"), eq(new Text("Hello, left!")));
        object.set("right", new Text("Hello, right!"));
        assertThat(object.get("right"), eq(new Text("Hello, right!")));
    }

    /**
     * simple summarize.
     * @throws Exception if test was failed
     */
    @Test
    public void summarize() throws Exception {
        TableModelDescription target = new TableModelBuilder("TARGET")
            .add(null, "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "VALUE_A", PropertyTypeKind.INT)
            .add(null, "VALUE_B", PropertyTypeKind.LONG)
            .add(null, "VALUE_C", PropertyTypeKind.DATE)
            .toDescription();
        SummarizedModelDescription summarize = new SummarizedModelBuilder("SIMPLE", target, "t")
            .groupBy("VALUE_A")
            .add("KEY", com.asakusafw.dmdl.thundergate.model.Aggregator.IDENT, "t.VALUE_A")
            .add("SUM", com.asakusafw.dmdl.thundergate.model.Aggregator.SUM, "t.VALUE_B")
            .add("COUNT", com.asakusafw.dmdl.thundergate.model.Aggregator.COUNT, "t.SID")
            .add("MAX", com.asakusafw.dmdl.thundergate.model.Aggregator.MAX, "t.VALUE_C")
            .add("MIN", com.asakusafw.dmdl.thundergate.model.Aggregator.MIN, "t.VALUE_C")
            .toDescription();

        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config());
        emitter.emit(target);
        emitter.emit(summarize);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_VIEW);

        ModelWrapper object = loader.newModel("Simple");

        object.set("key", 127);
        assertThat(object.get("key"), eq(127));
        object.set("sum", 256L);
        assertThat(object.get("sum"), eq(256L));
        object.set("count", 10L);
        assertThat(object.get("count"), eq(10L));
        object.set("max", new Date(2011, 12, 31));
        assertThat(object.get("max"), eq(new Date(2011, 12, 31)));
        object.set("min", new Date(2011, 4, 1));
        assertThat(object.get("min"), eq(new Date(2011, 4, 1)));
    }

    /**
     * table cache.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "SID", new BasicType(PropertyTypeKind.LONG))
            .add("", "TIMESTAMP", new BasicType(PropertyTypeKind.DATETIME))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);

        ModelWrapper object = loader.newModel("Simple");
        assertThat(object.unwrap(), instanceOf(ThunderGateCacheSupport.class));
        ThunderGateCacheSupport support = (ThunderGateCacheSupport) object.unwrap();

        object.set("sid", 123L);
        assertThat(support.__tgc__SystemId(), is(123L));
        assertThat(support.__tgc__TimestampColumn(), is("TIMESTAMP"));
        assertThat(support.__tgc__Deleted(), is(false));
    }

    /**
     * table cache.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached_with_delete() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "SID", new BasicType(PropertyTypeKind.LONG))
            .add("", "TIMESTAMP", new BasicType(PropertyTypeKind.DATETIME))
            .add("", "DELETE", new BasicType(PropertyTypeKind.BOOLEAN))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        config.setDeleteFlagColumn("DELETE");
        config.setDeleteFlagValue(new AstLiteral(null, "TRUE", LiteralKind.BOOLEAN));
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);

        ModelWrapper object = loader.newModel("Simple");
        assertThat(object.unwrap(), instanceOf(ThunderGateCacheSupport.class));
        ThunderGateCacheSupport support = (ThunderGateCacheSupport) object.unwrap();

        object.set("delete", true);
        assertThat(support.__tgc__Deleted(), is(true));
        object.set("delete", false);
        assertThat(support.__tgc__Deleted(), is(false));
        object.setOption("delete", null);
        assertThat(support.__tgc__Deleted(), is(false));
    }

    /**
     * table cache without SID.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached_no_sid() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "TIMESTAMP", new BasicType(PropertyTypeKind.DATETIME))
            .add("", "DELETE", new BasicType(PropertyTypeKind.BOOLEAN))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        config.setDeleteFlagColumn("DELETE");
        config.setDeleteFlagValue(new AstLiteral(null, "TRUE", LiteralKind.BOOLEAN));
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        assertThat(loader.newModel("Simple").unwrap(), not(instanceOf(ThunderGateCacheSupport.class)));
    }

    /**
     * table cache with invalid SID.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached_invalid_sid() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "SID", new BasicType(PropertyTypeKind.INT))
            .add("", "TIMESTAMP", new BasicType(PropertyTypeKind.DATETIME))
            .add("", "DELETE", new BasicType(PropertyTypeKind.BOOLEAN))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        config.setDeleteFlagColumn("DELETE");
        config.setDeleteFlagValue(new AstLiteral(null, "TRUE", LiteralKind.BOOLEAN));
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        assertThat(loader.newModel("Simple").unwrap(), not(instanceOf(ThunderGateCacheSupport.class)));
    }

    /**
     * table cache without timestamp.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached_no_timestamp() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "SID", new BasicType(PropertyTypeKind.LONG))
            .add("", "DELETE", new BasicType(PropertyTypeKind.BOOLEAN))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        config.setDeleteFlagColumn("DELETE");
        config.setDeleteFlagValue(new AstLiteral(null, "TRUE", LiteralKind.BOOLEAN));
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        assertThat(loader.newModel("Simple").unwrap(), not(instanceOf(ThunderGateCacheSupport.class)));
    }

    /**
     * table cache with invalid timestamp.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached_invalid_timestamp() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "SID", new BasicType(PropertyTypeKind.LONG))
            .add("", "TIMESTAMP", new BasicType(PropertyTypeKind.LONG))
            .add("", "DELETE", new BasicType(PropertyTypeKind.BOOLEAN))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        config.setDeleteFlagColumn("DELETE");
        config.setDeleteFlagValue(new AstLiteral(null, "TRUE", LiteralKind.BOOLEAN));
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        assertThat(loader.newModel("Simple").unwrap(), not(instanceOf(ThunderGateCacheSupport.class)));
    }

    /**
     * table cache without delete flag.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached_no_delete() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "SID", new BasicType(PropertyTypeKind.LONG))
            .add("", "TIMESTAMP", new BasicType(PropertyTypeKind.DATETIME))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        config.setDeleteFlagColumn("DELETE");
        config.setDeleteFlagValue(new AstLiteral(null, "TRUE", LiteralKind.BOOLEAN));
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        assertThat(loader.newModel("Simple").unwrap(), instanceOf(ThunderGateCacheSupport.class));
    }

    /**
     * table cache without SID.
     * @throws Exception if test was failed
     */
    @Test
    public void table_cached_invalid_delete() throws Exception {
        TableModelDescription table = new TableModelBuilder("SIMPLE")
            .add("", "SID", new BasicType(PropertyTypeKind.LONG))
            .add("", "TIMESTAMP", new BasicType(PropertyTypeKind.DATETIME))
            .add("", "DELETE", new BasicType(PropertyTypeKind.LONG))
            .toDescription();

        Configuration config = config();
        config.setSidColumn("SID");
        config.setTimestampColumn("TIMESTAMP");
        config.setDeleteFlagColumn("DELETE");
        config.setDeleteFlagValue(new AstLiteral(null, "TRUE", LiteralKind.BOOLEAN));
        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(config);
        emitter.emit(table);

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_TABLE);
        assertThat(loader.newModel("Simple").unwrap(), not(instanceOf(ThunderGateCacheSupport.class)));
    }

    private Matcher<Object> eq(Object object) {
        return is(object);
    }
}
