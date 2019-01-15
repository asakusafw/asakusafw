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

import org.junit.Test;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;

/**
 * Test for {@link HiveStringDriver}.
 */
public class HiveStringDriverTest extends GeneratorTesterRoot {

    /**
     * for date.
     */
    @Test
    public void date() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                "  @directio.hive.string",
                "  simple : DATE;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getTypeKind(), is(HiveFieldTrait.TypeKind.STRING));
    }

    /**
     * for datetime.
     */
    @Test
    public void datetime() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                "  @directio.hive.string",
                "  simple : DATETIME;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getTypeKind(), is(HiveFieldTrait.TypeKind.STRING));
    }

    /**
     * for decimal.
     */
    @Test
    public void decimal() {
        ModelDeclaration model = analyze(new String[] {
                "model = {",
                "  @directio.hive.string",
                "  simple : DECIMAL;",
                "};"
        }).findModelDeclaration("model");
        PropertyDeclaration property = model.findPropertyDeclaration("simple");
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        assertThat(trait, is(notNullValue()));
        assertThat(trait.getTypeKind(), is(HiveFieldTrait.TypeKind.STRING));
    }

    /**
     * with invalid type.
     */
    @Test
    public void invalid_type() {
        shouldSemanticError(new String[] {
                "model = {",
                "  @directio.hive.timestamp",
                "  simple : INT;",
                "};"
        });
    }
}
