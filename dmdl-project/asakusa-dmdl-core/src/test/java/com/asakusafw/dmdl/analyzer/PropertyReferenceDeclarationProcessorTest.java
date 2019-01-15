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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.asakusafw.dmdl.DmdlTesterRoot;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertyReferenceDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;

/**
 * Test for {@code PropertyReferenceDeclarationProcessor}.
 */
public class PropertyReferenceDeclarationProcessorTest extends DmdlTesterRoot {

    /**
     * w/ list - simple.
     */
    @Test
    public void reference_list() {
        DmdlSemantics env = resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  all = {a, b, c};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration a = model.findPropertyDeclaration("a");
        PropertyDeclaration b = model.findPropertyDeclaration("b");
        PropertyDeclaration c = model.findPropertyDeclaration("c");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().asList(), contains(a.getSymbol(), b.getSymbol(), c.getSymbol()));
    }

    /**
     * w/ map - simple.
     */
    @Test
    public void reference_map() {
        DmdlSemantics env = resolve(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  all = {`A`:a, `B`:b, `C`:c};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration a = model.findPropertyDeclaration("a");
        PropertyDeclaration b = model.findPropertyDeclaration("b");
        PropertyDeclaration c = model.findPropertyDeclaration("c");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        Map<String, PropertySymbol> map = all.getReference().asMap();
        assertThat(map.size(), is(3));
        assertThat(map, hasEntry("A", a.getSymbol()));
        assertThat(map, hasEntry("B", b.getSymbol()));
        assertThat(map, hasEntry("C", c.getSymbol()));
    }

    /**
     * w/ list - stub.
     */
    @Test
    public void reference_list_stub() {
        DmdlSemantics env = resolve(new String[] {
                "projective simple = {",
                "  all : {INT};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().isStub(), is(true));
    }

    /**
     * w/ map - stub.
     */
    @Test
    public void reference_map_stub() {
        DmdlSemantics env = resolve(new String[] {
                "projective simple = {",
                "  all : {:INT};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().isStub(), is(true));
    }

    /**
     * w/ list - empty.
     */
    @Test
    public void reference_list_empty() {
        DmdlSemantics env = resolve(new String[] {
                "projective simple = {",
                "  all : {INT} = {};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().isStub(), is(false));
        assertThat(all.getReference().getAllReferences(), hasSize(0));
    }

    /**
     * w/ map - empty.
     */
    @Test
    public void reference_map_empty() {
        DmdlSemantics env = resolve(new String[] {
                "projective simple = {",
                "  all : {:INT} = {:};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().isStub(), is(false));
        assertThat(all.getReference().getAllReferences(), hasSize(0));
    }

    /**
     * w/ implicitly inherit reference.
     */
    @Test
    public void inherit_implicit() {
        DmdlSemantics env = resolve(new String[] {
                "parent = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  r = {a};",
                "};",
                "simple = parent;",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration a = model.findPropertyDeclaration("a");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("r");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().asList(), contains(a.getSymbol()));
    }

    /**
     * override inherited reference.
     */
    @Test
    public void inherit_override() {
        DmdlSemantics env = resolve(new String[] {
                "parent = {",
                "  a : INT;",
                "  b : INT;",
                "  c : INT;",
                "  r = {a};",
                "};",
                "simple = parent + {",
                "  r = {a, b};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration a = model.findPropertyDeclaration("a");
        PropertyDeclaration b = model.findPropertyDeclaration("b");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("r");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().asList(), contains(a.getSymbol(), b.getSymbol()));
    }

    /**
     * override inherited reference by explicit reference.
     */
    @Test
    public void inherit_reference() {
        DmdlSemantics env = resolve(new String[] {
                "p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "p1 = {",
                "  b : INT;",
                "  r = {b};",
                "};",
                "simple = p0 + p1 + {",
                "  r = p1.r;",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration b = model.findPropertyDeclaration("b");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("r");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().asList(), contains(b.getSymbol()));
    }

    /**
     * inherit reference.
     */
    @Test
    public void inherit_determine() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r : {INT};",
                "};",
                "projective p1 = {",
                "  b : INT;",
                "  r = {b};",
                "};",
                "projective p2 = {",
                "  c : INT;",
                "  r : {INT};",
                "};",
                "simple = p0 + p1 + p2;",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration b = model.findPropertyDeclaration("b");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("r");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().asList(), contains(b.getSymbol()));
    }

    /**
     * inherit equivalent references.
     */
    @Test
    public void inherit_equivalent_list() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "projective p1 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "simple = p0 + p1;",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration a = model.findPropertyDeclaration("a");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("r");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().asList(), contains(a.getSymbol()));
    }

    /**
     * inherit equivalent references.
     */
    @Test
    public void inherit_equivalent_map() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {`A`:a};",
                "};",
                "projective p1 = {",
                "  a : INT;",
                "  r = {`A`:a};",
                "};",
                "simple = p0 + p1;",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyDeclaration a = model.findPropertyDeclaration("a");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("r");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().asMap(), hasEntry("A", a.getSymbol()));
    }

    /**
     * inherit stubs.
     */
    @Test
    public void inherit_stubs() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r : {INT};",
                "};",
                "projective p1 = {",
                "  b : INT;",
                "  r : {INT};",
                "};",
                "projective simple = p0 + p1;",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("r");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
        assertThat(all.getReference().isStub(), is(true));
    }

    /**
     * inherit type for empty list.
     */
    @Test
    public void inherit_type_list() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  all : {INT};",
                "};",
                "simple = p0 + {",
                "  all = {};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
    }

    /**
     * inherit type for empty map.
     */
    @Test
    public void inherit_type_map() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  all : {:INT};",
                "};",
                "simple = p0 + {",
                "  all = {:};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration all = model.findPropertyReferenceDeclaration("all");
        assertThat(all.getType(), is(type(BasicTypeKind.INT)));
    }

    /**
     * inherit description.
     */
    @Test
    public void inherit_description() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  'P0'",
                "  r : {INT};",
                "};",
                "simple = p0 +  {",
                "  r = {a};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration r = model.findPropertyReferenceDeclaration("r");
        assertThat(r.getDescription(), is(notNullValue()));
        assertThat(r.getDescription().getText(), is("P0"));
    }

    /**
     * inherit description - ambiguous.
     */
    @Test
    public void inherit_description_ambiguous() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  'P0'",
                "  r : {INT};",
                "};",
                "projective p1 = {",
                "  'P1'",
                "  r : {INT};",
                "};",
                "simple = p0 + p1 + {",
                "  a : INT;",
                "  r = {a};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration r = model.findPropertyReferenceDeclaration("r");
        assertThat(r.getDescription(), is(nullValue()));
    }

    /**
     * inherit description - ambiguous (present/absent).
     */
    @Test
    public void inherit_description_ambiguous_absent() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  r : {INT};",
                "};",
                "projective p1 = {",
                "  'P1'",
                "  r : {INT};",
                "};",
                "simple = p0 + p1 + {",
                "  a : INT;",
                "  r = {a};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration r = model.findPropertyReferenceDeclaration("r");
        assertThat(r.getDescription(), is(nullValue()));
    }

    /**
     * inherit description - equivalent.
     */
    @Test
    public void inherit_description_equivalent() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  'R'",
                "  r : {INT};",
                "};",
                "projective p1 = {",
                "  'R'",
                "  r : {INT};",
                "};",
                "simple = p0 + p1 + {",
                "  a : INT;",
                "  r = {a};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration r = model.findPropertyReferenceDeclaration("r");
        assertThat(r.getDescription(), is(notNullValue()));
        assertThat(r.getDescription().getText(), is("R"));
    }

    /**
     * inherit description - explicitly select.
     */
    @Test
    public void inherit_description_select() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  'P0'",
                "  r : {INT} = {};",
                "};",
                "projective p1 = {",
                "  a : INT;",
                "  'P1'",
                "  r = {a};",
                "};",
                "simple = p0 + p1 + {",
                "  r = p1.r;",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration r = model.findPropertyReferenceDeclaration("r");
        assertThat(r.getDescription(), is(notNullValue()));
        assertThat(r.getDescription().getText(), is("P1"));
    }

    /**
     * inherit description - explicit description.
     */
    @Test
    public void inherit_description_override() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  'P0'",
                "  r : {INT};",
                "};",
                "simple = p0 +  {",
                "  'SIMPLE'",
                "  r = {a};",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration r = model.findPropertyReferenceDeclaration("r");
        assertThat(r.getDescription(), is(notNullValue()));
        assertThat(r.getDescription().getText(), is("SIMPLE"));
    }

    /**
     * inherit description - explicitly select w/ explicit description.
     */
    @Test
    public void inherit_description_select_override() {
        DmdlSemantics env = resolve(new String[] {
                "projective p0 = {",
                "  'P0'",
                "  r : {INT} = {};",
                "};",
                "projective p1 = {",
                "  a : INT;",
                "  'P1'",
                "  r = {a};",
                "};",
                "simple = p0 + p1 + {",
                "  'SIMPLE'",
                "  r = p1.r;",
                "};",
        });
        ModelDeclaration model = env.findModelDeclaration("simple");
        PropertyReferenceDeclaration r = model.findPropertyReferenceDeclaration("r");
        assertThat(r.getDescription(), is(notNullValue()));
        assertThat(r.getDescription().getText(), is("SIMPLE"));
    }

    /**
     * invalid - invalid element type.
     */
    @Test
    public void invalid_scalar_type() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r : INT = {};",
                "};",
        });
    }

    /**
     * invalid - invalid element type.
     */
    @Test
    public void invalid_unknown_element_type() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r : {a};",
                "};",
        });
    }

    /**
     * invalid - invalid element type.
     */
    @Test
    public void invalid_deep_collection_type() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r : {{INT}};",
                "};",
        });
    }

    /**
     * invalid - inconsistent explicit type.
     */
    @Test
    public void invalid_missing_explicit_type_list() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r = {};",
                "};",
        });
    }

    /**
     * invalid - inconsistent explicit type.
     */
    @Test
    public void invalid_inconsistent_explicit_container_type_list() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r : {:INT} = {};",
                "};",
        });
    }

    /**
     * invalid - inconsistent explicit type.
     */
    @Test
    public void invalid_inconsistent_explicit_element_type_list() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  r : {LONG} = {a};",
                "};",
        });
    }

    /**
     * invalid - inconsistent explicit type.
     */
    @Test
    public void invalid_inconsistent_implicit_type_list() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : LONG;",
                "  r = {a, b};",
                "};",
        });
    }

    /**
     * invalid - inconsistent explicit type.
     */
    @Test
    public void invalid_inconsistent_explicit_container_type_map() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r : {INT} = {:};",
                "};",
        });
    }

    /**
     * invalid - inconsistent explicit type.
     */
    @Test
    public void invalid_inconsistent_explicit_element_type_map() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  r : {:LONG} = {`A`:a};",
                "};",
        });
    }

    /**
     * invalid - inconsistent implicit type.
     */
    @Test
    public void invalid_inconsistent_implicit_type_map() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  a : INT;",
                "  b : LONG;",
                "  r = {`A`:a, `B`:b};",
                "};",
        });
    }

    /**
     * invalid - missing explicit type.
     */
    @Test
    public void invalid_missing_explicit_type_map() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r = {:};",
                "};",
        });
    }

    /**
     * invalid - missing referent.
     */
    @Test
    public void invalid_missing_target_list() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r = {missing};",
                "};",
        });
    }

    /**
     * invalid - missing referent.
     */
    @Test
    public void invalid_missing_target_map() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r = {`A`:missing};",
                "};",
        });
    }

    /**
     * invalid - missing model references.
     */
    @Test
    public void invalid_missing_model_refs() {
        shouldSemanticError(new String[] {
                "simple = missing;",
        });
    }

    /**
     * invalid - conflict to existing property.
     */
    @Test
    public void invalid_conflict_to_property() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  r : {INT};",
                "};",
                "projective simple = p0 + {",
                "  r : INT;",
                "};",
        });
    }

    /**
     * invalid - conflict to existing property.
     */
    @Test
    public void invalid_conflict_from_property() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  r : INT;",
                "};",
                "projective simple = p0 + {",
                "  r : {INT};",
                "};",
        });
    }

    /**
     * invalid - inconsistent collection type of parents.
     */
    @Test
    public void invalid_inconsistent_inherit_collection_type() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  r : {INT};",
                "};",
                "projective p1 = {",
                "  r : {:INT};",
                "};",
                "projective simple = p0 + p1;",
        });
    }

    /**
     * invalid - inconsistent collection type of parents.
     */
    @Test
    public void invalid_inconsistent_inherit_element_type() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  r : {INT};",
                "};",
                "projective p1 = {",
                "  r : {LONG};",
                "};",
                "projective simple = p0 + p1;",
        });
    }

    /**
     * invalid - ambiguous inherit.
     */
    @Test
    public void invalid_inherit_ambiguous() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "projective p1 = {",
                "  b : INT;",
                "  r = {b};",
                "};",
                "simple = p0 + p1;",
        });
    }

    /**
     * invalid - ambiguous inherit.
     */
    @Test
    public void invalid_inherit_ambiguous_map_key() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {`A`:a};",
                "};",
                "projective p1 = {",
                "  a : INT;",
                "  r = {`B`:a};",
                "};",
                "simple = p0 + p1;",
        });
    }

    /**
     * invalid - ambiguous inherit.
     */
    @Test
    public void invalid_inherit_ambiguous_map_value() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {`A`:a};",
                "};",
                "projective p1 = {",
                "  b : INT;",
                "  r = {`A`:b};",
                "};",
                "simple = p0 + p1;",
        });
    }

    /**
     * invalid - stub for non-projective.
     */
    @Test
    public void invalid_stub() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r : {INT};",
                "};",
        });
    }

    /**
     * invalid - reference w/ undefined models.
     */
    @Test
    public void invalid_reference_missing_model() {
        shouldSemanticError(new String[] {
                "simple = {",
                "  r = missing.r;",
                "};",
        });
    }

    /**
     * invalid - reference w/ undefined models.
     */
    @Test
    public void invalid_reference_missing_reference() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "simple = p0 + {",
                "  r = p0.missing;",
                "};",
        });
    }

    /**
     * invalid - reference to invalid member.
     */
    @Test
    public void invalid_reference_not_reference() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "simple = p0 + {",
                "  r = p0.a;",
                "};",
        });
    }

    /**
     * invalid - reference to stub.
     */
    @Test
    public void invalid_reference_stub() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  r : {INT};",
                "};",
                "simple = p0 + {",
                "  r = p0.r;",
                "};",
        });
    }

    /**
     * invalid - reference w/ inconsistent type.
     */
    @Test
    public void invalid_reference_inconsistent_type() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  a : INT;",
                "  r = {a};",
                "};",
                "simple = p0 + {",
                "  r : {LONG} = p0.r;",
                "};",
        });
    }

    /**
     * invalid - reference w/ inconsistent type.
     */
    @Test
    public void invalid_reference_inconsistent_container() {
        shouldSemanticError(new String[] {
                "projective p0 = {",
                "  r : {INT};",
                "};",
                "simple = p0 + {",
                "  r = {:};",
                "};",
        });
    }
}
