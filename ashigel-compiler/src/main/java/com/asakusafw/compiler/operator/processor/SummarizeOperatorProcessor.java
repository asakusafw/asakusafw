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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.common.TargetOperator;
import com.asakusafw.compiler.operator.AbstractOperatorProcessor;
import com.asakusafw.compiler.operator.ExecutableAnalyzer;
import com.asakusafw.compiler.operator.ExecutableAnalyzer.TypeConstraint;
import com.asakusafw.compiler.operator.ImplementationBuilder;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor;
import com.asakusafw.compiler.operator.OperatorMethodDescriptor.Builder;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.Property.Aggregator;
import com.asakusafw.vocabulary.operator.Summarize;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * {@link Summarize 単純集計演算子}を処理する。
 */
@TargetOperator(Summarize.class)
public class SummarizeOperatorProcessor extends AbstractOperatorProcessor {

    @Override
    public OperatorMethodDescriptor describe(Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$

        ExecutableAnalyzer a = new ExecutableAnalyzer(getEnvironment(), context.element);
        if (a.isAbstract() == false) {
            a.error("単純集計演算子はabstractで宣言する必要があります");
        }
        TypeConstraint summarized = a.getReturnType();
        if (summarized.isModel() == false) {
            a.error("単純集計演算子は戻り値にモデルオブジェクト型を指定する必要があります");
        }
        TypeConstraint summarizee = a.getParameterType(0);
        if (summarizee.isModel() == false) {
            a.error(0, "単純集計演算子の最初の引数はモデルオブジェクト型である必要があります");
        }
        for (int i = 1, n = a.countParameters(); i < n; i++) {
            a.error(i, "単純集計演算子にはユーザー引数を利用できません");
        }
        if (a.hasError()) {
            return null;
        }
        if (summarized.isSummarizedModel(summarizee.getType()) == false) {
            a.error("単純集計演算子の戻り値型は最初の引数の集計結果を表す型である必要があります");
            return null;
        }

        ShuffleKey key = summarized.getSummarizeKey();
        key = remap(key, a, summarizee, summarized);
        Summarize annotation = context.element.getAnnotation(Summarize.class);
        if (annotation == null) {
            a.error("注釈の解釈に失敗しました");
            return null;
        }
        OperatorProcessorUtil.checkPortName(a, new String[] {
                annotation.summarizedPort(),
        });
        if (a.hasError()) {
            return null;
        }

        Builder builder = new Builder(getTargetAnnotationType(), context);
        builder.addAttribute(FlowBoundary.SHUFFLE);
        builder.addAttribute(a.getObservationCount());
        builder.setDocumentation(a.getExecutableDocument());
        builder.addInput(
                a.getParameterDocument(0),
                a.getParameterName(0),
                a.getParameterType(0).getType(),
                0,
                key);
        builder.addOutput(
                a.getReturnDocument(),
                annotation.summarizedPort(),
                a.getReturnType().getType(),
                null);
        return builder.toDescriptor();
    }

    private ShuffleKey remap(
            ShuffleKey key,
            ExecutableAnalyzer analyzer,
            TypeConstraint summarizee,
            TypeConstraint summarized) {
        assert analyzer != null;
        assert summarizee != null;
        assert summarized != null;
        if (key == null) {
            analyzer.error("{0}の@Keyを解析できませんでした", summarized.getType());
            return null;
        }
        TypeElement source = summarizee.getTypeElement();
        TypeElement target = summarized.getTypeElement();
        assert source != null;
        assert target != null;
        Map<String, String> mapping = createMapping(analyzer, target);
        if (analyzer.hasError()) {
            return null;
        }

        List<String> remappedGroup = new ArrayList<String>();
        for (String group : key.getGroupProperties()) {
            String normalized = JavaName.of(group).toMemberName();
            String remapped = mapping.get(normalized);
            if (remapped == null) {
                analyzer.error(
                        "{0}のgroup指定されたプロパティ{1}を集計前のモデルから発見できませんでした",
                        summarized.getType(),
                        normalized);
            } else {
                remappedGroup.add(remapped);
            }
        }
        if (analyzer.hasError()) {
            return null;
        }
        return new ShuffleKey(remappedGroup, Collections.<ShuffleKey.Order>emptyList());
    }

    private Map<String, String> createMapping(
            ExecutableAnalyzer analyzer,
            TypeElement target) {
        assert target != null;
        Map<String, String> mapping = new HashMap<String, String>();
        for (Element field : target.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) {
                continue;
            }
            String sourceName = getSourceName(analyzer, target, field);
            if (sourceName != null && mapping.containsKey(sourceName) == false) {
                mapping.put(sourceName, field.getSimpleName().toString());
            }
        }
        return mapping;
    }

    private String getSourceName(ExecutableAnalyzer analyzer, TypeElement target, Element field) {
        assert analyzer != null;
        assert target != null;
        assert field != null;

        // @Propertyが未指定のものは無視
        Property property = field.getAnnotation(Property.class);
        if (property == null) {
            return null;
        }

        // グループ化に利用できないものは無視
        if (property.aggregator() != Aggregator.IDENT) {
            return null;
        }
        String sourceName = property.from().name();
        if (sourceName.isEmpty()) {
            return null;
        }
        return sourceName;
    }

    @Override
    protected List<? extends TypeBodyDeclaration> override(Context context) {
        ImplementationBuilder builder = new ImplementationBuilder(context);
        ModelFactory f = context.factory;
        builder.addStatement(new TypeBuilder(f, context.importer.toType(UnsupportedOperationException.class))
            .newObject(Models.toLiteral(f, "単純集計演算子は組み込みの方法で処理されます"))
            .toThrowStatement());
        return builder.toImplementation();
    }
}
