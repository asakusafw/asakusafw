/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.dmdl.parser;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.asakusafw.dmdl.DmdlTesterRoot;
import com.asakusafw.dmdl.model.*;
import com.asakusafw.utils.collections.Lists;

/**
 * Test for {@link DmdlParser}.
 */
public class DmdlParserTest extends DmdlTesterRoot {

    /**
     * Parse a simple record definition.
     */
    @Test
    public void record_simple() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstRecord> record = getRecord(script, "simple");
        assertThat(record.description, is(nullValue()));
        assertThat(record.attributes.isEmpty(), is(true));

        List<AstRecord> terms = extract(record.expression);
        assertThat(terms.size(), is(1));
        AstRecord term = terms.get(0);

        assertThat(term, instanceOf(AstRecordDefinition.class));
        AstRecordDefinition def = (AstRecordDefinition) term;

        assertThat(def.properties.size(), is(1));
        AstPropertyDefinition pdef = def.properties.get(0);

        assertThat(pdef.description, is(nullValue()));
        assertThat(pdef.attributes.isEmpty(), is(true));
        assertThat(pdef.name.identifier, is("a"));
        assertThat(pdef.type, is(astType(BasicTypeKind.INT)));
    }

    /**
     * Parse a simple record definition with a model reference.
     */
    @Test
    public void record_reference() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstRecord> record = getRecord(script, "simple");

        List<AstRecord> terms = extract(record.expression);
        assertThat(terms.size(), is(1));
        assertThat(terms.get(0), instanceOf(AstModelReference.class));
        AstModelReference ref = (AstModelReference) terms.get(0);

        assertThat(ref.name.identifier, is("other"));
    }

    /**
     * Parse a record definition with multi references.
     */
    @Test
    public void record_multi_reference() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstRecord> record = getRecord(script, "simple");
        List<AstRecord> terms = extract(record.expression);
        assertThat(terms.size(), is(3));
        assertThat(terms.get(0), instanceOf(AstModelReference.class));
        assertThat(terms.get(1), instanceOf(AstModelReference.class));
        assertThat(terms.get(2), instanceOf(AstModelReference.class));
        AstModelReference a = (AstModelReference) terms.get(0);
        AstModelReference b = (AstModelReference) terms.get(1);
        AstModelReference c = (AstModelReference) terms.get(2);
        assertThat(a.name.identifier, is("a"));
        assertThat(b.name.identifier, is("b"));
        assertThat(c.name.identifier, is("c"));
    }

    /**
     * Parse a record definition with mixed terms.
     */
    @Test
    public void record_mixed() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstRecord> record = getRecord(script, "simple");
        List<AstRecord> terms = extract(record.expression);
        assertThat(terms.size(), is(3));
        assertThat(terms.get(0), instanceOf(AstModelReference.class));
        assertThat(terms.get(1), instanceOf(AstModelReference.class));
        assertThat(terms.get(2), instanceOf(AstRecordDefinition.class));
        AstModelReference a = (AstModelReference) terms.get(0);
        AstModelReference b = (AstModelReference) terms.get(1);
        AstRecordDefinition def = (AstRecordDefinition) terms.get(2);
        assertThat(a.name.identifier, is("a"));
        assertThat(b.name.identifier, is("b"));
        assertThat(def.properties.size(), is(1));
        assertThat(def.properties.get(0).name.identifier, is("c"));
    }

    /**
     * Parse a simple projection definition.
     */
    @Test
    public void projective_simple() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstRecord> record = getProjection(script, "simple");
        assertThat(record.description, is(nullValue()));
        assertThat(record.attributes.isEmpty(), is(true));

        List<AstRecord> terms = extract(record.expression);
        assertThat(terms.size(), is(1));
        AstRecord term = terms.get(0);

        assertThat(term, instanceOf(AstRecordDefinition.class));
        AstRecordDefinition def = (AstRecordDefinition) term;

        assertThat(def.properties.size(), is(1));
        AstPropertyDefinition pdef = def.properties.get(0);

        assertThat(pdef.description, is(nullValue()));
        assertThat(pdef.attributes.isEmpty(), is(true));
        assertThat(pdef.name.identifier, is("a"));
        assertThat(pdef.type, is(astType(BasicTypeKind.INT)));
    }

    /**
     * Parse a simple join model definition.
     */
    @Test
    public void join_simple() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstJoin> joined = getJoined(script, "simple");

        List<AstJoin> terms = extract(joined.expression);
        assertThat(terms.size(), is(2));

        AstJoin left = terms.get(0);
        AstJoin right = terms.get(1);

        assertThat(left.reference.name.identifier, is("left"));
        assertThat(left.mapping, is(nullValue()));
        assertThat(left.grouping, is(nullValue()));

        assertThat(right.reference.name.identifier, is("right"));
        assertThat(right.mapping, is(nullValue()));
        assertThat(right.grouping, is(nullValue()));
    }

    /**
     * Parse a join model definition with model mappings.
     */
    @Test
    public void join_mapping() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstJoin> joined = getJoined(script, "simple");

        List<AstJoin> terms = extract(joined.expression);
        assertThat(terms.size(), is(2));

        AstJoin left = terms.get(0);
        AstJoin right = terms.get(1);

        assertThat(left.reference.name.identifier, is("left"));
        assertThat(left.mapping, not(nullValue()));
        assertThat(left.mapping.properties.size(), is(1));
        assertThat(left.mapping.properties.get(0).source.identifier, is("a"));
        assertThat(left.mapping.properties.get(0).target.identifier, is("b"));

        assertThat(right.reference.name.identifier, is("right"));
        assertThat(right.mapping, not(nullValue()));
        assertThat(right.mapping.properties.size(), is(2));
        assertThat(right.mapping.properties.get(0).source.identifier, is("a"));
        assertThat(right.mapping.properties.get(0).target.identifier, is("c"));
        assertThat(right.mapping.properties.get(1).source.identifier, is("b"));
        assertThat(right.mapping.properties.get(1).target.identifier, is("d"));
    }

    /**
     * Parse a join model definition with join conditions.
     */
    @Test
    public void join_grouping() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstJoin> joined = getJoined(script, "simple");

        List<AstJoin> terms = extract(joined.expression);
        assertThat(terms.size(), is(2));

        AstJoin left = terms.get(0);
        AstJoin right = terms.get(1);

        assertThat(left.reference.name.identifier, is("left"));
        assertThat(left.grouping, not(nullValue()));
        assertThat(left.grouping.properties.size(), is(1));
        assertThat(left.grouping.properties.get(0).identifier, is("a"));

        assertThat(right.reference.name.identifier, is("right"));
        assertThat(right.grouping, not(nullValue()));
        assertThat(right.grouping.properties.size(), is(3));
        assertThat(right.grouping.properties.get(0).identifier, is("a"));
        assertThat(right.grouping.properties.get(1).identifier, is("b"));
        assertThat(right.grouping.properties.get(2).identifier, is("c"));
    }

    /**
     * Parse a simple summarized model definition.
     */
    @Test
    public void summarize_simple() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstSummarize> summarized = getSummarized(script, "simple");

        List<AstSummarize> terms = extract(summarized.expression);
        assertThat(terms.size(), is(1));
        AstSummarize term = terms.get(0);

        assertThat(term.reference.name.identifier, is("source"));
        assertThat(term.folding.properties.size(), is(1));
        assertThat(term.folding.properties.get(0).aggregator.toString(), is("any"));
        assertThat(term.folding.properties.get(0).source.toString(), is("a"));
        assertThat(term.folding.properties.get(0).target.toString(), is("b"));

        assertThat(term.grouping, is(nullValue()));
    }

    /**
     * Parse a summarized model definition with multi foldings.
     */
    @Test
    public void summarize_multi_folding() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstSummarize> summarized = getSummarized(script, "simple");

        List<AstSummarize> terms = extract(summarized.expression);
        assertThat(terms.size(), is(1));
        AstSummarize term = terms.get(0);

        assertThat(term.reference.name.identifier, is("source"));
        assertThat(term.folding.properties.size(), is(3));
        assertThat(term.folding.properties.get(0).aggregator.toString(), is("any"));
        assertThat(term.folding.properties.get(0).source.toString(), is("a"));
        assertThat(term.folding.properties.get(0).target.toString(), is("a"));
        assertThat(term.folding.properties.get(1).aggregator.toString(), is("count"));
        assertThat(term.folding.properties.get(1).source.toString(), is("b"));
        assertThat(term.folding.properties.get(1).target.toString(), is("b"));
        assertThat(term.folding.properties.get(2).aggregator.toString(), is("com.asakusafw.aggregate"));
        assertThat(term.folding.properties.get(2).source.toString(), is("c"));
        assertThat(term.folding.properties.get(2).target.toString(), is("d"));

        assertThat(term.grouping, is(nullValue()));
    }

    /**
     * Parse a simple summarized model definition.
     */
    @Test
    public void summarize_grouping() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<AstSummarize> summarized = getSummarized(script, "simple");

        List<AstSummarize> terms = extract(summarized.expression);
        assertThat(terms.size(), is(1));
        AstSummarize term = terms.get(0);

        assertThat(term.reference.name.identifier, is("source"));
        assertThat(term.folding.properties.size(), is(1));
        assertThat(term.grouping, not(nullValue()));
        assertThat(term.grouping.properties.size(), is(1));
        assertThat(term.grouping.properties.get(0).identifier, is("category"));
    }

    /**
     * Parse a model with description.
     */
    @Test
    public void model_description() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<?> model = script.models.get(0);
        assertThat(model.description, not(nullValue()));
        assertThat(model.description.token, is("\"With Description\""));
    }

    /**
     * Parse a model with an attribute.
     */
    @Test
    public void model_attribute() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<?> model = script.models.get(0);
        assertThat(model.attributes.size(), is(1));

        AstAttribute attribute = model.attributes.get(0);
        assertThat(attribute.name.toString(), is("attr"));
        assertThat(attribute.elements.isEmpty(), is(true));
    }

    /**
     * Parse a model with many attributes.
     */
    @Test
    public void model_multi_attribute() {
        AstScript script = load();
        assertThat(script.models.size(), is(1));

        AstModelDefinition<?> model = script.models.get(0);
        assertThat(model.attributes.size(), is(3));

        assertThat(model.attributes.get(0).name.toString(), is("a"));
        assertThat(model.attributes.get(1).name.toString(), is("b"));
        assertThat(model.attributes.get(2).name.toString(), is("c"));
    }

    /**
     * Parse a property with description.
     */
    @Test
    public void property_description() {
        AstRecordDefinition def = loadFirstTermAs(AstRecordDefinition.class);
        assertThat(def.properties.size(), is(1));
        AstPropertyDefinition prop = def.properties.get(0);
        assertThat(prop.description, not(nullValue()));
        assertThat(prop.description.token, is("\"With Description\""));
    }

    /**
     * Parse a property with an attribute.
     */
    @Test
    public void property_attribute() {
        AstRecordDefinition def = loadFirstTermAs(AstRecordDefinition.class);
        assertThat(def.properties.size(), is(1));
        AstPropertyDefinition prop = def.properties.get(0);
        assertThat(prop.attributes.size(), is(1));

        AstAttribute attribute = prop.attributes.get(0);
        assertThat(attribute.name.toString(), is("attr"));
        assertThat(attribute.elements.isEmpty(), is(true));
    }

    /**
     * Parse a property with many attributes.
     */
    @Test
    public void property_multi_attribute() {
        AstRecordDefinition def = loadFirstTermAs(AstRecordDefinition.class);
        assertThat(def.properties.size(), is(1));
        AstPropertyDefinition prop = def.properties.get(0);
        assertThat(prop.attributes.size(), is(3));

        assertThat(prop.attributes.get(0).name.toString(), is("a"));
        assertThat(prop.attributes.get(1).name.toString(), is("b"));
        assertThat(prop.attributes.get(2).name.toString(), is("c"));
    }

    /**
     * Parse all basic types.
     */
    @Test
    public void basic_type() {
        AstRecordDefinition def = loadFirstTermAs(AstRecordDefinition.class);
        assertThat(getProp(def, "p_int").type, is(astType(BasicTypeKind.INT)));
        assertThat(getProp(def, "p_long").type, is(astType(BasicTypeKind.LONG)));
        assertThat(getProp(def, "p_byte").type, is(astType(BasicTypeKind.BYTE)));
        assertThat(getProp(def, "p_short").type, is(astType(BasicTypeKind.SHORT)));
        assertThat(getProp(def, "p_decimal").type, is(astType(BasicTypeKind.DECIMAL)));
        assertThat(getProp(def, "p_float").type, is(astType(BasicTypeKind.FLOAT)));
        assertThat(getProp(def, "p_double").type, is(astType(BasicTypeKind.DOUBLE)));
        assertThat(getProp(def, "p_text").type, is(astType(BasicTypeKind.TEXT)));
        assertThat(getProp(def, "p_boolean").type, is(astType(BasicTypeKind.BOOLEAN)));
        assertThat(getProp(def, "p_date").type, is(astType(BasicTypeKind.DATE)));
        assertThat(getProp(def, "p_datetime").type, is(astType(BasicTypeKind.DATETIME)));
    }

    /**
     * Parse all literals.
     */
    @Test
    public void literals() {
        AstAttribute attr = loadFirstAttribute();
        assertThat(getValue(attr, "e_int"), is(value("100", LiteralKind.INTEGER)));
        assertThat(getValue(attr, "e_string"), is(value("\"Hello, world!\"", LiteralKind.STRING)));
        assertThat(getValue(attr, "e_decimal"), is(value("3.141592", LiteralKind.DECIMAL)));
        assertThat(getValue(attr, "e_boolean"), is(value("TRUE", LiteralKind.BOOLEAN)));
    }

    /**
     * Parse a qualified name.
     */
    @Test
    public void qualified_name() {
        AstAttribute attr = loadFirstAttribute();
        assertThat(attr.name.toString(), is("com.asakusafw.dmdl"));
    }

    /**
     * Parse an array.
     */
    @Test
    public void array() {
        AstAttribute attr = loadFirstAttribute();
        AstAttributeValue value = getValue(attr, "e_array");
        assertThat(value, instanceOf(AstAttributeValueArray.class));

        AstAttributeValueArray array = (AstAttributeValueArray) value;
        assertThat(array.elements.size(), is(4));
        assertThat(array.elements.get(0), is(value("100", LiteralKind.INTEGER)));
        assertThat(array.elements.get(1), is(value("\"Hello, world!\"", LiteralKind.STRING)));
        assertThat(array.elements.get(2), is(value("3.141592", LiteralKind.DECIMAL)));
        assertThat(array.elements.get(3), is(value("TRUE", LiteralKind.BOOLEAN)));
    }

    /**
     * Parse a qualified name in an attribute value.
     */
    @Test
    public void value_qname() {
        AstAttribute attr = loadFirstAttribute();
        AstAttributeValue value = getValue(attr, "e_qname");
        assertThat(value, instanceOf(AstQualifiedName.class));

        AstQualifiedName name = (AstQualifiedName) value;
        assertThat(name.toString(), is("com.asakusafw.dmdl"));
    }

    /**
     * Accept special names (e.g. "joined", "summarized" for model / property names).
     */
    @Test
    public void special_names() {
        load();
    }

    /**
     * Name with arrow.
     */
    @Test
    public void name_follows_arrow() {
        load();
    }

    /**
     * with invalid name.
     */
    @Test
    public void invalid_name() {
        shouldSyntaxError();
    }

    /**
     * with invalid name.
     */
    @Test
    public void invalid_name_separator() {
        shouldSyntaxError();
    }

    /**
     * with invalid type name.
     */
    @Test
    public void invalid_type_name() {
        shouldSyntaxError();
    }

    /**
     * with invalid separator character.
     */
    @Test
    public void invalid_separator() {
        shouldSyntaxError();
    }

    /**
     * with invalid name.
     */
    @Test
    public void invalid_missing_eq() {
        shouldSyntaxError();
    }

    /**
     * record definition must be followed semicolon.
     */
    @Test
    public void invalid_record_nodelim() {
        shouldSyntaxError();
    }

    /**
     * property definition in record must be followed semicolon.
     */
    @Test
    public void invalid_record_property_nodelim() {
        shouldSyntaxError();
    }

    /**
     * property definition in record must be followed semicolon.
     */
    @Test
    public void invalid_record_property_type() {
        shouldSyntaxError();
    }

    private AstType astType(BasicTypeKind kind) {
        return new AstBasicType(null, kind);
    }

    private AstAttributeValue value(String token, LiteralKind kind) {
        return new AstLiteral(null, token, kind);
    }

    @SuppressWarnings("unchecked")
    private <T extends AstTerm<T>> List<T> extract(AstExpression<T> expression) {
        List<T> results = Lists.create();
        LinkedList<AstExpression<T>> work = new LinkedList<AstExpression<T>>();
        work.add(expression);
        int count = 0;
        while (work.isEmpty() == false) {
            if (count++ > 1000) {
                throw new AssertionError(work);
            }
            AstExpression<T> first = work.removeFirst();
            if (first instanceof AstUnionExpression<?>) {
                AstUnionExpression<T> union = (AstUnionExpression<T>) first;
                work.addAll(0, union.terms);
            } else if (first instanceof AstTerm<?>) {
                results.add((T) first);
            } else {
                throw new AssertionError("Unknown expression: " + first);
            }
        }
        return results;
    }

    private AstModelDefinition<AstRecord> getRecord(AstScript script, String name) {
        for (AstModelDefinition<?> def : script.models) {
            if (def.name.identifier.equals(name)) {
                return def.asRecord();
            }
        }
        throw new AssertionError(name);
    }

    private AstModelDefinition<AstRecord> getProjection(AstScript script, String name) {
        for (AstModelDefinition<?> def : script.models) {
            if (def.name.identifier.equals(name)) {
                return def.asProjective();
            }
        }
        throw new AssertionError(name);
    }

    private AstModelDefinition<AstJoin> getJoined(AstScript script, String name) {
        for (AstModelDefinition<?> def : script.models) {
            if (def.name.identifier.equals(name)) {
                return def.asJoined();
            }
        }
        throw new AssertionError(name);
    }

    private AstModelDefinition<AstSummarize> getSummarized(AstScript script, String name) {
        for (AstModelDefinition<?> def : script.models) {
            if (def.name.identifier.equals(name)) {
                return def.asSummarized();
            }
        }
        throw new AssertionError(name);
    }

    private AstPropertyDefinition getProp(AstRecordDefinition record, String name) {
        for (AstPropertyDefinition prop : record.properties) {
            if (prop.name.identifier.equals(name)) {
                return prop;
            }
        }
        throw new AssertionError(name);
    }

    private AstAttributeValue getValue(AstAttribute attr, String name) {
        for (AstAttributeElement elem : attr.elements) {
            if (elem.name.identifier.equals(name)) {
                return elem.value;
            }
        }
        throw new AssertionError(name);
    }

    private AstScript load() {
        AstScript script = parse();
        AstScript restored = restore(script);
        assertThat(restored, equalTo(script));
        return script;
    }

    private AstScript restore(AstScript script) {
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        DmdlEmitter.emit(script, writer);
        writer.close();

        System.out.println(script.getRegion().sourceFile);
        System.out.println(output.toString());

        DmdlParser parser = new DmdlParser();
        try {
            return parser.parse(new StringReader(output.toString()), null);
        } catch (DmdlSyntaxException e) {
            throw new AssertionError(e);
        }
    }

    private <T extends AstTerm<?>> T loadFirstTermAs(Class<T> termKind) {
        AstScript script = load();
        assertThat(script.models.size(), greaterThanOrEqualTo(1));
        AstModelDefinition<?> firstModel = script.models.get(0);

        List<? extends AstTerm<?>> terms = extract(firstModel.expression);
        assertThat(terms.size(), greaterThanOrEqualTo(1));
        AstTerm<?> term = terms.get(0);

        return termKind.cast(term);
    }

    private AstAttribute loadFirstAttribute() {
        AstScript script = load();
        assertThat(script.models.size(), greaterThanOrEqualTo(1));
        AstModelDefinition<?> firstModel = script.models.get(0);

        assertThat(firstModel.attributes.size(), greaterThanOrEqualTo(1));
        return firstModel.attributes.get(0);
    }
}
