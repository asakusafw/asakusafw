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

import org.junit.Test;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;

/**
 * Test for {@link HiveFieldDriver}.
 */
public class HiveFieldDriverTest extends GeneratorTesterRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                "  @directio.hive.field(name = 'test_column')",
                "  simple : INT;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getColumnName(), is("test_column"));
        assertThat(trait.getTypeKind(), is(HiveFieldTrait.TypeKind.NATURAL));
        assertThat(trait.isColumnPresent(), is(true));
    }

    /**
     * name is not set.
     */
    @Test
    public void invalid_name_missing() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.field",
                "  simple : INT;",
                "};"
        });
    }

    /**
     * name is not string.
     */
    @Test
    public void invalid_name_non_string() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.field(name = 0)",
                "  simple : INT;",
                "};"
        });
    }

    /**
     * name is empty string.
     */
    @Test
    public void invalid_name_empty() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.field(name = '')",
                "  simple : INT;",
                "};"
        });
    }
}
