/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.java.util.JavaName;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.SingleElementAnnotation;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Implements {@code *AsString} methods.
 */
public class StringPropertyDriver extends JavaDataModelDriver {

    private static final BasicType TEXT_TYPE = new BasicType(null, BasicTypeKind.TEXT);

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
        boolean projective = model.getOriginalAst().kind == ModelDefinitionKind.PROJECTIVE;
        List<MethodDeclaration> results = Lists.create();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            if (isTextType(property) == false) {
                continue;
            }
            if (projective) {
                ModelFactory f = context.getModelFactory();
                results.add(makeInterfaceMethod(f, createStringGetter(context, property)));
                results.add(makeInterfaceMethod(f, createStringSetter(context, property)));
            } else {
                results.add(createStringGetter(context, property));
                results.add(createStringSetter(context, property));
            }
        }
        return results;
    }

    private MethodDeclaration createStringGetter(EmitContext context, PropertyDeclaration property) {
        assert context != null;
        assert property != null;
        JavaName name = JavaName.of(property.getName());
        name.addFirst("get");
        name.addLast("as");
        name.addLast("string");
        ModelFactory f = context.getModelFactory();
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("{0}を返す。",
                            context.getDescription(property))
                    .returns()
                        .text("{0}",
                                context.getDescription(property))
                    .exception(context.resolve(NullPointerException.class))
                        .text("{0}の値が<code>null</code>である場合",
                                context.getDescription(property))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                context.resolve(String.class),
                f.newSimpleName(name.toMemberName()),
                Collections.<FormalParameterDeclaration>emptyList(),
                Collections.singletonList(new ExpressionBuilder(f, f.newThis())
                    .field(context.getFieldName(property))
                    .method("getAsString")
                    .toReturnStatement()));
    }

    private MethodDeclaration createStringSetter(EmitContext context, PropertyDeclaration property) {
        assert context != null;
        assert property != null;
        JavaName name = JavaName.of(property.getName());
        name.addFirst("set");
        name.addLast("as");
        name.addLast("string");
        ModelFactory f = context.getModelFactory();
        SimpleName paramName = context.createVariableName(
                context.getFieldName(property).getToken());

        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("{0}を設定する。",
                            context.getDescription(property))
                    .param(paramName)
                        .text("設定する値",
                                context.getDescription(property))
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .annotation(
                            context.resolve(SuppressWarnings.class),
                            Models.toLiteral(f, "deprecation"))
                    .Public()
                    .toAttributes(),
                context.resolve(void.class),
                f.newSimpleName(name.toMemberName()),
                Arrays.asList(new FormalParameterDeclaration[] {
                        f.newFormalParameterDeclaration(
                                context.resolve(String.class),
                                paramName)
                }),
                Collections.singletonList(new ExpressionBuilder(f, f.newThis())
                    .field(context.getFieldName(property))
                    .method("modify", paramName)
                    .toStatement()));
    }

    private boolean isTextType(PropertyDeclaration property) {
        assert property != null;
        return property.getType().isSame(TEXT_TYPE);
    }

    private MethodDeclaration makeInterfaceMethod(ModelFactory f, MethodDeclaration method) {
        assert f != null;
        assert method != null;
        return f.newMethodDeclaration(
                method.getJavadoc(),
                filterInterfaceMethodModifiers(method.getModifiers()),
                method.getTypeParameters(),
                method.getReturnType(),
                method.getName(),
                method.getFormalParameters(),
                0,
                method.getExceptionTypes(),
                null);
    }

    private List<Attribute> filterInterfaceMethodModifiers(List<? extends Attribute> modifiers) {
        assert modifiers != null;
        List<Attribute> results = Lists.create();
        for (Attribute attribute : modifiers) {
            if (attribute.getModelKind() == ModelKind.MODIFIER) {
                ModifierKind kind = ((Modifier) attribute).getModifierKind();
                if (kind == ModifierKind.PUBLIC || kind == ModifierKind.ABSTRACT) {
                    continue;
                }
            } else if (attribute.getModelKind() == ModelKind.SINGLE_ELEMENT_ANNOTATION) {
                SingleElementAnnotation an = (SingleElementAnnotation) attribute;
                Name name = an.getType().getName();
                if (name.toNameString().equals(SuppressWarnings.class.getSimpleName())) {
                    continue;
                }
            }
            results.add(attribute);
        }
        return results;
    }
}
