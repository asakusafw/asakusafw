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
package com.asakusafw.dmdl.directio.hive.common;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.junit.Test;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;

/**
 * Test for {@link HiveDecimalDriver}.
 */
public class HiveDecimalDriverTest extends GeneratorTesterRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                "  @directio.hive.decimal(precision = 10, scale = 2)",
                "  simple : DECIMAL;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getTypeKind(), is(HiveFieldTrait.TypeKind.DECIMAL));
        assertThat(trait.getDecimalPrecision(), is(10));
        assertThat(trait.getDecimalScale(), is(2));
    }

    /**
     * minimum case.
     */
    @Test
    public void minimum() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                "  @directio.hive.decimal(precision = 1, scale = 0)",
                "  simple : DECIMAL;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getDecimalPrecision(), is(1));
        assertThat(trait.getDecimalScale(), is(0));
    }

    /**
     * maximum case.
     */
    @Test
    public void maximum() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                String.format("  @directio.hive.decimal(precision = %d, scale = %d)",
                        HiveDecimal.MAX_PRECISION,
                        HiveDecimal.MAX_SCALE),
                "  simple : DECIMAL;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getDecimalPrecision(), is(HiveDecimal.MAX_PRECISION));
        assertThat(trait.getDecimalScale(), is(HiveDecimal.MAX_SCALE));
    }

    /**
     * with invalid type.
     */
    @Test
    public void invalid_type() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.decimal(precision = 10, scale = 2)",
                "  simple : INT;",
                "};"
        });
    }

    /**
     * w/o precision.
     */
    @Test
    public void invalid_wo_precision() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.decimal(scale = 2)",
                "  simple : DECIMAL;",
                "};"
        });
    }

    /**
     * w/ small precision.
     */
    @Test
    public void invalid_precision_small() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.decimal(precision = 0, scale = 2)",
                "  simple : DECIMAL;",
                "};"
        });
    }

    /**
     * w/ large precision.
     */
    @Test
    public void invalid_precision_large() {
        shouldSemanticError(new String[] {
                "model = {",
                String.format("  @directio.hive.decimal(precision = %d, scale = %d)",
                        HiveDecimal.MAX_PRECISION + 1,
                        0),
                "  simple : DECIMAL;",
                "};"
        });
    }

    /**
     * w/o scale.
     */
    @Test
    public void invalid_wo_scale() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.decimal(precision = 10)",
                "  simple : DECIMAL;",
                "};"
        });
    }

    /**
     * w/ small scale.
     */
    @Test
    public void invalid_scale_small() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.decimal(precision = 10, scale = -1)",
                "  simple : DECIMAL;",
                "};"
        });
    }

    /**
     * w/ large scale.
     */
    @Test
    public void invalid_scale_large() {
        shouldSemanticError(new String[] {
                "model = {",
                String.format("  @directio.hive.decimal(precision = %d, scale = %d)",
                        HiveDecimal.MAX_PRECISION,
                        HiveDecimal.MAX_SCALE + 1),
                "  simple : DECIMAL;",
                "};"
        });
    }

    /**
     * w/ precision {@code <} scale.
     */
    @Test
    public void invalid_precision_lt_scale() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.decimal(precision = 10, scale = 11)",
                "  simple : DECIMAL;",
                "};"
        });
    }
}
