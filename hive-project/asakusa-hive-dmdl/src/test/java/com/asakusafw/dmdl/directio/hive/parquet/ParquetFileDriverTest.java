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
package com.asakusafw.dmdl.directio.hive.parquet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import parquet.column.ParquetProperties.WriterVersion;
import parquet.hadoop.metadata.CompressionCodecName;

import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.dmdl.directio.hive.common.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.hive.common.HiveDataModelTrait;
import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Test for {@link ParquetFileDriver}.
 */
public class ParquetFileDriverTest extends GeneratorTesterRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        ModelDeclaration model = analyze(new String[] {
                "@directio.hive.parquet",
                "model = { simple : INT; };"
        }).findModelDeclaration("model");
        assertThat(model.getTrait(HiveDataModelTrait.class), is(notNullValue()));

        ParquetFileTrait trait = model.getTrait(ParquetFileTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getOriginalAst(), is(notNullValue()));
        assertThat(trait.getTableName(), is(nullValue()));
        assertThat(trait.configuration().getFieldMappingStrategy(), is(nullValue()));
        assertThat(trait.configuration().getOnMissingSource(), is(nullValue()));
        assertThat(trait.configuration().getOnMissingTarget(), is(nullValue()));
        assertThat(trait.configuration().getOnIncompatibleType(), is(nullValue()));
        assertThat(trait.configuration().getCompressionCodecName(), is(nullValue()));
        assertThat(trait.configuration().getBlockSize(), is(nullValue()));
        assertThat(trait.configuration().getDataPageSize(), is(nullValue()));
        assertThat(trait.configuration().getDictionaryPageSize(), is(nullValue()));
        assertThat(trait.configuration().getEnableDictionary(), is(nullValue()));
        assertThat(trait.configuration().getEnableValidation(), is(nullValue()));
        assertThat(trait.configuration().getWriterVersion(), is(nullValue()));
    }

    /**
     * explicitly defines all attributes.
     */
    @Test
    public void attributes() {
        ModelDeclaration model = analyze(new String[] {
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
        }).findModelDeclaration("model");
        assertThat(model.getTrait(HiveDataModelTrait.class), is(notNullValue()));

        ParquetFileTrait trait = model.getTrait(ParquetFileTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getOriginalAst(), is(notNullValue()));
        assertThat(trait.getTableName(), is("attributes_test"));
        assertThat(trait.configuration().getFieldMappingStrategy(), is(FieldMappingStrategy.NAME));
        assertThat(trait.configuration().getOnMissingSource(), is(ExceptionHandlingStrategy.IGNORE));
        assertThat(trait.configuration().getOnMissingTarget(), is(ExceptionHandlingStrategy.FAIL));
        assertThat(trait.configuration().getOnIncompatibleType(), is(ExceptionHandlingStrategy.LOGGING));
        assertThat(trait.configuration().getCompressionCodecName(), is(CompressionCodecName.UNCOMPRESSED));
        assertThat(trait.configuration().getBlockSize(), is(100000000));
        assertThat(trait.configuration().getDataPageSize(), is(1000001));
        assertThat(trait.configuration().getDictionaryPageSize(), is(1000002));
        assertThat(trait.configuration().getEnableDictionary(), is(false));
        assertThat(trait.configuration().getEnableValidation(), is(true));
        assertThat(trait.configuration().getWriterVersion(), is(WriterVersion.PARQUET_2_0));
    }

    /**
     * unsupported type - decimal.
     */
    @Test
    public void invalid_decimal() {
        shouldSemanticError(new String[] {
                "@directio.hive.parquet",
                "model = { simple : DECIMAL; };"
        });
    }

    /**
     * unsupported type - date.
     */
    @Test
    public void invalid_date() {
        shouldSemanticError(new String[] {
                "@directio.hive.parquet",
                "model = { simple : DATE; };"
        });
    }

    /**
     * unsupported type - datetime.
     */
    @Test
    public void invalid_date_time() {
        shouldSemanticError(new String[] {
                "@directio.hive.parquet",
                "model = { simple : DATETIME; };"
        });
    }

    /**
     * unsupported type but it is ignored.
     */
    @Test
    public void invalid_ignore() {
        // ok.
        analyze(new String[] {
                "@directio.hive.parquet",
                "model = {",
                "  value : INT;",
                "  @directio.hive.ignore",
                "  ignored : DECIMAL;",
                "};",
        });
    }

    /**
     * unknown format version.
     */
    @Test
    public void invalid_format_version() {
        shouldSemanticError(new String[] {
                "@directio.hive.parquet(",
                "  format_version = 'UNKNOWN',",
                ")",
                "model = { simple : INT; };"
        });
    }

    /**
     * unknown compression type.
     */
    @Test
    public void invalid_compression() {
        shouldSemanticError(new String[] {
                "@directio.hive.parquet(",
                "  compression = 'UNKNOWN',",
                ")",
                "model = { simple : INT; };"
        });
    }
}
