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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.compiler.operator.OperatorProcessor;
import com.asakusafw.vocabulary.operator.MasterSelection;


/**
 * {@code Master*}系の演算子を解析する。
 */
public final class MasterKindOperatorAnalyzer {

    /**
     * 指定の演算子メソッドに対するマスタ選択補助演算子を検出する。
     * @param environment コンパイラの環境
     * @param context 現在の文脈
     * @return 発見した補助演算子、指定しない場合は{@code null}
     * @throws ResolveException 要素の解決に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static ExecutableElement findSelector(
            OperatorCompilingEnvironment environment,
            OperatorProcessor.Context context) throws ResolveException {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        String selectorName = getSelectorName(context);
        if (selectorName == null) {
            return null;
        }
        ExecutableElement selectorMethod = getSelectorMethod(context, selectorName);
        checkParameters(environment, context.element, selectorMethod);
        return selectorMethod;
    }

    private static void checkParameters(
            OperatorCompilingEnvironment environment,
            ExecutableElement operatorMethod,
            ExecutableElement selectorMethod) throws ResolveException {
        assert environment != null;
        assert operatorMethod != null;
        assert selectorMethod != null;
        assert operatorMethod.getParameters().isEmpty() == false;
        List<? extends VariableElement> operatorParams = operatorMethod.getParameters();
        List<? extends VariableElement> selectorParams = selectorMethod.getParameters();
        if (operatorParams.size() < selectorParams.size()) {
            throw new ResolveException(MessageFormat.format(
                    "マスタ選択を行うメソッド{0}は、この演算子の引数と同じかそれよりも少ない数の引数のみ宣言できます",
                    selectorMethod.getSimpleName()));
        }
        if (selectorParams.size() == 0) {
            throw new ResolveException(MessageFormat.format(
                    "マスタ選択を行うメソッド{0}には、{1}のリストをとる第1引数が必要です",
                    selectorMethod.getSimpleName(),
                    operatorParams.get(0).asType()));
        } else if (selectorParams.size() == 1) {
            throw new ResolveException(MessageFormat.format(
                    "マスタ選択を行うメソッド{0}には、{1}をとる第2引数が必要です",
                    selectorMethod.getSimpleName(),
                    operatorParams.get(1).asType()));
        }
        if (selectorMethod.getTypeParameters().isEmpty() == false) {
            checkGenericParameters(environment, operatorMethod, selectorMethod);
        } else {
            checkNormalParameters(environment, operatorMethod, selectorMethod);
        }
    }

    private static void checkNormalParameters(
            OperatorCompilingEnvironment environment,
            ExecutableElement operatorMethod,
            ExecutableElement selectorMethod) throws ResolveException {
        assert environment != null;
        assert operatorMethod != null;
        assert selectorMethod != null;
        List<? extends VariableElement> operatorParams = operatorMethod.getParameters();
        List<? extends VariableElement> selectorParams = selectorMethod.getParameters();
        Types types = environment.getTypeUtils();
        {
            // type of List<Master>
            TypeMirror expected = types.getDeclaredType(environment.getElementUtils()
                .getTypeElement(List.class.getName()), operatorParams.get(0).asType());
            TypeMirror actual = selectorParams.get(0).asType();
            if (types.isSubtype(expected, actual) == false) {
                throw new ResolveException(MessageFormat.format(
                        "マスタ選択を行うメソッド{0}の1つめの引数は、{1}のスーパータイプでなければなりません",
                        selectorMethod.getSimpleName(),
                        expected));
            }
        }
        for (int i = 1, n = selectorParams.size(); i < n; i++) {
            TypeMirror expected = operatorParams.get(i).asType();
            TypeMirror actual = selectorParams.get(i).asType();
            if (types.isSubtype(expected, actual) == false) {
                throw new ResolveException(MessageFormat.format(
                        "マスタ選択を行うメソッド{0}の{2}つめの引数は、{1}のスーパータイプでなければなりません",
                        selectorMethod.getSimpleName(),
                        expected,
                        String.valueOf(i + 1)));
            }
        }
        {
            TypeMirror expected = operatorParams.get(0).asType();
            TypeMirror actual = selectorMethod.getReturnType();
            if (types.isSubtype(actual, expected) == false) {
                throw new ResolveException(MessageFormat.format(
                        "マスタ選択を行うメソッド{0}の戻り値は、{1}のサブタイプでなければなりません",
                        selectorMethod.getSimpleName(),
                        expected));
            }
        }
    }

    private static void checkGenericParameters(
            OperatorCompilingEnvironment environment,
            ExecutableElement operatorMethod,
            ExecutableElement selectorMethod) throws ResolveException {
        List<? extends VariableElement> operatorParams = operatorMethod.getParameters();
        List<? extends VariableElement> selectorParams = selectorMethod.getParameters();
        Types types = environment.getTypeUtils();
        TypeMirror master = captureMaster(environment, operatorMethod, selectorMethod);
        {
            TypeMirror operator = operatorMethod.getParameters().get(1).asType();
            TypeMirror selector = selectorMethod.getParameters().get(1).asType();
            if (selector.getKind() == TypeKind.TYPEVAR) {
                TypeVariable var = (TypeVariable) selector;
                // bound check
                if (types.isSubtype(operator, var.getUpperBound()) == false) {
                    throw new ResolveException(MessageFormat.format(
                            "マスタ選択を行うメソッド{0}の2つめの引数は、{1}のスーパータイプでなければなりません",
                            selectorMethod.getSimpleName(),
                            operator));
                }
            } else {
                if (types.isSubtype(operator, selector) == false) {
                    throw new ResolveException(MessageFormat.format(
                            "マスタ選択を行うメソッド{0}の2つめの引数は、{1}のスーパータイプでなければなりません",
                            selectorMethod.getSimpleName(),
                            operator));
                }
            }
        }
        for (int i = 2, n = selectorParams.size(); i < n; i++) {
            TypeMirror expected = operatorParams.get(i).asType();
            TypeMirror actual = selectorParams.get(i).asType();
            if (types.isSubtype(expected, actual) == false) {
                throw new ResolveException(MessageFormat.format(
                        "マスタ選択を行うメソッド{0}の{2}つめの引数は、{1}のスーパータイプでなければなりません",
                        selectorMethod.getSimpleName(),
                        expected,
                        String.valueOf(i + 1)));
            }
        }
        {
            TypeMirror actual = selectorMethod.getReturnType();
            if (types.isSubtype(actual, master) == false) {
                throw new ResolveException(MessageFormat.format(
                        "マスタ選択を行うメソッド{0}の戻り値は、{1}のサブタイプでなければなりません",
                        selectorMethod.getSimpleName(),
                        master));
            }
        }
    }

    private static TypeMirror captureMaster(
            OperatorCompilingEnvironment environment,
            ExecutableElement operatorMethod,
            ExecutableElement selectorMethod) throws ResolveException {
        assert environment != null;
        assert operatorMethod != null;
        assert selectorMethod != null;
        TypeMirror selector = selectorMethod.getParameters().get(0).asType();
        TypeMirror erasedSelector = environment.getErasure(selector);
        Types types = environment.getTypeUtils();
        if (types.isSameType(erasedSelector, environment.getDeclaredType(List.class)) == false) {
            throw new ResolveException(MessageFormat.format(
                    "マスタ選択を行うメソッド{0}の1つめの引数は、List<...>の形式でなければなりません",
                    selectorMethod.getSimpleName()));
        }
        DeclaredType list = (DeclaredType) selector;
        if (list.getTypeArguments().size() != 1) {
            throw new ResolveException(MessageFormat.format(
                    "マスタ選択を行うメソッド{0}の1つめの引数は、List<...>の形式でなければなりません",
                    selectorMethod.getSimpleName()));
        }
        TypeMirror selectorElement = list.getTypeArguments().get(0);
        TypeMirror operator = operatorMethod.getParameters().get(0).asType();
        if (selectorElement.getKind() == TypeKind.TYPEVAR) {
            TypeVariable var = (TypeVariable) selectorElement;
            // bound check
            if (types.isSubtype(operator, var.getUpperBound()) == false) {
                throw new ResolveException(MessageFormat.format(
                        "マスタ選択を行うメソッド{0}の1つめの引数Listは、要素に{1}を取れなければなりません",
                        selectorMethod.getSimpleName(),
                        operator));
            }
            return var;
        } else {
            if (types.isSameType(operator, selectorElement) == false) {
                throw new ResolveException(MessageFormat.format(
                        "マスタ選択を行うメソッド{0}の1つめの引数Listは、要素に{1}を取れなければなりません",
                        selectorMethod.getSimpleName(),
                        operator));
            }
        }
        return selectorElement;
    }

    private static ExecutableElement getSelectorMethod(
            OperatorProcessor.Context context,
            String selectorName) throws ResolveException {
        assert context != null;
        assert selectorName != null;
        for (Element member : context.element.getEnclosingElement().getEnclosedElements()) {
            if (member.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (member.getSimpleName().contentEquals(selectorName)) {
                if (member.getAnnotation(MasterSelection.class) == null) {
                    throw new ResolveException(MessageFormat.format(
                            "マスタ選択の補助演算子{0}には@{1}が付与されている必要があります",
                            selectorName,
                            MasterSelection.class.getSimpleName()));
                }
                return (ExecutableElement) member;
            }
        }
        throw new ResolveException(MessageFormat.format(
                "マスタ選択の補助演算子{0}が見つかりません",
                selectorName));
    }

    private static String getSelectorName(OperatorProcessor.Context context) {
        assert context != null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : context.annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(MasterSelection.ELEMENT_NAME)) {
                Object value = entry.getValue().getValue();
                if (value instanceof String) {
                    return (String) value;
                }
            }
        }
        return null;
    }

    private MasterKindOperatorAnalyzer() {
        return;
    }

    /**
     * 要素の解決に失敗したことを表す例外。
     */
    public static class ResolveException extends Exception {

        private static final long serialVersionUID = 1L;

        /**
         * インスタンスを生成する。
         * @param message 例外メッセージ
         */
        public ResolveException(String message) {
            super(message);
        }

        /**
         * インスタンスを生成する。
         * @param message 例外メッセージ
         * @param cause 原因となった例外
         */
        public ResolveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
