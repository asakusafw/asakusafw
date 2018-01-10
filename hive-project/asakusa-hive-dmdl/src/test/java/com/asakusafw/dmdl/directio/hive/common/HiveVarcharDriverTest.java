/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * Test for {@link HiveVarcharDriver}.
 */
public class HiveVarcharDriverTest extends GeneratorTesterRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                "  @directio.hive.varchar(length = 3)",
                "  simple : TEXT;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getTypeKind(), is(HiveFieldTrait.TypeKind.VARCHAR));
        assertThat(trait.getStringLength(), is(3));
    }

    /**
     * with invalid type.
     */
    @Test
    public void invalid_type() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.char(length = 3)",
                "  simple : INT;",
                "};"
        });
    }

    /**
     * without length property.
     */
    @Test
    public void invalid_wo_length() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.varchar",
                "  simple : TEXT;",
                "};"
        });
    }

    /**
     * with too small length.
     */
    @Test
    public void invalid_too_small() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.varchar(length = -1)",
                "  simple : TEXT;",
                "};"
        });
    }

    /**
     * with too large length.
     */
    @Test
    public void invalid_too_large() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.varchar(length = 65536)",
                "  simple : TEXT;",
                "};"
        });
    }

    /**
     * conflict between char and varchar.
     */
    @Test
    public void invalid_conflict() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.char(length = 10)",
                "  @directio.hive.varchar(length = 10)",
                "  simple : TEXT;",
                "};"
        });
    }
}
