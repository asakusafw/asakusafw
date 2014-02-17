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
package com.asakusafw.dmdl.analyzer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.text.MessageFormat;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.dmdl.DmdlTesterRoot;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.semantics.trait.JoinTrait;
import com.asakusafw.dmdl.semantics.trait.MappingFactor;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;
import com.asakusafw.dmdl.semantics.trait.ReduceTerm;
import com.asakusafw.dmdl.semantics.trait.SummarizeTrait;

/**
 * Test for {@link DmdlAnalyzer}.
 */
public class DmdlAnalyzerTest extends DmdlTesterRoot {

    /**
     * simple record.
     */
    @Test
    public void simple() {
        DmdlSemantics resolved = resolve();
        ModelDeclaration simple = resolved.findModelDeclaration("simple");
        assertThat(simple, not(nullValue()));
        PropertyDeclaration property = simple.findPropertyDeclaration("a");
        assertThat(property, not(nullValue()));
        assertThat(property.getType(), is(type(BasicTypeKind.INT)));
    }

    /**
     * simple record with projections.
     */
    @Test
    public void projections() {
        DmdlSemantics resolved = resolve();
        ModelDeclaration simple = resolved.findModelDeclaration("simple");
        assertThat(simple, not(nullValue()));

        PropertyDeclaration a = simple.findPropertyDeclaration("a");
        assertThat(a, not(nullValue()));
        assertThat(a.getType(), is(type(BasicTypeKind.INT)));

        PropertyDeclaration b = simple.findPropertyDeclaration("b");
        assertThat(b, not(nullValue()));
        assertThat(b.getType(), is(type(BasicTypeKind.LONG)));

        ProjectionsTrait trait = simple.getTrait(ProjectionsTrait.class);
        assertThat(trait, not(nullValue()));
        assertThat(trait.getProjections(), has(model("a")));
        assertThat(trait.getProjections(), has(model("b")));
    }

