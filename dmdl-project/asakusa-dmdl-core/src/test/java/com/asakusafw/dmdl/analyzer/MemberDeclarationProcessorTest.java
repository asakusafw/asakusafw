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
package com.asakusafw.dmdl.analyzer;

import org.junit.Test;

import com.asakusafw.dmdl.DmdlTesterRoot;
import com.asakusafw.dmdl.analyzer.driver.NamespaceDriver;

/**
 * Test for {@code MemberDeclarationProcessor}.
 */
public class MemberDeclarationProcessorTest extends DmdlTesterRoot {

    /**
     * reference - type.
     */
    @Test
    public void reference_type_only() {
        resolve(new String[] {
                "projective simple = {",
                "  a : INT;",
                "  r : {INT};",
                "};",
        });
    }

    /**
     * reference - expr.
     */
    @Test
    public void reference_expr_only() {
        resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  r = {a};",
                "};",
        });
    }

    /**
     * reference - type + expr.
     */
    @Test
    public void reference_type_expr() {
        resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  r : {INT} = {a};",
                "};",
        });
    }

    /**
     * reference - qualified name.
     */
    @Test
    public void reference_name() {
        resolve(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "simple = p0 + {",
                "  r = p0.r;",
                "};",
        });
    }

    /**
     * reference - list.
     */
    @Test
    public void reference_list() {
        resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  r = {a, b, c};",
                "};",
        });
    }

    /**
     * reference - list w/ duplicated references.
     */
    @Test
    public void reference_list_dup() {
        resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  r = {a, b, b, c};",
                "};",
        });
    }

    /**
     * reference - map.
     */
    @Test
    public void reference_map() {
        resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  r = {`A`:a, `B`:b, `C`:c};",
                "};",
        });
    }

    /**
     * reference - map w/ duplicate reference.
     */
    @Test
    public void reference_map_dup() {
        resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  r = {`A`:a, `B`:b, `C`:c, `?`:b,};",
                "};",
        });
    }

    /**
     * reference - w/ attributes.
     */
    @Test
    public void invalid_reference_attributes() {
        attributeDrivers.add(new NamespaceDriver());
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  @namespace(value=com.example)",
                "  r = {a};",
                "};",
        });
    }

    /**
     * reference - simple name.
     */
    @Test
    public void invalid_reference_sname() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "simple = p0 + {",
                "  r = r;",
                "};",
        });
    }

    /**
     * reference - extra qualified name.
     */
    @Test
    public void invalid_reference_long_name() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "simple = p0 + {",
                "  r = p0.r.a;",
                "};",
        });
    }

    /**
     * reference - list with literal.
     */
    @Test
    public void invalid_reference_list_literal() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  r = {`a`};",
                "};",
        });
    }

    /**
     * reference - map with integer key.
     */
    @Test
    public void invalid_reference_map_key_int() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  r = {1:a};",
                "};",
        });
    }

    /**
     * reference - map with literal value.
     */
    @Test
    public void invalid_reference_map_value_literal() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  r = {`A`:`a`};",
                "};",
        });
    }

    /**
     * reference - map w/ conflict keys.
     */
    @Test
    public void invalid_reference_map_conflict_key() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "  r = {`A`:`a`, `A`:b};",
                "};",
        });
    }
}
