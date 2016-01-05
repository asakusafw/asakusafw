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
package com.asakusafw.dmdl.thundergate.driver;

import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;

/**
 * Emits {@link PrimaryKey} annotations.
 */
public class PrimaryKeyEmitter extends JavaDataModelDriver {

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) {
        PrimaryKeyTrait trait = model.getTrait(PrimaryKeyTrait.class);
        if (trait == null) {
            return Collections.emptyList();
        }
        ModelFactory f = context.getModelFactory();
        List<Expression> properties = Lists.create();
        for (PropertySymbol property : trait.getProperties()) {
            String name = context.getFieldName(property.findDeclaration()).getToken();
            properties.add(Models.toLiteral(f, name));
        }
        return new AttributeBuilder(f)
            .annotation(context.resolve(PrimaryKey.class),
                    "value", f.newArrayInitializer(properties))
            .toAnnotations();
    }
}
