/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.emitter.driver;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.trait.MappingFactor;
import com.asakusafw.dmdl.semantics.trait.ReduceTerm;
import com.asakusafw.dmdl.semantics.trait.SummarizeTrait;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.Summarized;

/**
 * Implements summarized-model feature.
 */
public class SummarizeDriver extends JavaDataModelDriver {

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) {
        if (model.getOriginalAst().kind != ModelDefinitionKind.SUMMARIZED) {
            return Collections.emptyList();
        }
        SummarizeTrait trait = model.getTrait(SummarizeTrait.class);
        if (trait == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Internal Error: summarized model {0} has no SummarizeTrait",
                    model.getName()));
        }

        ModelFactory f = context.getModelFactory();
        List<Annotation> eTerms = Lists.create();
        for (ReduceTerm<?> term : trait.getTerms()) {
            ClassLiteral source = f.newClassLiteral(context.resolve(term.getSource()));
            ArrayInitializer mappings = toMappings(context, term.getMappings());
            Annotation shuffle = toKey(context, term);
            eTerms.addAll(new AttributeBuilder(f)
                .annotation(context.resolve(Summarized.Term.class),
                        "source", source,
                        "foldings", mappings,
                        "shuffle", shuffle)
                .toAnnotations());
        }
        return new AttributeBuilder(f)
            .annotation(context.resolve(Summarized.class),
                    "term", eTerms.get(0))
            .toAnnotations();
    }

    private ArrayInitializer toMappings(EmitContext context, List<MappingFactor> foldings) {
        assert context != null;
        assert foldings != null;
        ModelFactory f = context.getModelFactory();
        List<Annotation> eachFolding = Lists.create();
        for (MappingFactor factor : foldings) {
            Expression aggregator = new TypeBuilder(f, context.resolve(Summarized.Aggregator.class))
                .field(convert(factor.getKind()).name())
                .toExpression();
            String source = context.getFieldName(factor.getSource().findDeclaration()).getToken();
            String target = context.getFieldName(factor.getTarget().findDeclaration()).getToken();
            eachFolding.addAll(new AttributeBuilder(f)
                .annotation(context.resolve(Summarized.Folding.class),
                        "aggregator", aggregator,
                        "source", Models.toLiteral(f, source),
                        "destination", Models.toLiteral(f, target))
                .toAnnotations());
        }
        return f.newArrayInitializer(eachFolding);
    }

    private Summarized.Aggregator convert(PropertyMappingKind kind) {
        assert kind != null;
        switch (kind) {
        case ANY:
            return Summarized.Aggregator.ANY;
        case COUNT:
            return Summarized.Aggregator.COUNT;
        case MAX:
            return Summarized.Aggregator.MAX;
        case MIN:
            return Summarized.Aggregator.MIN;
        case SUM:
            return Summarized.Aggregator.SUM;
        default:
            throw new AssertionError(kind);
        }
    }

    private Annotation toKey(EmitContext context, ReduceTerm<?> term) {
        assert context != null;
        assert term != null;
        ModelFactory f = context.getModelFactory();
        List<Literal> properties = Lists.create();
        Map<String, PropertySymbol> reverseMapping = Maps.create();
        for (MappingFactor mapping : term.getMappings()) {
            reverseMapping.put(mapping.getTarget().getName().identifier, mapping.getSource());
        }
        for (PropertySymbol property : term.getGrouping()) {
            PropertySymbol origin = reverseMapping.get(property.getName().identifier);
            assert origin != null;
            PropertyDeclaration decl = origin.findDeclaration();
            properties.add(Models.toLiteral(f, context.getFieldName(decl).getToken()));
        }
        return new AttributeBuilder(f)
            .annotation(context.resolve(Key.class),
                    "group", f.newArrayInitializer(properties))
            .toAnnotations()
            .get(0);
    }
}
