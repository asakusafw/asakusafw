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
package com.asakusafw.dmdl.directio.hive.orc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

import org.apache.hadoop.hive.ql.io.orc.CompressionKind;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.junit.Test;

import com.asakusafw.directio.hive.serde.DataModelMapping.ExceptionHandlingStrategy;
import com.asakusafw.directio.hive.serde.DataModelMapping.FieldMappingStrategy;
import com.asakusafw.dmdl.directio.hive.common.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.hive.common.HiveDataModelTrait;
import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Test for {@link OrcFileDriver}.
 */
public class OrcFileDriverTest extends GeneratorTesterRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        ModelDeclaration model = analyze(new String[] {
                "@directio.hive.orc",
                "model = { simple : INT; };"
        }).findModelDeclaration("model");
        assertThat(model.getTrait(HiveDataModelTrait.class), is(notNullValue()));

        OrcFileTrait trait = model.getTrait(OrcFileTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getOriginalAst(), is(notNullValue()));
        assertThat(trait.getTableName(), is(nullValue()));
        assertThat(trait.configuration().getFieldMappingStrategy(), is(nullValue()));
        assertThat(trait.configuration().getOnMissingSource(), is(nullValue()));
        assertThat(trait.configuration().getOnMissingTarget(), is(nullValue()));
        assertThat(trait.configuration().getOnIncompatibleType(), is(nullValue()));
        assertThat(trait.configuration().getFormatVersion(), is(nullValue()));
        assertThat(trait.configuration().getCompressionKind(), is(nullValue()));
        assertThat(trait.configuration().getStripeSize(), is(nullValue()));
    }

    /**
     * explicitly defines all attributes.
     */
    @Test
    public void attributes() {
        ModelDeclaration model = analyze(new String[] {
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
        }).findModelDeclaration("model");

        OrcFileTrait trait = model.getTrait(OrcFileTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getOriginalAst(), is(notNullValue()));
        assertThat(trait.getTableName(), is("attributes_test"));
        assertThat(trait.configuration().getFieldMappingStrategy(), is(FieldMappingStrategy.NAME));
        assertThat(trait.configuration().getOnMissingSource(), is(ExceptionHandlingStrategy.IGNORE));
        assertThat(trait.configuration().getOnMissingTarget(), is(ExceptionHandlingStrategy.FAIL));
        assertThat(trait.configuration().getOnIncompatibleType(), is(ExceptionHandlingStrategy.LOGGING));
        assertThat(trait.configuration().getFormatVersion(), is(OrcFile.Version.V_0_11));
        assertThat(trait.configuration().getCompressionKind(), is(CompressionKind.ZLIB));
        assertThat(trait.configuration().getStripeSize(), is(123456789L));
    }

    /**
     * table name is empty.
     */
    @Test
    public void invalid_table_name_empty() {
        shouldSemanticError(new String[] {
                "@directio.hive.orc(table_name = '')",
                "model = { simple : INT; };"
        });
    }

    /**
     * table name is not string.
     */
    @Test
    public void invalid_table_name_not_string() {
        shouldSemanticError(new String[] {
                "@directio.hive.orc(table_name = 1)",
                "model = { simple : INT; };"
        });
    }

    /**
     * format version unknown.
     */
    @Test
    public void invalid_format_version_unknown() {
        shouldSemanticError(new String[] {
                "@directio.hive.orc(format_version = '___UNKNOWN___')",
                "model = { simple : INT; };"
        });
    }

    /**
     * compression kind is unknown.
     */
    @Test
    public void invalid_compression_kind_unknown() {
        shouldSemanticError(new String[] {
                "@directio.hive.orc(compression = '___UNKNOWN___')",
                "model = { simple : INT; };"
        });
    }

    /**
     * stripe size is too small.
     */
    @Test
    public void invalid_stripe_size_too_small() {
        shouldSemanticError(new String[] {
                "@directio.hive.orc(stripe_size = 0)",
                "model = { simple : INT; };"
        });
    }

    /**
     * stripe size is too large.
     */
    @Test
    public void invalid_stripe_size_too_large() {
        shouldSemanticError(new String[] {
                "@directio.hive.orc(stripe_size = " + BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE) + ")",
                "model = { simple : INT; };"
        });
    }
}
