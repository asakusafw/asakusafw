/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.DataModelMirror;
import com.asakusafw.compiler.operator.DataModelMirror.Kind;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.compiler.operator.OperatorProcessor;
import com.asakusafw.vocabulary.operator.MasterSelection;

/**
 * Analyzes MasterJoin-like operators.
 */
public final class MasterKindOperatorAnalyzer {

    /**
     * Returns the selector method for the target operator method.
     * @param context the current context
     * @return the selector method, or {@code null} if the operator method does not use one
     * @throws ResolveException if error occurred while resolving language elements
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static ExecutableElement findSelector(OperatorProcessor.Context context) throws ResolveException {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        String selectorName = getSelectorName(context);
        if (selectorName == null) {
            return null;
        }
        ExecutableElement selectorMethod = getSelectorMethod(context, selectorName);
        checkParameters(context.environment, context.element, selectorMethod);
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
        checkParameterCount(operatorMethod, selectorMethod);
        DataModelMirror operatorMaster = environment.loadDataModel(operatorParams.get(0).asType());
        DataModelMirror selectorMaster = extractSelectorMaster(
                environment, selectorMethod, selectorParams.get(0).asType());
        if (isValidMaster(operatorMaster, selectorMaster) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorAnalyzer.errorInvalidSelectorMaster"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorMaster));
        }
        if (selectorParams.size() == 1) {
            return;
        }
        DataModelMirror operatorTx = environment.loadDataModel(operatorParams.get(1).asType());
        DataModelMirror selectorTx = environment.loadDataModel(selectorParams.get(1).asType());
        if (isValidTx(operatorTx, selectorTx) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorAnalyzer.errorInvalidSelectorTransaction"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorTx));
        }
        DataModelMirror selectorResult = environment.loadDataModel(selectorMethod.getReturnType());
        if (isValidResult(operatorMaster, selectorMaster, selectorResult) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorAnalyzer.errorInvalidSelectorResult"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorMaster));
        }
        for (int i = 2, n = selectorParams.size(); i < n; i++) {
            TypeMirror expected = operatorParams.get(i).asType();
            TypeMirror actual = selectorParams.get(i).asType();
            if (environment.getTypeUtils().isSubtype(expected, actual) == false) {
                throw new ResolveException(MessageFormat.format(
                        Messages.getString(
                                "MasterKindOperatorAnalyzer.errorInconsistentSelectorOptionParameter"), //$NON-NLS-1$
                        selectorMethod.getSimpleName(),
                        expected,
                        String.valueOf(i + 1)));
            }
        }
    }

    private static boolean isValidMaster(DataModelMirror operatorMaster, DataModelMirror selectorMaster) {
        if (operatorMaster == null || selectorMaster == null) {
            return false;
        }
        return operatorMaster.canContain(selectorMaster);
    }

    private static boolean isValidTx(DataModelMirror operatorTx, DataModelMirror selectorTx) {
        if (operatorTx == null || selectorTx == null) {
            return false;
        }
        return operatorTx.canInvoke(selectorTx);
    }

    private static boolean isValidResult(
            DataModelMirror operatorMaster,
            DataModelMirror selectorMaster,
            DataModelMirror selectorResult) {
        if (operatorMaster == null || selectorMaster == null || selectorResult == null) {
            return false;
        }
        if (selectorResult.canInvoke(operatorMaster)) {
            return true;
        }
        // FIXME restrict
        if (selectorMaster.getKind() == Kind.PARTIAL && selectorMaster.isSame(selectorResult)) {
            return true;
        }
        return false;
    }

    private static void checkParameterCount(
            ExecutableElement operatorMethod,
            ExecutableElement selectorMethod) throws ResolveException {
        assert operatorMethod != null;
        assert selectorMethod != null;
        List<? extends VariableElement> operatorParams = operatorMethod.getParameters();
        List<? extends VariableElement> selectorParams = selectorMethod.getParameters();
        if (operatorParams.size() < selectorParams.size()) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorAnalyzer.errorExtraSelectorParameter"), //$NON-NLS-1$
                    selectorMethod.getSimpleName()));
        }
        if (selectorParams.size() == 0) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorAnalyzer.errorInconsistentSelectorMasterType"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorParams.get(0).asType()));
        }
    }

    private static DataModelMirror extractSelectorMaster(
            OperatorCompilingEnvironment environment,
            ExecutableElement selectorMethod,
            TypeMirror firstParameter) throws ResolveException {
        assert environment != null;
        assert selectorMethod != null;
        assert firstParameter != null;
        TypeMirror erasedSelector = environment.getErasure(firstParameter);
        Types types = environment.getTypeUtils();
        if (types.isSameType(erasedSelector, environment.getDeclaredType(List.class)) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString(
                            "MasterKindOperatorAnalyzer.errorInvalidSelectorMasterContainer"), //$NON-NLS-1$
                    selectorMethod.getSimpleName()));
        }
        DeclaredType list = (DeclaredType) firstParameter;
        if (list.getTypeArguments().size() != 1) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString(
                            "MasterKindOperatorAnalyzer.errorInvalidSelectorMasterTypeArgument"), //$NON-NLS-1$
                    selectorMethod.getSimpleName()));
        }
        TypeMirror selectorElement = list.getTypeArguments().get(0);
        return environment.loadDataModel(selectorElement);
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
                            Messages.getString(
                                    "MasterKindOperatorAnalyzer.errorMissingSelectorAnnotation"), //$NON-NLS-1$
                            selectorName,
                            MasterSelection.class.getSimpleName()));
                }
                return (ExecutableElement) member;
            }
        }
        throw new ResolveException(MessageFormat.format(
                Messages.getString("MasterKindOperatorAnalyzer.errorMissingSelectorMethod"), //$NON-NLS-1$
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
     * An exception for tell that resolving elements are failed.
     */
    public static class ResolveException extends Exception {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance.
         * @param message the exception message (nullable)
         */
        public ResolveException(String message) {
            super(message);
        }

        /**
         * Creates a new instance.
         * @param message the exception message (nullable)
         * @param cause the original cause (nullable)
         */
        public ResolveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
