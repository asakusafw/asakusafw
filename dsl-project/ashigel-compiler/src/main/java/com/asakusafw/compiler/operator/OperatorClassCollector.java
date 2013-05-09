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
package com.asakusafw.compiler.operator;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.operator.OperatorHelper;


/**
 * 演算子クラスを集計する。
 */
public class OperatorClassCollector {

    private final OperatorCompilingEnvironment environment;

    private final RoundEnvironment round;

    private final List<TargetMethod> targetMethods;

    private boolean sawError;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @param round ラウンドオブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public OperatorClassCollector(
            OperatorCompilingEnvironment environment,
            RoundEnvironment round) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(round, "round"); //$NON-NLS-1$
        this.environment = environment;
        this.round = round;
        this.targetMethods = Lists.create();
    }

    /**
     * コンパイラが利用する演算子プロセッサを追加する。
     * @param processor 追加する演算子プロセッサ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void add(OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        Class<? extends Annotation> target = processor.getTargetAnnotationType();
        assert target != null;

        Set<? extends Element> elements = round.getElementsAnnotatedWith(target);
        for (Element element : elements) {
            ExecutableElement method = toOperatorMethodElement(element);
            if (method == null) {
                continue;
            }
            registerMethod(processor, method);
        }
    }

    private void registerMethod(
            OperatorProcessor processor,
            ExecutableElement method) {
        assert processor != null;
        assert method != null;
        targetMethods.add(new TargetMethod(method, processor));
    }

    private ExecutableElement toOperatorMethodElement(Element element) {
        assert element != null;
        if (element.getKind() != ElementKind.METHOD) {
            raiseInvalid(element, "演算子{0}はメソッドとして宣言する必要があります");
            return null;
        }
        ExecutableElement method = (ExecutableElement) element;
        validateMethodModifiers(method);

        return method;
    }

    private void validateMethodModifiers(ExecutableElement method) {
        assert method != null;
        if (method.getModifiers().contains(Modifier.PUBLIC) == false) {
            raiseInvalid(method, "演算子メソッド{0}はpublicとして宣言する必要があります");
        }
        if (method.getModifiers().contains(Modifier.STATIC)) {
            raiseInvalid(method, "演算子メソッド{0}はstaticとして宣言してはいけません");
        }
        if (method.getThrownTypes().isEmpty() == false) {
            raiseInvalid(method, "演算子メソッド{0}には例外型を指定できません");
        }
    }

    private void raiseInvalid(Element member, String message) {
        assert member != null;
        assert message != null;
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
            MessageFormat.format(
                    message,
                    member.getSimpleName()),
            member);
        sawError = true;
    }

    /**
     * ここまでに {@link #add(OperatorProcessor)} に指定された演算子プロセッサを元に
     * 構築した演算子クラスの一覧を返す。
     * @return 構築した演算子クラスの一覧
     * @throws OperatorCompilerException 構築に失敗した場合
     */
    public List<OperatorClass> collect() {
        if (sawError) {
            throw new OperatorCompilerException("演算子メソッドの分析に失敗したため、処理を中止します");
        }
        Map<TypeElement, List<TargetMethod>> mapping = Maps.create();
        for (TargetMethod target : targetMethods) {
            Maps.addToList(mapping, target.type, target);
        }

        List<OperatorClass> results = Lists.create();
        for (Map.Entry<TypeElement, List<TargetMethod>> entry : mapping.entrySet()) {
            OperatorClass klass = toOperatorClass(entry.getKey(), entry.getValue());
            results.add(klass);
        }

        if (sawError) {
            throw new OperatorCompilerException("演算子クラスの分析に失敗したため、処理を中止します");
        }
        return results;
    }

    private OperatorClass toOperatorClass(
            TypeElement type,
            List<TargetMethod> targets) {
        assert type != null;
        assert targets != null;
        validateClassModifiers(type);
        validateConstructorWithNoParameters(type);
        validateMemberNames(type);
        validateCoverage(type, targets);

        OperatorClass result = new OperatorClass(type);
        for (TargetMethod target : targets) {
            result.add(target.method, target.processor);
        }
        return result;
    }

    private void validateClassModifiers(TypeElement type) {
        assert type != null;
        if (type.getKind() != ElementKind.CLASS) {
            raiseInvalidClass(type, "演算子クラス{0}はクラスとして宣言する必要があります");
        }
        if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            raiseInvalidClass(type, "演算子クラス{0}はパッケージ直下のトップレベルクラスとして宣言する必要があります");
        }
        if (type.getTypeParameters().isEmpty() == false) {
            raiseInvalidClass(type, "演算子クラス{0}には型引数を指定できません");
        }
        if (type.getModifiers().contains(Modifier.PUBLIC) == false) {
            raiseInvalidClass(type, "演算子クラス{0}はpublicとして宣言する必要があります");
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT) == false) {
            raiseInvalidClass(type, "演算子クラス{0}はabstractとして宣言する必要があります");
        }
    }

    private void validateConstructorWithNoParameters(TypeElement type) {
        assert type != null;
        List<ExecutableElement> ctors = ElementFilter.constructorsIn(type.getEnclosedElements());
        if (ctors.isEmpty()) {
            return;
        }
        for (ExecutableElement ctor : ctors) {
            if (ctor.getParameters().isEmpty()
                    && ctor.getTypeParameters().isEmpty()
                    && ctor.getThrownTypes().isEmpty()) {
                return;
            }
        }
        raiseInvalidClass(type, "演算子クラス{0}には引数、型引数、例外宣言がないpublicなコンストラクタを宣言する必要があります");
    }

    private void validateMemberNames(TypeElement type) {
        Map<String, Element> saw = Maps.create();
        for (Element member : type.getEnclosedElements()) {
            ElementKind kind = member.getKind();
            if (kind != ElementKind.METHOD
                    && kind.isClass() == false
                    && kind.isInterface() == false) {
                continue;
            }
            String id = member.getSimpleName().toString().toUpperCase();
            if (saw.containsKey(id)) {
                raiseInvalid(member, MessageFormat.format(
                    "{0}は演算子クラス内のほかのメンバー{1}と異なる名前を指定してください",
                    "{0}", //$NON-NLS-1$
                    member.getSimpleName()));
            } else {
                saw.put(id, member);
            }
        }
    }

    private void validateCoverage(TypeElement type, List<TargetMethod> targets) {
        assert type != null;
        assert targets != null;

        Set<ExecutableElement> methods = Sets.create();
        methods.addAll(ElementFilter.methodsIn(type.getEnclosedElements()));

        Set<ExecutableElement> saw = Sets.create();
        for (TargetMethod target : targets) {
            ExecutableElement method = target.method;
            if (saw.contains(method)) {
                raiseInvalid(method, "演算子メソッド{0}には複数の演算子注釈が付与されています");
            } else {
                saw.add(method);
                boolean removed = methods.remove(method);
                assert removed : method;
            }
        }
        for (ExecutableElement method : methods) {
            boolean helper = isOperatorHelper(method);
            boolean open = method.getModifiers().contains(Modifier.PUBLIC);
            if (helper && open == false) {
                raiseInvalid(method, "演算子補助注釈の付いたメソッドはpublicである必要があります");
            } else if (helper == false && open) {
                raiseInvalid(method, "演算子クラスには演算子メソッド以外のpublicメソッドを宣言できません");
            }
        }
    }

    private boolean isOperatorHelper(ExecutableElement method) {
        assert method != null;
        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            DeclaredType annotationType = mirror.getAnnotationType();
            Element element = annotationType.asElement();
            if (element != null && element.getAnnotation(OperatorHelper.class) != null) {
                return true;
            }
        }
        return false;
    }

    private void raiseInvalidClass(TypeElement element, String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
            MessageFormat.format(
                    message,
                    element.getQualifiedName()),
                    element);
        sawError = true;
    }

    private static class TargetMethod {

        final TypeElement type;

        final ExecutableElement method;

        final OperatorProcessor processor;

        public TargetMethod(
                ExecutableElement method,
                OperatorProcessor processor) {
            assert method != null;
            assert processor != null;
            this.type = (TypeElement) method.getEnclosingElement();
            this.method = method;
            this.processor = processor;
        }
    }
}
