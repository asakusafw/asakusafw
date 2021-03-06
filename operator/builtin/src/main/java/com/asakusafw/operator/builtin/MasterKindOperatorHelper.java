/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.operator.builtin;

import java.text.MessageFormat;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.KeyRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.model.DataModelMirror.Kind;
import com.asakusafw.operator.util.AnnotationHelper;

final class MasterKindOperatorHelper {

    private static final ClassDescription TYPE_LIST = Descriptions.classOf(List.class);

    private static final String NAME_SELECTION = "selection"; //$NON-NLS-1$

    private static final String NO_SELECTION = "-"; //$NON-NLS-1$

    static final int INDEX_MASTER = 0;

    static final int INDEX_TX = 1;

    static final int INDEX_EXTRA_START = 2;

    private static final int SIZE_INPUT_MANDATORY = 2;

    public static void consumeMaster(DslBuilder dsl) {
        ElementRef p = dsl.parameter(INDEX_MASTER);
        TypeRef type = p.type();
        if (type.isDataModel()) {
            KeyRef key = p.resolveKey(type);
            dsl.addInput(p.document(), p.name(), p.type().mirror(), key, p.reference());
        } else {
            p.error(Messages.getString("MasterKindOperatorHelper.errorMasterInputNotDataModelType")); //$NON-NLS-1$
        }
    }

    public static void consumeTx(DslBuilder dsl) {
        ElementRef p = dsl.parameter(INDEX_TX);
        TypeRef type = p.type();
        if (type.isDataModel()) {
            KeyRef key = p.resolveKey(type);
            dsl.addInput(p.document(), p.name(), p.type().mirror(), key, p.reference());
        } else {
            p.error(Messages.getString("MasterKindOperatorHelper.errorTransactionInputNotDataModelType")); //$NON-NLS-1$
        }
    }

    public static ExecutableElement extractMasterSelection(DslBuilder dsl) {
        String selection = dsl.annotation().string(NAME_SELECTION);
        if (selection == null || selection.equals(NO_SELECTION)) {
            return null;
        }
        ExecutableElement selector = findSelector(dsl, selection);
        if (selector == null) {
            return null;
        }
        validateSelectorDeclaration(dsl, selector);
        if (dsl.sawError()) {
            return null;
        }
        try {
            checkParameters(dsl.getEnvironment(), dsl.method, selector);
        } catch (ResolveException e) {
            dsl.annotation().error(NAME_SELECTION, e.getMessage());
        }
        return selector;
    }

    private static ExecutableElement findSelector(DslBuilder dsl, String selection) {
        assert selection != null;
        ClassDescription className = Constants.getBuiltinOperatorClass("MasterSelection"); //$NON-NLS-1$
        TypeElement annotationDecl = dsl.getEnvironment().findTypeElement(className);
        if (annotationDecl == null) {
            dsl.annotation().error(NAME_SELECTION,
                    Messages.getString("MasterKindOperatorHelper.errorFailedToResolveMasterSelectionType")); //$NON-NLS-1$
            return null;
        }

        ExecutableElement result = null;
        TypeElement declaring = (TypeElement) dsl.getMethod().getEnclosingElement();
        for (ExecutableElement element : ElementFilter.methodsIn(declaring.getEnclosedElements())) {
            if (element.getSimpleName().contentEquals(selection)) {
                AnnotationMirror annotation = AnnotationHelper.findAnnotation(
                        dsl.getEnvironment(),
                        annotationDecl,
                        element);
                if (annotation == null) {
                    continue;
                }
                if (result == null) {
                    result = element;
                } else {
                    dsl.annotation().error(NAME_SELECTION,
                            Messages.getString("MasterKindOperatorHelper.errorAmbiguousSelectorMethod")); //$NON-NLS-1$
                    return null;
                }
            }
        }

        if (result == null) {
            dsl.annotation().error(NAME_SELECTION,
                    Messages.getString("MasterKindOperatorHelper.errorMissingSelectorMethod")); //$NON-NLS-1$
            return null;
        }
        return result;
    }

