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
import com.asakusafw.dmdl.model.AstGrouping;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstModelFolding;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstPropertyFolding;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.ModelReference;
import com.asakusafw.dmdl.thundergate.model.Source;
import com.asakusafw.dmdl.thundergate.model.SummarizedModelDescription;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;

/**
 * Creates summarized models.
 */
public final class SummarizedModelGenerator {

    private final SummarizedModelDescription model;

    private SummarizedModelGenerator(SummarizedModelDescription model) {
        assert model != null;
        this.model = model;
    }

    /**
     * Returns the corresponded DMDL model definition.
     * @param model the thundergate model
     * @return the converted model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstModelDefinition<AstSummarize> generate(SummarizedModelDescription model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        return new AstModelDefinition<AstSummarize>(
                null,
                ModelDefinitionKind.SUMMARIZED,
                AstBuilder.getDesciption(
                        "{0}を集約したモデル",
                        model.getOriginalModel().getSimpleName()),
                Arrays.asList(new AstAttribute[] {
                        AstBuilder.getAutoProjection(),
                        AstBuilder.getNamespace(AstBuilder.toDmdlName(Constants.SOURCE_VIEW)),
                        AstBuilder.getOriginalName(model.getReference().getSimpleName()),
                }),
                AstBuilder.toName(model.getReference()),
                new SummarizedModelGenerator(model).generateExpression());
    }

    private AstSummarize generateExpression() {
        return generateTerm(model.getOriginalModel(), model.getGroupBy());
    }

    private AstSummarize generateTerm(ModelReference sourceModel, List<Source> group) {
        Map<String, ModelProperty> resolver = Maps.create();
       List<AstPropertyFolding> foldings = Lists.create();
        for (ModelProperty property : model.getProperties()) {
            Source source = property.getSource();
            assert source.getDeclaring().equals(sourceModel);
            resolver.put(source.getName(), property);
            foldings.add(new AstPropertyFolding(
                    null,
                    AstBuilder.getDesciption("{0}({1})",
                            source.getAggregator().name(),
                            source.getName()),
                    Arrays.asList(new AstAttribute[] {
                            AstBuilder.getOriginalName(property.getName()),
                    }),
                    AstBuilder.toName(source.getAggregator()),
                    AstBuilder.toName(source),
                    AstBuilder.toName(property)));
        }

        List<AstSimpleName> grouping = Lists.create();
        for (Source source : group) {
            ModelProperty property = resolver.get(source.getName());
            assert property != null : source;
            grouping.add(AstBuilder.toName(property));
        }

        return new AstSummarize(
                null,
                new AstModelReference(null, AstBuilder.toName(sourceModel)),
                new AstModelFolding(null, foldings),
                grouping.isEmpty() ? null : new AstGrouping(null, grouping));
    }
}
