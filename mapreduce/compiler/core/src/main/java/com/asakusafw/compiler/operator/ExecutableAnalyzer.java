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
package com.asakusafw.compiler.operator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.DataModelMirror.Kind;
import com.asakusafw.compiler.operator.DataModelMirror.PropertyMirror;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.parser.javadoc.JavadocConverter;
import com.asakusafw.utils.java.parser.javadoc.JavadocParseException;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.Summarized;
import com.asakusafw.vocabulary.operator.Sticky;
import com.asakusafw.vocabulary.operator.Volatile;

/**
 * Analyzes operator methods and constructors.
 * @since 0.1.0
 * @version 0.7.1
 */
public class ExecutableAnalyzer {

    final OperatorCompilingEnvironment environment;

    final ExecutableElement executable;

    final Javadoc documentation;

    private boolean sawError;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param executable the target executable element
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExecutableAnalyzer(OperatorCompilingEnvironment environment, ExecutableElement executable) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(executable, "executable"); //$NON-NLS-1$
        this.environment = environment;
        this.executable = executable;
        this.documentation = getJavadoc(environment, executable);
        this.sawError = false;
    }

    /**
     * Raises an error to this element.
     * @param message an error message, or message pattern if the arguments are not empty
     * @param arguments the message arguments
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public void error(String message, Object...arguments) {
        Precondition.checkMustNotBeNull(message, "message"); //$NON-NLS-1$
        environment.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                format(message, arguments),
                executable);
        sawError = true;
    }

    /**
     * Raises an error to a parameter of this element.
     * @param parameterIndex the target parameter index (0-origin)
     * @param message an error message, or message pattern if the arguments are not empty
     * @param arguments the message arguments
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public void error(int parameterIndex, String message, Object...arguments) {
        Precondition.checkMustNotBeNull(message, "message"); //$NON-NLS-1$
        if (parameterIndex < 0 || parameterIndex >= executable.getParameters().size()) {
            error(format(message, arguments));
            return;
        }
        environment.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                format(message, arguments),
                executable.getParameters().get(parameterIndex));
        sawError = true;
    }

    boolean typeEqual(TypeMirror a, TypeMirror b) {
        Types types = environment.getTypeUtils();
        return types.isSameType(a, b);
    }

    boolean typeDeclEqual(TypeMirror a, TypeMirror b) {
        if (a.getKind() != TypeKind.DECLARED) {
            return false;
        }
        if (b.getKind() != TypeKind.DECLARED) {
            return false;
        }
        Types types = environment.getTypeUtils();
        if (types.isSameType(a, b)) {
            return true;
        }
        DeclaredType at = (DeclaredType) a;
        DeclaredType bt = (DeclaredType) b;
        return at.asElement().equals(bt.asElement());
    }

    private String format(String message, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return message;
        } else {
            return MessageFormat.format(message, arguments);
        }
    }

    /**
     * Returns whether this analysis result contains any erroneous information or not.
     * @return {@code true} if this contains any erroneous information, otherwise {@code false}
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * Returns whether this element is declared as {@code abstract} or not.
     * @return {@code true} if this element is declared as {@code abstract}, otherwise {@code false}
     */
    public boolean isAbstract() {
        return executable.getModifiers().contains(Modifier.ABSTRACT);
    }

    /**
     * Returns {@code true} iff this element is declared as a generic method.
     * @return {@code true} iff this element is declared as a generic method
     */
    public boolean isGeneric() {
        return executable.getTypeParameters().isEmpty() == false;
    }

    /**
     * Returns the {@link ObservationCount} attribute of this element.
     * @param defaults the default constraints
     * @return the extracted attribute
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ObservationCount getObservationCount(ObservationCount... defaults) {
        Precondition.checkMustNotBeNull(defaults, "defaults"); //$NON-NLS-1$
        ObservationCount current = ObservationCount.DONT_CARE;
        for (ObservationCount oc : defaults) {
            current = current.and(oc);
        }
        if (current.atLeastOnce == false) {
            if (executable.getAnnotation(Sticky.class) != null) {
                current = current.and(ObservationCount.AT_LEAST_ONCE);
            }
        }
        if (current.atMostOnce == false) {
            if (executable.getAnnotation(Volatile.class) != null) {
                current = current.and(ObservationCount.AT_MOST_ONCE);
            }
        }
        return current;
    }

    /**
     * Returns the documentation of the target element.
     * @param element the target element
     * @return the documentation, or an empty list if the target element does not have any documentations
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<? extends DocElement> getDocument(Element element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Javadoc doc = getJavadoc(environment, element);
        return getAbstractBlock(doc);
    }

    private static Javadoc getJavadoc(
            OperatorCompilingEnvironment environment,
            Element element) {
        assert environment != null;
        assert element != null;
        ModelFactory f = environment.getFactory();
        String comment = environment.getElementUtils().getDocComment(element);
        if (comment == null) {
            return f.newJavadoc(Collections.emptyList());
        }
        if (comment.startsWith("/**") == false) { //$NON-NLS-1$
            comment = "/**" + comment; //$NON-NLS-1$
        }
        if (comment.endsWith("*/") == false) { //$NON-NLS-1$
            comment = comment + "*/"; //$NON-NLS-1$
        }

        try {
            return new JavadocConverter(f).convert(comment, 0);
        } catch (JavadocParseException e) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    e.getMessage(),
                    element);
            return f.newJavadoc(Collections.emptyList());
        }
    }

    private static List<? extends DocElement> getAbstractBlock(Javadoc doc) {
        assert doc != null;
        List<? extends DocBlock> blocks = doc.getBlocks();
        if (blocks.isEmpty()) {
            return Collections.emptyList();
        }
        DocBlock first = blocks.get(0);
        if (first.getTag().equals("") == false) { //$NON-NLS-1$
            return Collections.emptyList();
        }
        return first.getElements();
    }

    /**
     * Returns the number of parameters in this element.
     * @return the number of parameters in this element
     */
    public int countParameters() {
        return executable.getParameters().size();
    }

    /**
     * Returns the type constraint object of the return type.
     * @return the return type constraint
     */
    public TypeConstraint getReturnType() {
        return new TypeConstraint(executable.getReturnType());
    }

    /**
     * Returns the type constraint object of the target parameter type.
     * @param index the target parameter index (0-origin)
     * @return the parameter type constraint
     */
    public TypeConstraint getParameterType(int index) {
        List<? extends VariableElement> parameters = executable.getParameters();
        if (index >= parameters.size()) {
            return new TypeConstraint(environment.getTypeUtils().getNoType(TypeKind.NONE));
        }
        return new TypeConstraint(parameters.get(index).asType());
    }

    /**
     * Returns the parameter name.
     * @param index the target parameter index (0-origin)
     * @return the parameter name
     */
    public String getParameterName(int index) {
        List<? extends VariableElement> parameters = executable.getParameters();
        String name = parameters.get(index).getSimpleName().toString();
        return name;
    }

    /**
     * Returns the shuffle key of the target parameter.
     * @param index the target parameter index (0-origin)
     * @return the shuffle key of the target parameter
     * @deprecated Use {@link #getParameterKeySpec(int)} instead
     */
    @Deprecated
    public ShuffleKey getParameterKey(int index) {
        VariableElement parameter = executable.getParameters().get(index);
        TypeConstraint type = getParameterType(index);
        TypeConstraint arg = type.getTypeArgument();
        if (arg.exists()) {
            type = arg;
        }
        DataModelMirror model = environment.loadDataModel(type.getType());
        if (model == null) {
            return null;
        }
        return toShuffleKey(index, model, findAnnotation(parameter, environment.getDeclaredType(Key.class)));
    }

    /**
     * Returns the shuffle key spec of the target parameter.
     * @param index the parameter index
     * @return the spec, or {@code null} if the key is not valid
     * @since 0.7.1
     */
    public ShuffleKeySpec getParameterKeySpec(int index) {
        VariableElement parameter = executable.getParameters().get(index);
        TypeConstraint type = getParameterType(index);
        TypeConstraint arg = type.getTypeArgument();
        if (arg.exists()) {
            type = arg;
        }
        DataModelMirror model = environment.loadDataModel(type.getType());
        if (model == null) {
            return null;
        }
        ShuffleKey key = toShuffleKey(index, model, findAnnotation(parameter, environment.getDeclaredType(Key.class)));
        if (key == null) {
            return null;
        }
        return new ShuffleKeySpec(index, model, key);
    }

    /**
     * Validates shuffle keys.
     * @param keys keys
     */
    public void validateShuffleKeys(ShuffleKeySpec... keys) {
        validateShuffleKeys(Arrays.asList(keys));
    }

    /**
     * Validates shuffle keys.
     * @param keys keys
     */
    public void validateShuffleKeys(List<ShuffleKeySpec> keys) {
        // ignores if errors are occurred
        if (hasError() || keys.isEmpty()) {
            return;
        }
        for (ShuffleKeySpec key : keys) {
            if (key == null) {
                return;
            }
        }
        assert keys.isEmpty() == false;
        ShuffleKeySpec first = keys.get(0);
        for (int i = 1, inputCount = keys.size(); i < inputCount; i++) {
            ShuffleKeySpec spec = keys.get(i);
            List<PropertyMirror> aGroup = first.groupProperties;
            List<PropertyMirror> bGroup = spec.groupProperties;
            if (aGroup.size() != bGroup.size()) {
                error(spec.position,
                        Messages.getString("ExecutableAnalyzer.errorInconsistentGroupKeyCount")); //$NON-NLS-1$
                continue;
            }
            assert aGroup.size() == bGroup.size();
            Types types = environment.getTypeUtils();
            for (int pIndex = 0, propertyCount = aGroup.size(); pIndex < propertyCount; pIndex++) {
                PropertyMirror aProperty = aGroup.get(pIndex);
                PropertyMirror bProperty = bGroup.get(pIndex);
                if (aProperty == null || bProperty == null) {
                    continue;
                }
                if (types.isSameType(aProperty.getType(), bProperty.getType()) == false) {
                    error(spec.position,
                            Messages.getString("ExecutableAnalyzer.errorInconsistentGroupKeyType"), //$NON-NLS-1$
                            getParameterName(spec.position),
                            spec.key.getGroupProperties().get(pIndex),
                            getParameterName(first.position),
                            first.key.getGroupProperties().get(pIndex));
                }
            }
        }
    }

    /**
     * Returns the documentation of this element (only abstract section).
     * @return the documentation of this element, or an empty list if this does not have any documents
     */
    public List<? extends DocElement> getExecutableDocument() {
        Javadoc doc = documentation;
        return getAbstractBlock(doc);
    }

    /**
     * Returns the documentation of the target parameter.
     * @param index the target parameter index (0-origin)
     * @return the documentation of the target parameter, or an empty list if this does not have any documents
     */
    public List<? extends DocElement> getParameterDocument(int index) {
        String name = getParameterName(index);
        for (DocBlock block : documentation.getBlocks()) {
            if (block.getTag().equals("@param") == false) { //$NON-NLS-1$
                continue;
            }
            List<? extends DocElement> elements = block.getElements();
            if (elements.isEmpty()) {
                continue;
            }
            DocElement first = elements.get(0);
            if (first.getModelKind() != ModelKind.SIMPLE_NAME) {
                continue;
            }
            if (name.equals(((SimpleName) first).getToken()) == false) {
                continue;
            }
            return elements.subList(1, elements.size());
        }
        return Collections.emptyList();
    }

    /**
     * Returns the documentation of the return value.
     * @return the documentation of the return value, or an empty list if this does not have any documents
     */
    public List<? extends DocElement> getReturnDocument() {
        for (DocBlock block : documentation.getBlocks()) {
            if (block.getTag().equals("@return") == false //$NON-NLS-1$
                    && block.getTag().equals("@returns") == false) { //$NON-NLS-1$
                continue;
            }
            return block.getElements();
        }
        return Collections.emptyList();
    }

    ShuffleKey toShuffleKey(
            int position,
            DataModelMirror model,
            AnnotationMirror annotation) {
        if (annotation == null) {
            return null;
        }
        ShuffleKey key = toUncheckedShuffleKey(position, annotation);
        if (key == null) {
            return null;
        }
        checkShuffleKey(position, model, key);
        return key;
    }

    ShuffleKey toUncheckedShuffleKey(
            int position,
            AnnotationMirror annotation) {
        assert annotation != null;
        Map<String, AnnotationValue> values = getValues(annotation);
        List<String> group = toStringList(values.get("group")); //$NON-NLS-1$
        List<String> order = toStringList(values.get("order")); //$NON-NLS-1$
        if (group == null) {
            error(position, Messages.getString("ExecutableAnalyzer.errorMissingGroup")); //$NON-NLS-1$
            return null;
        }
        if (order == null) {
            order = Collections.emptyList();
        }
        List<ShuffleKey.Order> formedOrder = new ArrayList<>();
        for (String orderString : order) {
            ShuffleKey.Order o = ShuffleKey.Order.parse(orderString);
            if (o == null) {
                error(position,
                        Messages.getString("ExecutableAnalyzer.errorInvalidOrderKey"), orderString); //$NON-NLS-1$
            } else {
                formedOrder.add(o);
            }
        }
        return new ShuffleKey(group, formedOrder);
    }

    private void checkShuffleKey(int position, DataModelMirror model, ShuffleKey key) {
        assert model != null;
        assert key != null;
        for (String name : key.getGroupProperties()) {
            if (model.findProperty(name) == null) {
                error(position, Messages.getString("ExecutableAnalyzer.errorUnknownGroupKey"), //$NON-NLS-1$
                        name,
                        model);
            }
        }
        for (ShuffleKey.Order order : key.getOrderings()) {
            if (model.findProperty(order.getProperty()) == null) {
                error(position, Messages.getString("ExecutableAnalyzer.errorUnknownOrderKey"), //$NON-NLS-1$
                        order.getProperty(),
                        model);
            }
        }
    }

    List<String> toStringList(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        Object object = value.getValue();
        if (object instanceof String) {
            return Collections.singletonList((String) object);
        }
        if ((object instanceof List<?>) == false) {
            return null;
        }
        List<?> list = (List<?>) object;
        List<String> results = new ArrayList<>();
        for (Object element : list) {
            Object elementValue = ((AnnotationValue) element).getValue();
            if ((elementValue instanceof String) == false) {
                return null;
            }
            results.add((String) elementValue);
        }
        return results;
    }

    AnnotationMirror findAnnotation(
            Element elem,
            DeclaredType annotationType) {
        assert annotationType != null;
        if (elem == null) {
            return null;
        }
        for (AnnotationMirror annotation : elem.getAnnotationMirrors()) {
            DeclaredType aType = annotation.getAnnotationType();
            if (typeEqual(aType, annotationType)) {
                return annotation;
            }
        }
        return null;
    }

    static <T> T getValue(
            Class<T> valueType,
            Map<String, AnnotationValue> valueMap,
            String name) {
        assert valueType != null;
        assert valueMap != null;
        assert name != null;
        AnnotationValue value = valueMap.get(name);
        if (value == null) {
            return null;
        }
        Object content = value.getValue();
        if (valueType.isInstance(content) == false) {
            return null;
        }
        return valueType.cast(content);
    }

    @SuppressWarnings("unchecked")
    static List<? extends AnnotationValue> getList(
            Map<String, AnnotationValue> valueMap,
            String name) {
        assert valueMap != null;
        assert name != null;
        AnnotationValue value = valueMap.get(name);
        if (value == null) {
            return null;
        }
        Object content = value.getValue();
        if ((content instanceof List<?>) == false) {
            return null;
        }
        return (List<? extends AnnotationValue>) content;
    }

    static TypeMirror getReduceTermType(AnnotationMirror annotation) {
        assert annotation != null;
        Map<String, AnnotationValue> values = getValues(annotation);
        return getValue(TypeMirror.class, values, "source"); //$NON-NLS-1$
    }

    static AnnotationMirror getReduceTermKey(AnnotationMirror annotation) {
        assert annotation != null;
        Map<String, AnnotationValue> values = getValues(annotation);
        return getValue(AnnotationMirror.class, values, "shuffle"); //$NON-NLS-1$
    }

    static Map<String, AnnotationValue> getValues(
            AnnotationMirror annotation) {
        assert annotation != null;
        Map<String, AnnotationValue> results = new HashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : annotation.getElementValues().entrySet()) {
            ExecutableElement key = entry.getKey();
            AnnotationValue value = entry.getValue();
            results.put(key.getSimpleName().toString(), value);
        }
        return results;
    }

    /**
     * Represents a {@link ShuffleKey} with original parameter info.
     * @since 0.7.1
     */
    public static class ShuffleKeySpec {

        final int position;

        final DataModelMirror model;

        final ShuffleKey key;

        final List<PropertyMirror> groupProperties;

        ShuffleKeySpec(int position, DataModelMirror model, ShuffleKey key) {
            this.position = position;
            this.model = model;
            this.key = key;
            this.groupProperties = resolveProperties();
        }

        private List<PropertyMirror> resolveProperties() {
            List<PropertyMirror> results = new ArrayList<>();
            for (String name : key.getGroupProperties()) {
                results.add(model.findProperty(name));
            }
            return results;
        }

        /**
         * Returns the target key.
         * @return the key
         */
        public ShuffleKey getKey() {
            return key;
        }
    }

    /**
     * Represents constraints of the type.
     */
    public class TypeConstraint {

        private final TypeMirror type;

        private final Element element;

        /**
         * Creates a new instance.
         * @param type the target type
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        TypeConstraint(TypeMirror type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            this.type = type;
            this.element = environment.getTypeUtils().asElement(type);
        }

        /**
         * Returns the target type.
         * @return the target type
         */
        public TypeMirror getType() {
            return type;
        }

        /**
         * Returns whether the target type is actual type or not.
         * @return {@code true} if the target type is actual type, otherwise {@code false}
         */
        public boolean exists() {
            return type.getKind() != TypeKind.NONE;
        }

        /**
         * Returns whether the target type is void type or not.
         * @return {@code true} if the target type is void type, otherwise {@code false}
         */
        public boolean isVoid() {
            return type.getKind() == TypeKind.VOID;
        }

        /**
         * Returns whether the target type is a type variable or not.
         * @return {@code true} if the target type is a type variable, otherwise {@code false}
         */
        public boolean isTypeVariable() {
            return type.getKind() == TypeKind.TYPEVAR;
        }

        /**
         * Returns whether the target type is an enum type or not.
         * @return {@code true} if the target type is an enum type, otherwise {@code false}
         */
        public boolean isEnum() {
            if (element == null) {
                return false;
            }
            if (element.getKind() == ElementKind.ENUM && element.getModifiers().contains(Modifier.PUBLIC)) {
                return true;
            }
            return false;
        }

        /**
         * Returns the type element of this type.
         * @return the type element, or {@code null} if this does not have the type element
         */
        public TypeElement getTypeElement() {
            if (element instanceof TypeElement) {
                return (TypeElement) element;
            }
            return null;
        }

        /**
         * Returns the enum constants of this enum type.
         * @return the enum constants
         * @throws IllegalStateException if this does not represents an enum type
         */
        public List<VariableElement> getEnumConstants() {
            if (isEnum() == false) {
                throw new IllegalStateException();
            }
            TypeElement decl = (TypeElement) element;
            List<VariableElement> results = new ArrayList<>();
            for (Element member : decl.getEnclosedElements()) {
                if (isEnumConstant(member)) {
                    results.add((VariableElement) member);
                }
            }
            return results;
        }

        private boolean isEnumConstant(Element member) {
            if (member.getKind() != ElementKind.ENUM_CONSTANT) {
                return false;
            }
            // for Eclipse APT bug
            Set<Modifier> modifiers = member.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC) == false
                    || modifiers.contains(Modifier.STATIC) == false
                    || modifiers.contains(Modifier.FINAL) == false) {
                return false;
            }
            VariableElement variable = (VariableElement) member;
            TypeMirror constantType = variable.asType();
            if (constantType.getKind() != TypeKind.DECLARED) {
                return false;
            }
            Types types = environment.getTypeUtils();
            return types.isSameType(constantType, type);
        }

        /**
         * Returns whether the target type is an operator class or not.
         * @return {@code true} if the target type is an operator class, otherwise {@code false}
         */
        public boolean isOperator() {
            return environment.getTypeUtils().isSubtype(type, environment.getDeclaredType(Operator.class));
        }

        /**
         * Returns whether the target type is an data model type or not.
         * @return {@code true} if the target type is an data model type, otherwise {@code false}
         */
        public boolean isModel() {
            DataModelMirror model = environment.loadDataModel(type);
            return model != null;
        }

        /**
         * Returns whether the target type is a concrete data model type (not projective) or not.
         * @return {@code true} if the target type is a concrete data model type, otherwise {@code false}
         */
        public boolean isConcreteModel() {
            DataModelMirror model = environment.loadDataModel(type);
            return model != null && model.getKind() == Kind.CONCRETE;
        }

        /**
         * Returns whether the target type is a projective data model type or not.
         * @return {@code true} if the target type is a projective data model type, otherwise {@code false}
         */
        public boolean isProjectiveModel() {
            DataModelMirror model = environment.loadDataModel(type);
            return model != null && model.getKind() == Kind.PARTIAL;
        }

        /**
         * Returns whether the target type is joined data model type with the specified sources or not.
         * @param a the first source type
         * @param b the second source type
         * @return {@code true} if the target type is a joined type with the specified sources, otherwise {@code false}
         */
        public boolean isJoinedModel(TypeMirror a, TypeMirror b) {
            AnnotationMirror annotation = findAnnotation(element, environment.getDeclaredType(Joined.class));
            if (annotation == null) {
                return false;
            }
            Map<String, AnnotationValue> values = getValues(annotation);
            List<? extends AnnotationValue> terms = getList(values, "terms"); //$NON-NLS-1$
            if (terms == null
                    || terms.size() != 2
                    || (terms.get(0).getValue() instanceof AnnotationMirror) == false
                    || (terms.get(1).getValue() instanceof AnnotationMirror) == false) {
                return false;
            }
            AnnotationMirror from = (AnnotationMirror) terms.get(0).getValue();
            AnnotationMirror join = (AnnotationMirror) terms.get(1).getValue();
            TypeMirror fromType = getReduceTermType(from);
            TypeMirror joinType = getReduceTermType(join);
            if (fromType == null || joinType == null) {
                return false;
            }
            if (environment.getTypeUtils().isSameType(a, fromType)) {
                return environment.getTypeUtils().isSameType(b, joinType);
            }
            if (environment.getTypeUtils().isSameType(b, fromType)) {
                return environment.getTypeUtils().isSameType(a, joinType);
            }
            return false;
        }

        /**
         * Returns whether the target type is a joined data model type and the specified type is a left source type,
         * or not.
         * @param target the target source type
         * @return {@code true} if the target type is such the joined data model type, otherwise {@code false}
         */
        public boolean isJoinFrom(TypeMirror target) {
            Precondition.checkMustNotBeNull(target, "target"); //$NON-NLS-1$
            AnnotationMirror annotation = findAnnotation(element, environment.getDeclaredType(Joined.class));
            if (annotation == null) {
                return false;
            }
            Map<String, AnnotationValue> values = getValues(annotation);
            List<? extends AnnotationValue> terms = getList(values, "terms"); //$NON-NLS-1$
            if (terms == null
                    || terms.isEmpty()
                    || (terms.get(0).getValue() instanceof AnnotationMirror) == false) {
                return false;
            }
            AnnotationMirror from = (AnnotationMirror) terms.get(0).getValue();
            TypeMirror fromType = getReduceTermType(from);
            if (fromType == null) {
                return false;
            }
            return typeEqual(fromType, target);
        }

        /**
         * Returns the join key for the specified source type.
         * @param target the source type
         * @return the corresponded source type
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public ShuffleKey getJoinKey(TypeMirror target) {
            Precondition.checkMustNotBeNull(target, "target"); //$NON-NLS-1$
            DataModelMirror model = environment.loadDataModel(target);
            AnnotationMirror annotation = findAnnotation(element, environment.getDeclaredType(Joined.class));
            if (model == null || annotation == null) {
                throw new IllegalArgumentException();
            }
            Map<String, AnnotationValue> values = getValues(annotation);
            List<? extends AnnotationValue> terms = getList(values, "terms"); //$NON-NLS-1$
            if (terms == null) {
                throw new IllegalArgumentException();
            }
            for (AnnotationValue value : terms) {
                if ((value.getValue() instanceof AnnotationMirror) == false) {
                    continue;
                }
                AnnotationMirror term = (AnnotationMirror) value.getValue();
                if (typeEqual(target, getReduceTermType(term))) {
                    AnnotationMirror shuffle = getReduceTermKey(term);
                    ShuffleKey key = toShuffleKey(-1, model, shuffle);
                    return key;
                }
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns whether the target type is a summarized data model type with the specified source type or not.
         * @param target the source type
         * @return {@code true} if the target type is such a summarized data model type, otherwise {@code false}
         */
        public boolean isSummarizedModel(TypeMirror target) {
            Precondition.checkMustNotBeNull(target, "target"); //$NON-NLS-1$
            AnnotationMirror annotation = findAnnotation(element, environment.getDeclaredType(Summarized.class));
            if (annotation == null) {
                return false;
            }
            Map<String, AnnotationValue> values = getValues(annotation);
            AnnotationMirror from = getValue(AnnotationMirror.class, values, "term"); //$NON-NLS-1$
            if (from == null) {
                return false;
            }
            TypeMirror fromType = getReduceTermType(from);
            if (fromType == null) {
                return false;
            }
            return typeEqual(fromType, target);
        }

        /**
         * Returns grouping key of this summarized type.
         * @return the grouping key
         */
        public ShuffleKey getSummarizeKey() {
            AnnotationMirror annotation = findAnnotation(element, environment.getDeclaredType(Summarized.class));
            if (annotation == null) {
                throw new IllegalArgumentException();
            }
            Map<String, AnnotationValue> values = getValues(annotation);
            AnnotationMirror reduce = getValue(AnnotationMirror.class, values, "term"); //$NON-NLS-1$
            if (reduce == null) {
                throw new IllegalArgumentException();
            }
            TypeMirror shuffleType = getReduceTermType(reduce);
            DataModelMirror model = environment.loadDataModel(shuffleType);
            AnnotationMirror shuffleKey = getReduceTermKey(reduce);
            if (model != null && shuffleKey != null) {
                return toShuffleKey(-1, model, shuffleKey);
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns whether the target type is boolean type or not.
         * @return {@code true} if the target type is boolean type, otherwise {@code false}
         */
        public boolean isBoolean() {
            return type.getKind() == TypeKind.BOOLEAN;
        }

        /**
         * Returns whether the target type is a string type or not.
         * @return {@code true} if the target type is a string type, otherwise {@code false}
         */
        public boolean isString() {
            return typeEqual(type, environment.getDeclaredType(String.class));
        }

        /**
         * Returns whether the target type is a subtype of {@link Iterable} or not.
         * @return {@code true} if the target type is a list type, otherwise {@code false}
         * @see #getTypeArgument()
         */
        public boolean isIterable() {
            return isList() || typeDeclEqual(type, environment.getDeclaredType(Iterable.class));
        }

        /**
         * Returns whether the target type is a list type or not.
         * @return {@code true} if the target type is a list type, otherwise {@code false}
         * @see #getTypeArgument()
         */
        public boolean isList() {
            return typeDeclEqual(type, environment.getDeclaredType(List.class));
        }

        /**
         * Returns whether the target type is a result type or not.
         * @return {@code true} if the target type is a result type, otherwise {@code false}
         * @see #getTypeArgument()
         */
        public boolean isResult() {
            return typeDeclEqual(type, environment.getDeclaredType(Result.class));
        }

        /**
         * Returns whether the target type is a flow input type or not.
         * @return {@code true} if the target type is a flow input type, otherwise {@code false}
         * @see #getTypeArgument()
         */
        public boolean isIn() {
            return typeDeclEqual(type, environment.getDeclaredType(In.class));
        }

        /**
         * Returns whether the target type is a flow output type or not.
         * @return {@code true} if the target type is a flow output type, otherwise {@code false}
         * @see #getTypeArgument()
         */
        public boolean isOut() {
            return typeDeclEqual(type, environment.getDeclaredType(Out.class));
        }

        /**
         * Returns whether the target type is a user parameter type or not.
         * @return {@code true} if the target type is a user parameter type, otherwise {@code false}
         * @see #getTypeArgument()
         */
        public boolean isBasic() {
            if (type.getKind().isPrimitive() || typeEqual(type, environment.getDeclaredType(String.class))) {
                return true;
            }
            return false;
        }

        /**
         * Returns the type constraint object for the first type argument of this.
         * @return the type constraint object for the first type argument of this
         */
        public TypeConstraint getTypeArgument() {
            if (type.getKind() != TypeKind.DECLARED) {
                return new TypeConstraint(environment.getTypeUtils().getNoType(TypeKind.NONE));
            }
            DeclaredType declared = (DeclaredType) type;
            List<? extends TypeMirror> arguments = declared.getTypeArguments();
            if (arguments.isEmpty()) {
                return new TypeConstraint(environment.getTypeUtils().getNoType(TypeKind.NONE));
            }
            return new TypeConstraint(arguments.get(0));
        }
    }
}
