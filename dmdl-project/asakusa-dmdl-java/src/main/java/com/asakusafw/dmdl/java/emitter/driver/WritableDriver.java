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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.Writable;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.Models;

/**
 * Implements {@link Writable} interface.
 */
public class WritableDriver extends JavaDataModelDriver {

    @Override
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) {
        return Collections.singletonList(context.resolve(Writable.class));
    }

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
        if (model.getOriginalAst().kind == ModelDefinitionKind.PROJECTIVE) {
            return Collections.emptyList();
        }
        List<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
        results.add(createWrite(context, model));
        results.add(createReadFields(context, model));
        return results;
    }

    private MethodDeclaration createWrite(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        SimpleName parameter = context.createVariableName("out");
        List<Statement> statements = new ArrayList<Statement>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            SimpleName fieldName = context.getFieldName(property);
            statements.add(new ExpressionBuilder(f, fieldName)
                .method("write", parameter)
                .toStatement());
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                f.newSimpleName("write"),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        context.resolve(DataOutput.class),
                        parameter)),
                0,
                Collections.singletonList(context.resolve(IOException.class)),
                f.newBlock(statements));
    }

    private MethodDeclaration createReadFields(EmitContext context, ModelDeclaration model) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        SimpleName parameter = context.createVariableName("in");
        List<Statement> statements = new ArrayList<Statement>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            SimpleName fieldName = context.getFieldName(property);
            statements.add(new ExpressionBuilder(f, fieldName)
                .method("readFields", parameter)
                .toStatement());
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                f.newSimpleName("readFields"),
                Arrays.asList(f.newFormalParameterDeclaration(
                        context.resolve(DataInput.class),
                        parameter)),
                0,
                Collections.singletonList(context.resolve(IOException.class)),
                f.newBlock(statements));
    }
}
