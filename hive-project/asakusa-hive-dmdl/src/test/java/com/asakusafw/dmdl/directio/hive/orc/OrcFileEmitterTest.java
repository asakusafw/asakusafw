/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.hive.orc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.hadoop.hive.ql.io.orc.CompressionKind;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.junit.Test;

import com.asakusafw.directio.hive.orc.AbstractOrcFileFormat;
import com.asakusafw.directio.hive.orc.OrcFormatConfiguration;
import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.dmdl.directio.hive.common.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.hive.common.HiveDataModelEmitter;

/**
 * Test for {@link OrcFileEmitter}.
 */
public class OrcFileEmitterTest extends GeneratorTesterRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc",
                "model = {",
                "  simple : INT;",
                "};"
        });
        AbstractOrcFileFormat<?> format = load(loader, "ModelOrcFileFormat");
        assertThat(format.getDataModelDescriptor().getDataModelClass(), equalTo((Object) loader.modelType("Model")));
        assertThat(format.getTableName(), is("model"));

        OrcFormatConfiguration conf = format.getFormatConfiguration();
        assertThat(conf.getFormatVersion(), is(nullValue()));
        assertThat(conf.getCompressionKind(CompressionKind.class), is(CompressionKind.SNAPPY));
        assertThat(conf.getStripeSize(), is(64L * 1024 * 1024));

        assertThat(loader.exists(OrcFileEmitter.CATEGORY, "AbstractModelOrcFileInputDescription"), is(true));
        assertThat(loader.exists(OrcFileEmitter.CATEGORY, "AbstractModelOrcFileOutputDescription"), is(true));
    }

    /**
     * w/o attribute.
     */
    @Test
    public void wo_mark() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "model = {",
                "  simple : INT;",
                "};"
        });
        assertThat(loader.exists(OrcFileEmitter.CATEGORY, "ModelOrcFileFormat"), is(false));
    }

    /**
     * all attributes.
     */
    @Test
    public void attributes() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new OrcFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.orc(",
                "  table_name = 'attributes_test',",
                "  field_mapping = 'name',",
                "  on_missing_source = 'ignore',",
                "  on_missing_target = 'fail',",
                "  on_incompatible_type = 'logging',",
                "  format_version = '0.11',",
                "  compression = 'zlib',",
                "  stripe_size = 123456789,",
                ")",
                "model = { simple : INT; };"
        });
        AbstractOrcFileFormat<?> format = load(loader, "ModelOrcFileFormat");
        assertThat(format.getDataModelDescriptor().getDataModelClass(), equalTo((Object) loader.modelType("Model")));
        assertThat(format.getTableName(), is("attributes_test"));

        OrcFormatConfiguration conf = format.getFormatConfiguration();
        assertThat(conf.getFieldMappingStrategy(), is(FieldMappingStrategy.NAME));
        assertThat(conf.getOnMissingSource(), is(ExceptionHandlingStrategy.IGNORE));
        assertThat(conf.getOnMissingTarget(), is(ExceptionHandlingStrategy.FAIL));
        assertThat(conf.getOnIncompatibleType(), is(ExceptionHandlingStrategy.LOGGING));
        assertThat(conf.getFormatVersion(OrcFile.Version.class), is(OrcFile.Version.V_0_11));
        assertThat(conf.getCompressionKind(CompressionKind.class), is(CompressionKind.ZLIB));
        assertThat(conf.getStripeSize(), is(123456789L));
    }

    private AbstractOrcFileFormat<?> load(ModelLoader loader, String simpleName) {
        return (AbstractOrcFileFormat<?>) loader.newObject(OrcFileEmitter.CATEGORY, simpleName);
    }
}
