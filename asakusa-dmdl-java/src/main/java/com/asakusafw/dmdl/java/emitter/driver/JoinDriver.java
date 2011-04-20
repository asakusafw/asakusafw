/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.trait.JoinTrait;
import com.asakusafw.dmdl.semantics.trait.MappingFactor;
import com.asakusafw.dmdl.semantics.trait.ReduceTerm;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;
import com.ashigeru.lang.java.model.syntax.Annotation;
import com.ashigeru.lang.java.model.syntax.ArrayInitializer;
import com.ashigeru.lang.java.model.syntax.ClassLiteral;
import com.ashigeru.lang.java.model.syntax.Literal;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.Models;

/**
 * Implements joined-model feature.
 */
public class JoinDriver implements JavaDataModelDriver {

    @Override
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) {
        return Collections.emptyList();
    }

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
        return Collections.emptyList();
    }

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) {
        if (model.getOriginalAst().kind != ModelDefinitionKind.JOINED) {
            return Collections.emptyList();
        }
        JoinTrait trait = model.getTrait(JoinTrait.class);
        if (trait == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Internal Error: joined model {0} has no JoinTrait",
                    model.getName()));
        }

        ModelFactory f = context.getModelFactory();
        List<Annotation> eTerms = new ArrayList<Annotation>();
        for (ReduceTerm<?> term : trait.getTerms()) {
            ClassLiteral source = f.newClassLiteral(context.resolve(term.getSource()));
            ArrayInitializer mappings = toMappings(context, term.getMappings());
            Annotation shuffle = toKey(context, term);
            eTerms.addAll(new AttributeBuilder(f)
                .annotation(context.resolve(Joined.Term.class),
                        "source", source,
                        "mappings", mappings,
                        "shuffle", shuffle)
                .toAnnotations());
        }
        return new AttributeBuilder(f)
            .annotation(context.resolve(Joined.class),
                    "terms", f.newArrayInitializer(eTerms))
            .toAnnotations();
    }

    private ArrayInitializer toMappings(EmitContext context, List<MappingFactor> mappings) {
        assert context != null;
        assert mappings != null;
        ModelFactory f = context.getModelFactory();
        List<Annotation> eachMapping = new ArrayList<Annotation>();
        for (MappingFactor factor : mappings) {
            String source = context.getFieldName(factor.getSource().findDeclaration()).getToken();
            String target = context.getFieldName(factor.getTarget().findDeclaration()).getToken();
            eachMapping.addAll(new AttributeBuilder(f)
                .annotation(context.resolve(Joined.Mapping.class),
                        "source", Models.toLiteral(f, source),
                        "destination", Models.toLiteral(f, target))
                .toAnnotations());
        }
        return f.newArrayInitializer(eachMapping);
    }

    private Annotation toKey(EmitContext context, ReduceTerm<?> term) {
        assert context != null;
        assert term != null;
        ModelFactory f = context.getModelFactory();
        List<Literal> properties = new ArrayList<Literal>();
        Map<String, PropertySymbol> reverseMapping = new HashMap<String, PropertySymbol>();
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

    @Override
    public List<Annotation> getMemberAnnotations(EmitContext context, PropertyDeclaration property) {
        return Collections.emptyList();
    }
}