    /**
     * joined model.
     */
    @Test
    public void join() {
        DmdlSemantics resolved = resolve();
        ModelDeclaration simple = resolved.findModelDeclaration("simple");
        assertThat(simple, not(nullValue()));

        PropertyDeclaration sid = simple.findPropertyDeclaration("sid");
        assertThat(sid, not(nullValue()));
        assertThat(sid.getType(), is(type(BasicTypeKind.LONG)));

        PropertyDeclaration aValue = simple.findPropertyDeclaration("a_value");
        assertThat(aValue, not(nullValue()));
        assertThat(aValue.getType(), is(type(BasicTypeKind.TEXT)));

        PropertyDeclaration bValue = simple.findPropertyDeclaration("b_value");
        assertThat(bValue, not(nullValue()));
        assertThat(bValue.getType(), is(type(BasicTypeKind.DATE)));

        JoinTrait trait = simple.getTrait(JoinTrait.class);
        assertThat(trait, not(nullValue()));
        assertThat(trait.getTerms().size(), is(2));

        ReduceTerm<AstJoin> aTerm = trait.getTerms().get(0);
        assertThat(aTerm.getSource(), is(model("a")));
        assertThat(aTerm.getGrouping(), has(property("sid")));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.ANY, "sid", "sid")));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.ANY, "value", "a_value")));

        ReduceTerm<AstJoin> bTerm = trait.getTerms().get(1);
        assertThat(bTerm.getSource(), is(model("b")));
        assertThat(bTerm.getGrouping(), has(property("sid")));
        assertThat(bTerm.getMappings(), has(mapping(PropertyMappingKind.ANY, "sid", "sid")));
        assertThat(bTerm.getMappings(), has(mapping(PropertyMappingKind.ANY, "value", "b_value")));
    }

    /**
     * summarized model.
     */
    @Test
    public void summarize() {
        DmdlSemantics resolved = resolve();
        ModelDeclaration simple = resolved.findModelDeclaration("simple");
        assertThat(simple, not(nullValue()));

        PropertyDeclaration key = simple.findPropertyDeclaration("key");
        assertThat(key, not(nullValue()));
        assertThat(key.getType(), is(type(BasicTypeKind.INT)));

        PropertyDeclaration sum = simple.findPropertyDeclaration("sum");
        assertThat(sum, not(nullValue()));
        assertThat(sum.getType(), is(type(BasicTypeKind.LONG)));

        PropertyDeclaration count = simple.findPropertyDeclaration("count");
        assertThat(count, not(nullValue()));
        assertThat(count.getType(), is(type(BasicTypeKind.LONG)));

        PropertyDeclaration max = simple.findPropertyDeclaration("max");
        assertThat(max, not(nullValue()));
        assertThat(max.getType(), is(type(BasicTypeKind.DATE)));

        PropertyDeclaration min = simple.findPropertyDeclaration("min");
        assertThat(min, not(nullValue()));
        assertThat(min.getType(), is(type(BasicTypeKind.DATE)));

        SummarizeTrait trait = simple.getTrait(SummarizeTrait.class);
        assertThat(trait, not(nullValue()));
        assertThat(trait.getTerms().size(), is(1));

        ReduceTerm<AstSummarize> aTerm = trait.getTerms().get(0);
        assertThat(aTerm.getSource(), is(model("a")));
        assertThat(aTerm.getGrouping(), has(property("key")));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.ANY, "value_a", "key")));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.SUM, "value_b", "sum")));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.COUNT, "value_a", "count")));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.MAX, "value_c", "max")));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.MIN, "value_c", "min")));
    }

    /**
     * summarized model.
     */
    @Test
    public void summarize_whole() {
        DmdlSemantics resolved = resolve();
        ModelDeclaration counter = resolved.findModelDeclaration("counter");
        assertThat(counter, not(nullValue()));

        PropertyDeclaration count = counter.findPropertyDeclaration("count");
        assertThat(count, not(nullValue()));
        assertThat(count.getType(), is(type(BasicTypeKind.LONG)));

        SummarizeTrait trait = counter.getTrait(SummarizeTrait.class);
        assertThat(trait, not(nullValue()));
        assertThat(trait.getTerms().size(), is(1));

        ReduceTerm<AstSummarize> aTerm = trait.getTerms().get(0);
        assertThat(aTerm.getSource(), is(model("simple")));
        assertThat(aTerm.getGrouping().size(), is(0));
        assertThat(aTerm.getMappings(), has(mapping(PropertyMappingKind.COUNT, "sid", "count")));
    }

    /**
     * model duplicated.
     */
    @Test
    public void invalid_duplicate_model() {
        shouldSemanticError();
    }

    /**
     * cyclic dependency detected.
     */
    @Test
    public void invalid_cyclic_dependencies() {
        shouldSemanticError();
    }

    /**
     * unbound model in record model def of right hand side.
     */
    @Test
    public void invalid_unbound_record() {
        shouldSemanticError();
    }

    /**
     * unbound model in joined model def of right hand side.
     */
    @Test
    public void invalid_unbound_join() {
        shouldSemanticError();
    }

    /**
     * unbound model in joined model def of right hand side.
     */
    @Test
    public void invalid_unbound_summarize() {
        shouldSemanticError();
    }

    /**
     * unbound property in joined model def of right hand side.
     */
    @Test
    public void invalid_unbound_mapping() {
        shouldSemanticError();
    }

    /**
     * unbound property in summarized model def of right hand side.
     */
    @Test
    public void invalid_unbound_folding() {
        shouldSemanticError();
    }

    /**
     * unbound property in grouping.
     */
    @Test
    public void invalid_unbound_grouping() {
        shouldSemanticError();
    }

    /**
     * duplicate record property in same record term.
     */
    @Test
    public void invalid_duplicate_record_property() {
        shouldSemanticError();
    }

    /**
     * conflict record property as same name but different type.
     */
    @Test
    public void invalid_conflict_record_property() {
        shouldSemanticError();
    }

    /**
     * unbound types.
     */
    @Test
    public void invalid_unbound_type() {
        typeDrivers.clear();
        shouldSemanticError();
    }

    /**
     * duplicate mapped property in same term.
     */
    @Test
    public void invalid_duplicate_mapping_property() {
        shouldSemanticError();
    }

    /**
     * duplicate mapped property as same name but different type.
     */
    @Test
    public void invalid_conflict_mapping_property() {
        shouldSemanticError();
    }

    /**
     * duplicate mapped property in same term.
     */
    @Test
    public void invalid_unbound_join_key() {
        shouldSemanticError();
    }

    /**
     * number of properties in each grouping is inconsistent.
     */
    @Test
    public void invalid_inconsistent_group_count() {
        shouldSemanticError();
    }

    /**
     * grouping properties in each term has inconsistent type.
     */
    @Test
    public void invalid_inconsistent_group_type() {
        shouldSemanticError();
    }

    /**
     * grouping properties in each term has inconsistent type but both are unified.
     */
    @Test
    public void invalid_inconsistent_group_type_unified() {
        shouldSemanticError();
    }

    /**
     * duplicate joined property in same term.
     */
    @Test
    public void invalid_duplicate_folding_property() {
        shouldSemanticError();
    }

    /**
     * unbound aggregators.
     */
    @Test
    public void invalid_unbound_aggregator() {
        shouldSemanticError();
    }

    /**
     * conflict folded property as same name but different type.
     */
    @Test
    public void invalid_unbound_folding_type() {
        shouldSemanticError();
    }

    /**
     * unknown attributes.
     */
    @Test
    public void invalid_unknown_attribute() {
        shouldSemanticError();
    }

    private Matcher<MappingFactor> mapping(
            final PropertyMappingKind kind,
            final String source,
            final String target) {
        return new BaseMatcher<MappingFactor>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof MappingFactor) {
                    MappingFactor factor = (MappingFactor) object;
                    return factor.getKind() == kind
                        && factor.getSource().getName().identifier.equals(source)
                        && factor.getTarget().getName().identifier.equals(target);
                }
                return false;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(MessageFormat.format(
                        "{0} {1} -> {2}",
                        kind.name().toLowerCase(),
                        source,
                        target));
            }
        };
    }
}