    private static void validateSelectorDeclaration(DslBuilder dsl, ExecutableElement selector) {
        assert selector != null;
        if (selector.getModifiers().contains(Modifier.PUBLIC) == false) {
            dsl.annotation().error(NAME_SELECTION,
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodNotPublic")); //$NON-NLS-1$
        }
        if (selector.getModifiers().contains(Modifier.ABSTRACT)) {
            dsl.annotation().error(NAME_SELECTION,
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodAbstract")); //$NON-NLS-1$
        }
        if (selector.getModifiers().contains(Modifier.STATIC)) {
            dsl.annotation().error(NAME_SELECTION,
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodStatic")); //$NON-NLS-1$
        }
    }

    private static void checkParameters(
            CompileEnvironment environment,
            ExecutableElement operatorMethod,
            ExecutableElement selectorMethod) throws ResolveException {
        assert environment != null;
        assert operatorMethod != null;
        assert selectorMethod != null;
        assert operatorMethod.getParameters().isEmpty() == false;
        List<? extends VariableElement> operatorParams = operatorMethod.getParameters();
        List<? extends VariableElement> selectorParams = selectorMethod.getParameters();
        checkParameterCount(operatorMethod, selectorMethod);
        DataModelMirror operatorMaster = environment.findDataModel(operatorParams.get(INDEX_MASTER).asType());
        DataModelMirror selectorMaster = extractSelectorMaster(
                environment, selectorMethod, selectorParams.get(INDEX_MASTER).asType());
        DataModelMirror selectorResult = environment.findDataModel(selectorMethod.getReturnType());
        if (isValidResult(operatorMaster, selectorMaster, selectorResult) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodReturnInconsistentType"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorMaster));
        }
        if (isValidMaster(operatorMaster, selectorMaster) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodMasterInputInconsistentType"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorMaster));
        }
        checkRedundantKey(environment, selectorMethod, selectorParams.get(INDEX_MASTER));
        if (selectorParams.size() <= INDEX_TX) {
            return;
        }
        DataModelMirror operatorTx = environment.findDataModel(operatorParams.get(INDEX_TX).asType());
        DataModelMirror selectorTx = environment.findDataModel(selectorParams.get(INDEX_TX).asType());
        if (isValidTx(operatorTx, selectorTx) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodTransactionInputInconsistentType"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorTx));
        }
        checkRedundantKey(environment, selectorMethod, selectorParams.get(INDEX_TX));
        for (int i = INDEX_EXTRA_START, n = selectorParams.size(); i < n; i++) {
            TypeMirror expected = operatorParams.get(i).asType();
            TypeMirror actual = selectorParams.get(i).asType();
            if (environment.getProcessingEnvironment().getTypeUtils().isSubtype(expected, actual) == false) {
                throw new ResolveException(MessageFormat.format(
                        Messages.getString("MasterKindOperatorHelper.errorSelectorMethodParameterInconsistentType"), //$NON-NLS-1$
                        selectorMethod.getSimpleName(),
                        expected,
                        selectorParams.get(i)));
            }
            checkRedundantKey(environment, selectorMethod, selectorParams.get(i));
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
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodParameterTooMany"), //$NON-NLS-1$
                    selectorMethod.getSimpleName()));
        }
        if (selectorParams.size() == 0) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodParameterMissing"), //$NON-NLS-1$
                    selectorMethod.getSimpleName(),
                    operatorParams.get(0).asType()));
        }
    }

    private static void checkRedundantKey(
            CompileEnvironment environment,
            ExecutableElement selectorMethod,
            VariableElement parameter) {
        TypeElement annotationType = environment.findTypeElement(Constants.TYPE_KEY);
        if (annotationType == null) {
            return;
        }
        AnnotationMirror annotation = AnnotationHelper.findAnnotation(environment, annotationType, parameter);
        if (annotation != null) {
            environment.getProcessingEnvironment().getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    MessageFormat.format(
                            Messages.getString("MasterKindOperatorHelper.warnSelectorMethodParameterRedundantKey"), //$NON-NLS-1$
                            selectorMethod.getSimpleName(),
                            parameter.getSimpleName()),
                    parameter);
        }
    }

    private static DataModelMirror extractSelectorMaster(
            CompileEnvironment environment,
            ExecutableElement selectorMethod,
            TypeMirror firstParameter) throws ResolveException {
        assert environment != null;
        assert selectorMethod != null;
        assert firstParameter != null;
        TypeMirror erasedSelector = environment.getErasure(firstParameter);
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        if (types.isSubtype(environment.findDeclaredType(TYPE_LIST), erasedSelector) == false) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodMasterInputNotList"), //$NON-NLS-1$
                    selectorMethod.getSimpleName()));
        }
        DeclaredType list = (DeclaredType) firstParameter;
        if (list.getTypeArguments().size() != 1) {
            throw new ResolveException(MessageFormat.format(
                    Messages.getString("MasterKindOperatorHelper.errorSelectorMethodMasterInputRawList"), //$NON-NLS-1$
                    selectorMethod.getSimpleName()));
        }
        TypeMirror selectorElement = list.getTypeArguments().get(0);
        return environment.findDataModel(selectorElement);
    }

    public static List<ElementRef> consumeExtras(DslBuilder dsl) {
        return dsl.parametersFrom(INDEX_EXTRA_START);
    }

    public static boolean hasMandatoryInputs(DslBuilder dsl) {
        return dsl.getInputs().size() >= SIZE_INPUT_MANDATORY;
    }

    private MasterKindOperatorHelper() {
        return;
    }

    static class ResolveException extends Exception {

        private static final long serialVersionUID = 1L;

        ResolveException(String message) {
            super(message);
        }
    }
}
