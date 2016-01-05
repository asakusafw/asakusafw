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
package com.asakusafw.dmdl.thundergate.emitter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstExpression;
import com.asakusafw.dmdl.model.AstGrouping;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstModelMapping;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstPropertyMapping;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstUnionExpression;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.model.JoinedModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.ModelReference;
import com.asakusafw.dmdl.thundergate.model.Source;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;

/**
 * Creates joined models.
 */
public final class JoinedModelGenerator {

    private final JoinedModelDescription model;

    private JoinedModelGenerator(JoinedModelDescription model) {
        assert model != null;
        this.model = model;
    }

    /**
     * Returns the corresponded DMDL model definition.
     * @param model the thundergate model
     * @return the converted model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstModelDefinition<AstJoin> generate(JoinedModelDescription model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        return new AstModelDefinition<AstJoin>(
                null,
                ModelDefinitionKind.JOINED,
                AstBuilder.getDesciption(
                        "{0}と{1}を結合したモデル",
                        model.getFromModel().getSimpleName(),
                        model.getJoinModel().getSimpleName()),
                Arrays.asList(new AstAttribute[] {
                        AstBuilder.getAutoProjection(),
                        AstBuilder.getNamespace(AstBuilder.toDmdlName(Constants.SOURCE_VIEW)),
                        AstBuilder.getOriginalName(model.getReference().getSimpleName()),
                }),
                AstBuilder.toName(model.getReference()),
                new JoinedModelGenerator(model).generateExpression());
    }

    private AstExpression<AstJoin> generateExpression() {
        AstJoin from = generateTerm(model.getFromModel(), model.getFromCondition(), true);
        AstJoin join = generateTerm(model.getJoinModel(), model.getJoinCondition(), false);
        return new AstUnionExpression<AstJoin>(null, Arrays.asList(from, join));
    }

    private AstJoin generateTerm(ModelReference sourceModel, List<Source> group, boolean from) {
        Map<String, ModelProperty> resolver = Maps.create();
        List<AstPropertyMapping> mappings = Lists.create();
        for (ModelProperty property : model.getProperties()) {
            Source source = from ? property.getFrom() : property.getJoined();
            if (source == null) {
                continue;
            }
            resolver.put(source.getName(), property);
            mappings.add(new AstPropertyMapping(
                    null,
                    AstBuilder.getDesciption("{0}.{1}",
                            sourceModel.getSimpleName(),
                            source.getName()),
                    Arrays.asList(new AstAttribute[] {
                            AstBuilder.getOriginalName(property.getName()),
                    }),
                    AstBuilder.toName(source),
                    AstBuilder.toName(property)));
        }

        List<AstSimpleName> grouping = Lists.create();
        for (Source source : group) {
            ModelProperty property = resolver.get(source.getName());
            assert property != null : source;
            grouping.add(AstBuilder.toName(property));
        }

        return new AstJoin(
                null,
                new AstModelReference(null, AstBuilder.toName(sourceModel)),
                new AstModelMapping(null, mappings),
                grouping.isEmpty() ? null : new AstGrouping(null, grouping));
    }
}
