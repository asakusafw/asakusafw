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
package com.asakusafw.compiler.operator.processor;

import java.util.List;

import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.ImplementationBuilder;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.model.JoinedModel;
import com.asakusafw.vocabulary.operator.Split;
import com.ashigeru.lang.java.model.syntax.FieldAccessExpression;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;

/**
 * {@link Split 分割演算子}を処理する。
 */
@TargetOperator(Split.class)
public class SplitOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(getEnvironment(), context.element);
        if (a.isAbstract() == false) {
            a.error("分割演算子はabstractを指定する必要があります");
        }
        if (a.getReturnType().isVoid() == false) {
            a.error("分割演算子は戻り値にvoidを指定する必要があります");
        }
        if (a.getParameterType(0).isModel() == false) {
            a.error(0, "分割演算子の最初の引数はモデルオブジェクト型である必要があります");
        }
        for (int i = 1; i <= 2; i++) {
            if (a.getParameterType(i).isResult() == false) {
                a.error(i, "分割演算子の{0}つ目の引数は結果型である必要があります", i + 1);
            } else if (a.getParameterType(i).getTypeArgument().isModel() == false) {
                a.error(i, "分割演算子の{0}つ目の引数は結果のモデル型である必要があります", i + 1);
            }
        }
        for (int i = 3, n = a.countParameters(); i < n; i++) {
            a.error(i, "分割演算子にはユーザー引数を利用できません");
        }
        if (a.hasError()) {
            return null;
        }

        TypeConstraint joined = a.getParameterType(0);
        TypeConstraint from = a.getParameterType(1).getTypeArgument();
        TypeConstraint join = a.getParameterType(2).getTypeArgument();
        if (joined.isJoinedModel(from.getType(), join.getType()) == false) {
            a.error(0, "分割演算子の最初の引数は結果の2つの型を結合した型である必要があります");
            return null;
        }

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(a.getObservationCount());
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getType(),
                0);
        builder.addOutput(
                a.getParameterDocument(1),
                a.getParameterName(1),
                a.getParameterType(1).getTypeArgument().getType(),
                1);
        builder.addOutput(
                a.getParameterDocument(2),
                a.getParameterName(2),
                a.getParameterType(2).getTypeArgument().getType(),
                2);

        return builder.toDescriptor();
    }

    @Override
    protected List<? extends TypeBodyDeclaration> override(Context context) {
        ModelFactory f = context.factory;

        ExecutableAnalyzer a = new ExecutableAnalyzer(getEnvironment(), context.element);
        TypeMirror fromType = a.getParameterType(1).getTypeArgument().getType();
        TypeMirror joinType = a.getParameterType(2).getTypeArgument().getType();
        SimpleName fromName = f.newSimpleName(a.getParameterName(1));
        SimpleName joinName = f.newSimpleName(a.getParameterName(2));

        if (a.getParameterType(0).isJoinFrom(fromType) == false) {
            TypeMirror holdType = fromType;
            SimpleName holdName = fromName;
            fromType = joinType;
            fromName = joinName;
            joinType = holdType;
            joinName = holdName;
        }

        ImplementationBuilder builder = new ImplementationBuilder(context);
        FieldAccessExpression fromCache = builder.addModelObjectField(
                fromType, "_cache_from");
        FieldAccessExpression joinCache = builder.addModelObjectField(
                joinType, "_cache_join");

        SimpleName joined = f.newSimpleName(a.getParameterName(0));
        builder.addStatement(new ExpressionBuilder(f, joined)
            .method(JoinedModel.Interface.METHOD_NAME_SPLIT_INTO,
                    fromCache,
                    joinCache)
            .toStatement());
        builder.addStatement(new ExpressionBuilder(f, fromName)
            .method("add", fromCache)
            .toStatement());
        builder.addStatement(new ExpressionBuilder(f, joinName)
            .method("add", joinCache)
            .toStatement());

        return builder.toImplementation();
    }
}
