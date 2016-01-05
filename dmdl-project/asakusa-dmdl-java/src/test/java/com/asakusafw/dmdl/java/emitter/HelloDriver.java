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
package com.asakusafw.dmdl.java.emitter;

import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Creates a new method {@code def hello() = "hello"}.
 */
public class HelloDriver extends JavaDataModelDriver {

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
        ModelFactory f = context.getModelFactory();
        return Collections.singletonList(f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                context.resolve(String.class),
                f.newSimpleName("hello"),
                Collections.<FormalParameterDeclaration>emptyList(),
                Collections.singletonList(new ExpressionBuilder(f, Models.toLiteral(f, "hello"))
                    .toReturnStatement())));
    }
}
