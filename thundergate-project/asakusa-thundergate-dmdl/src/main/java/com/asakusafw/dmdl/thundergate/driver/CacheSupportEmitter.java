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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.Text;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits {@link ThunderGateCacheSupport} implementation.
 * @since 0.2.3
 */
public class CacheSupportEmitter extends JavaDataModelDriver {

    private static final String FIELD_DELETE_FLAG_VALUE = "__TGC_DELETE_FLAG_VALUE";

    @Override
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return Collections.emptyList();
        }
        return Arrays.asList(context.resolve(ThunderGateCacheSupport.class));
    }

    @Override
    public List<FieldDeclaration> getFields(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return Collections.emptyList();
        }
        CacheSupportTrait trait = model.getTrait(CacheSupportTrait.class);
        assert trait != null;

        List<FieldDeclaration> results = Lists.create();
        if (trait.getDeleteFlagValue() != null) {
            results.add(createDeleteFlagValueField(context, model, trait.getDeleteFlagValue()));
        }
        return results;
    }

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return Collections.emptyList();
        }
        CacheSupportTrait trait = model.getTrait(CacheSupportTrait.class);
        assert trait != null;

        List<MethodDeclaration> results = Lists.create();
        results.add(createModelVersionMethod(context, model, trait));
        results.add(createTimestampColumnMethod(context, model, trait.getTimestamp()));
        results.add(createSystemIdMethod(context, model, trait.getSid()));
        results.add(createDeletedMethod(context, model, trait.getDeleteFlag()));
        return results;
    }

    private FieldDeclaration createDeleteFlagValueField(
            EmitContext context,
            ModelDeclaration model,
            AstLiteral deleteFlagValue) {
        assert context != null;
        assert model != null;
        assert deleteFlagValue != null;
        ModelFactory f = context.getModelFactory();
        Type type;
        Expression value;
        switch (deleteFlagValue.kind) {
        case BOOLEAN:
            type = context.resolve(boolean.class);
            value = Models.toLiteral(f, deleteFlagValue.toBooleanValue());
            break;
        case INTEGER:
            type = context.resolve(int.class);
            value = Models.toLiteral(f, deleteFlagValue.toIntegerValue().intValue());
            break;
        case STRING:
            type = context.resolve(Text.class);
            value = new TypeBuilder(f, context.resolve(Text.class))
                .newObject(Models.toLiteral(f, deleteFlagValue.toStringValue()))
                .toExpression();
            break;
        default:
            throw new AssertionError(deleteFlagValue);
        }
        return f.newFieldDeclaration(
                null,
                new AttributeBuilder(f)
                    .Private()
                    .Static()
                    .Final()
                    .toAttributes(),
                type,
                f.newSimpleName(FIELD_DELETE_FLAG_VALUE),
                value);
    }

    private MethodDeclaration createModelVersionMethod(
            EmitContext context,
            ModelDeclaration model,
            CacheSupportTrait trait) {
        assert context != null;
        assert model != null;
        assert trait != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = Lists.create();
        statements.add(new ExpressionBuilder(f, Models.toLiteral(f, computeModelVersion(context, model, trait)))
            .toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                context.resolve(long.class),
                f.newSimpleName("__tgc__DataModelVersion"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private long computeModelVersion(EmitContext context, ModelDeclaration model, CacheSupportTrait trait) {
        assert context != null;
        assert model != null;
        assert trait != null;
        long hash = 1;
        final long prime = 31;
        hash = hash * prime + context.getQualifiedTypeName().toNameString().hashCode();
        hash = hash * prime + trait.getSid().getName().identifier.hashCode();
        hash = hash * prime + trait.getTimestamp().getName().identifier.hashCode();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            hash = hash * prime + property.getName().identifier.hashCode();
            com.asakusafw.dmdl.semantics.Type type = property.getType();
            assert type instanceof BasicType;
            hash = hash * prime + ((BasicType) type).getKind().name().hashCode();
        }
        return hash;
    }

    private MethodDeclaration createTimestampColumnMethod(
            EmitContext context,
            ModelDeclaration model,
            PropertySymbol timestamp) {
        assert context != null;
        assert model != null;
        assert timestamp != null;
        ModelFactory f = context.getModelFactory();
        String name = OriginalNameEmitter.getOriginalName(timestamp.findDeclaration());
        List<Statement> statements = Lists.create();
        statements.add(new ExpressionBuilder(f, Models.toLiteral(f, name))
            .toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                context.resolve(String.class),
                f.newSimpleName("__tgc__TimestampColumn"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private MethodDeclaration createSystemIdMethod(
            EmitContext context,
            ModelDeclaration model,
            PropertySymbol sid) {
        assert context != null;
        assert model != null;
        assert sid != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = Lists.create();
        statements.add(new ExpressionBuilder(f, f.newThis())
            .method(context.getValueGetterName(sid.findDeclaration()))
            .toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                context.resolve(long.class),
                f.newSimpleName("__tgc__SystemId"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private MethodDeclaration createDeletedMethod(
            EmitContext context,
            ModelDeclaration model,
            PropertySymbol deleteFlagOrNull) {
        assert context != null;
        assert model != null;
        ModelFactory f = context.getModelFactory();
        List<Statement> statements = Lists.create();
        if (deleteFlagOrNull == null) {
            statements.add(new ExpressionBuilder(f, Models.toLiteral(f, false))
                .toReturnStatement());
        } else {
            statements.add(new ExpressionBuilder(f, f.newThis())
                .method(context.getOptionGetterName(deleteFlagOrNull.findDeclaration()))
                .method("has", f.newSimpleName(FIELD_DELETE_FLAG_VALUE))
                .toReturnStatement());
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(context.resolve(Override.class))
                    .Public()
                    .toAttributes(),
                context.resolve(boolean.class),
                f.newSimpleName("__tgc__Deleted"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        return model.getTrait(CacheSupportTrait.class) != null;
    }
}
