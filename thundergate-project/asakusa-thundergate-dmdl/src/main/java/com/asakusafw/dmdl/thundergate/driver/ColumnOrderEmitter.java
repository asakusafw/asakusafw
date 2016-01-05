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

import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.bulkloader.ColumnOrder;

/**
 * Emits {@link ColumnOrder} annotations.
 */
public class ColumnOrderEmitter extends JavaDataModelDriver {

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) {
        ModelFactory f = context.getModelFactory();
        List<Expression> columns = Lists.create();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            columns.add(Models.toLiteral(f, OriginalNameEmitter.getOriginalName(property)));
        }
        return new AttributeBuilder(f)
            .annotation(context.resolve(ColumnOrder.class),
                    "value", f.newArrayInitializer(columns))
            .toAnnotations();
    }
}
