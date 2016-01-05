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
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstExpression;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstPropertyDefinition;
import com.asakusafw.dmdl.model.AstRecord;
import com.asakusafw.dmdl.model.AstRecordDefinition;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.utils.collections.Lists;

/**
 * Creates record models.
 * @since 0.2.0
 */
public final class RecordModelGenerator {

    private final TableModelDescription model;

    private RecordModelGenerator(TableModelDescription model) {
        assert model != null;
        this.model = model;
    }

    /**
     * Returns the corresponded DMDL model definition.
     * @param model the thundergate model
     * @return the converted model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AstModelDefinition<AstRecord> generate(TableModelDescription model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        return generate(model, new AstAttribute[0]);
    }

    /**
     * Returns the corresponded DMDL model definition.
     * @param model the thundergate model
     * @param extra extra attributes
     * @return the converted model
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.3
     */
    public static AstModelDefinition<AstRecord> generate(TableModelDescription model, AstAttribute... extra) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        if (extra == null) {
            throw new IllegalArgumentException("extra must not be null"); //$NON-NLS-1$
        }
        List<AstAttribute> attrs = Lists.create();
        attrs.add(AstBuilder.getAutoProjection());
        attrs.add(AstBuilder.getNamespace(AstBuilder.toDmdlName(Constants.SOURCE_TABLE)));
        attrs.add(AstBuilder.getOriginalName(model.getReference().getSimpleName()));
        attrs.add(AstBuilder.getPrimaryKey(model));
        Collections.addAll(attrs, extra);

        return new AstModelDefinition<AstRecord>(
                null,
                ModelDefinitionKind.RECORD,
                AstBuilder.getDesciption("テーブル{0}", model.getReference().getSimpleName()),
                attrs,
                AstBuilder.toName(model.getReference()),
                new RecordModelGenerator(model).generateExpression());
    }

    private AstExpression<AstRecord> generateExpression() {
        return generateTerm();
    }

    private AstRecord generateTerm() {
        List<AstPropertyDefinition> properties = Lists.create();
        for (ModelProperty property : model.getProperties()) {
            properties.add(new AstPropertyDefinition(
                    null,
                    AstBuilder.getDesciption("{0}", property.getName()),
                    Arrays.asList(new AstAttribute[] {
                            AstBuilder.getOriginalName(property.getName()),
                    }),
                    AstBuilder.toName(property),
                    AstBuilder.toType(property.getType())));
        }
        return new AstRecordDefinition(
                null,
                properties);
    }
}
