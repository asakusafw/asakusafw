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
package com.asakusafw.dmdl.directio.hive.parquet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import parquet.column.ParquetProperties.WriterVersion;
import parquet.hadoop.metadata.CompressionCodecName;

import com.asakusafw.directio.hive.parquet.AbstractParquetFileFormat;
import com.asakusafw.directio.hive.parquet.ParquetFormatConfiguration;
import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.dmdl.directio.hive.common.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.hive.common.HiveDataModelEmitter;

/**
 * Test for {@link ParquetFileEmitter}.
 */
public class ParquetFileEmitterTest extends GeneratorTesterRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new ParquetFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.parquet",
                "model = {",
                "  simple : INT;",
                "};"
        });
        AbstractParquetFileFormat<?> format = load(loader, "ModelParquetFileFormat");
        assertThat(format.getDataModelDescriptor().getDataModelClass(), equalTo((Object) loader.modelType("Model")));
        assertThat(format.getTableName(), is("model"));

        ParquetFormatConfiguration conf = format.getFormatConfiguration();
        assertThat(conf.getCompressionCodecName(), is(nullValue()));
        assertThat(conf.getBlockSize(), is(nullValue()));
        assertThat(conf.getDataPageSize(), is(nullValue()));
        assertThat(conf.getDictionaryPageSize(), is(nullValue()));
        assertThat(conf.getEnableDictionary(), is(nullValue()));
        assertThat(conf.getEnableValidation(), is(nullValue()));
        assertThat(conf.getWriterVersion(), is(nullValue()));

        assertThat(loader.exists(ParquetFileEmitter.CATEGORY, "AbstractModelParquetFileInputDescription"), is(true));
        assertThat(loader.exists(ParquetFileEmitter.CATEGORY, "AbstractModelParquetFileOutputDescription"), is(true));
    }

    /**
     * w/o attribute.
     */
    @Test
    public void wo_mark() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new ParquetFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "model = {",
                "  simple : INT;",
                "};"
        });
        assertThat(loader.exists(ParquetFileEmitter.CATEGORY, "ModelParquetFileFormat"), is(false));
    }

    /**
     * all attributes.
     */
    @Test
    public void attributes() {
        emitDrivers.add(new HiveDataModelEmitter());
        emitDrivers.add(new ParquetFileEmitter());
        ModelLoader loader = generateJava(new String[] {
                "@directio.hive.parquet(",
                "  table_name = 'attributes_test',",
                "  field_mapping = 'name',",
                "  on_missing_source = 'ignore',",
                "  on_missing_target = 'fail',",
                "  on_incompatible_type = 'logging',",
                "  format_version = 'v2',",
                "  compression = 'uncompressed',",
                "  block_size = 100000000,",
                "  data_page_size = 1000001,",
                "  dictionary_page_size = 1000002,",
                "  enable_dictionary = FALSE,",
                "  enable_validation = TRUE,",
                ")",
                "model = { simple : INT; };"
        });
        AbstractParquetFileFormat<?> format = load(loader, "ModelParquetFileFormat");
        assertThat(format.getDataModelDescriptor().getDataModelClass(), equalTo((Object) loader.modelType("Model")));
        assertThat(format.getTableName(), is("attributes_test"));

        ParquetFormatConfiguration conf = format.getFormatConfiguration();
        assertThat(conf.getFieldMappingStrategy(), is(FieldMappingStrategy.NAME));
        assertThat(conf.getOnMissingSource(), is(ExceptionHandlingStrategy.IGNORE));
        assertThat(conf.getOnMissingTarget(), is(ExceptionHandlingStrategy.FAIL));
        assertThat(conf.getOnIncompatibleType(), is(ExceptionHandlingStrategy.LOGGING));
        assertThat(conf.getCompressionCodecName(), is(CompressionCodecName.UNCOMPRESSED));
        assertThat(conf.getBlockSize(), is(100000000));
        assertThat(conf.getDataPageSize(), is(1000001));
        assertThat(conf.getDictionaryPageSize(), is(1000002));
        assertThat(conf.getEnableDictionary(), is(false));
        assertThat(conf.getEnableValidation(), is(true));
        assertThat(conf.getWriterVersion(), is(WriterVersion.PARQUET_2_0));
    }

    private AbstractParquetFileFormat<?> load(ModelLoader loader, String simpleName) {
        return (AbstractParquetFileFormat<?>) loader.newObject(ParquetFileEmitter.CATEGORY, simpleName);
    }
}
