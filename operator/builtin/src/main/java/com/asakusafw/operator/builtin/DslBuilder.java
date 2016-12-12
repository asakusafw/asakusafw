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
package com.asakusafw.operator.builtin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.processing.Messager;
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
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorDriver;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.description.EnumConstantDescription;
import com.asakusafw.operator.description.ObjectDescription;
import com.asakusafw.operator.description.ValueDescription;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.model.KeyMirror;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Document;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.ParameterReference;
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.operator.model.OperatorDescription.Reference.Kind;
import com.asakusafw.operator.util.AnnotationHelper;

/**
 * Helper for built-in operators.
 * @since 0.9.0
 * @version 0.9.1
 */
final class DslBuilder {

    private static final ValueDescription[] EMPTY_ATTRS = new ValueDescription[0];

    static final EnumConstantDescription CONSTANT_SHUFFLE = new EnumConstantDescription(
            new ClassDescription("com.asakusafw.vocabulary.flow.graph.FlowBoundary"), //$NON-NLS-1$
            "SHUFFLE"); //$NON-NLS-1$

    static final ClassDescription TYPE_ITERABLE = Descriptions.classOf(Iterable.class);

    static final ClassDescription TYPE_LIST = Descriptions.classOf(List.class);

    static final ClassDescription TYPE_ENUM = Descriptions.classOf(Enum.class);

    static final ClassDescription TYPE_STRING = Descriptions.classOf(String.class);

    static final ClassDescription TYPE_VOLATILE =
            new ClassDescription("com.asakusafw.vocabulary.operator.Volatile"); //$NON-NLS-1$

    static final ClassDescription TYPE_STICKY =
            new ClassDescription("com.asakusafw.vocabulary.operator.Sticky"); //$NON-NLS-1$

    static final ClassDescription TYPE_OBSERVATION_COUNT =
            new ClassDescription("com.asakusafw.vocabulary.flow.graph.ObservationCount"); //$NON-NLS-1$

    static final ClassDescription TYPE_VIEW_INFO =
            new ClassDescription("com.asakusafw.vocabulary.attribute.ViewInfo"); //$NON-NLS-1$

    static final String NAME_FLAT_VIEW_INFO_FACTORY = "flat"; //$NON-NLS-1$

    static final String NAME_GROUP_VIEW_INFO_FACTORY = "groupOf"; //$NON-NLS-1$

    private final List<Node> parameters = new ArrayList<>();

    private final List<Node> inputs = new ArrayList<>();

    private final List<Node> arguments = new ArrayList<>();

    private final List<Node> outputs = new ArrayList<>();

    private final List<KeyRef> mainKeys = new ArrayList<>();

    private ExecutableElement support;

    private final List<ValueDescription> attributes = new ArrayList<>();

    final CompileEnvironment environment;

    final ExecutableElement method;

    final AtomicBoolean errorSink = new AtomicBoolean();

    private final AnnotationRef annotationRef;

    private final ElementRef methodRef;

    private final List<ElementRef> parameterRefs;

    private final ElementRef resultRef;

    private final ElementRef unknownRef;

    DslBuilder(OperatorDriver.Context context) {
        Objects.requireNonNull(context, "context must not be null"); //$NON-NLS-1$
        this.environment = context.getEnvironment();
        this.method = context.getMethod();
        this.annotationRef = new AnnotationRef(context.getMethod(), context.getAnnotation());
        this.unknownRef = new MissingElementRef(context.getMethod());
        this.methodRef = new GeneralElementRef(context.getMethod(), Reference.method());
        this.parameterRefs = new ArrayList<>();
        int paramIndex = 0;
        for (VariableElement p : context.getMethod().getParameters()) {
            parameterRefs.add(new GeneralElementRef(p, Reference.parameter(paramIndex++)));
        }
        this.resultRef = new ResultElementRef(context.getMethod());
    }

    public void addInput(Document document, String name, TypeMirror type, Reference reference) {
        addInput(document, name, type, null, reference, EMPTY_ATTRS);
    }

    public void addInput(Document document, String name, TypeMirror type, KeyRef key, Reference reference) {
        addInput(document, name, type, key, reference, EMPTY_ATTRS);
    }

    public Node addOutput(Document document, String name, TypeMirror type, Reference reference) {
        return addOutput(document, name, type, reference, EMPTY_ATTRS);
    }

    public void addInput(
            Document document, String name, TypeMirror type,
            Reference reference,
            ValueDescription... attrs) {
        addInput(document, name, type, null, reference, attrs);
    }

    public void addInput(
            Document document, String name, TypeMirror type,
            KeyRef key, Reference reference,
            ValueDescription... attrs) {
        Node node = new Node(Node.Kind.INPUT, name, document, type, reference);
        inputs.add(node);
        if (key != null) {
            node.withKey(key.getModel());
            mainKeys.add(key);
        }
        for (ValueDescription attr : attrs) {
            node.withAttribute(attr);
        }
        parameters.add(node);
    }

    public Node addOutput(
            Document document, String name, TypeMirror type,
            Reference reference,
            ValueDescription... attrs) {
        Node node = new Node(Node.Kind.OUTPUT, name, document, type, reference);
        for (ValueDescription attr : attrs) {
            node.withAttribute(attr);
        }
        outputs.add(node);
        return node;
    }

    public void addArgument(Document document, String name, TypeMirror type, Reference reference) {
        Node node = new Node(Node.Kind.DATA, name, document, type, reference);
        arguments.add(node);
        parameters.add(node);
    }

    public void requireShuffle() {
        addAttribute(CONSTANT_SHUFFLE);
    }

    public void setSupport(ExecutableElement newValue) {
        this.support = newValue;
    }

    public void addAttribute(ValueDescription attribute) {
        attributes.add(attribute);
    }

    public CompileEnvironment getEnvironment() {
        return environment;
    }

    public ExecutableElement getMethod() {
        return method;
    }

    public List<Node> getInputs() {
        return inputs;
    }

    public List<Node> getOutputs() {
        return outputs;
    }

    public List<Node> getParameters() {
        return parameters;
    }

    public boolean sawError() {
        return errorSink.get();
    }

    public OperatorDescription toDescription() {
        if (sawError()) {
            return null;
        }
        if (inputs.isEmpty()) {
            methodRef.error(Messages.getString("DslBuilder.errorInputMissing")); //$NON-NLS-1$
        }
        if (outputs.isEmpty()) {
            methodRef.error(Messages.getString("DslBuilder.errorOutputMissing")); //$NON-NLS-1$
        }
        validateMainKeys();
        if (environment.isStrictOperatorParameterOrder()) {
            validateParameterOrder();
        }
        if (sawError()) {
            return null;
        }
        List<ValueDescription> attrs = new ArrayList<>();
        attrs.addAll(attributes);
        attrs.add(computeObservationCount());
        return new OperatorDescription(Document.reference(Reference.method()), parameters, outputs, attrs)
            .withSupport(support);
    }

    private void validateMainKeys() {
        if (mainKeys.size() <= 1) {
            return;
        }
        KeyRef first = mainKeys.get(0);
        for (int i = 1, n = mainKeys.size(); i < n; i++) {
            KeyRef target = mainKeys.get(i);
            validateMainKey(first, target);
        }
    }

    private void validateMainKey(KeyRef first, KeyRef target) {
        List<KeyMirror.Group> as = first.getModel().getGroup();
        List<KeyMirror.Group> bs = target.getModel().getGroup();
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        for (int i = 0, n = Math.min(as.size(), bs.size()); i < n; i++) {
            KeyMirror.Group a = as.get(i);
            KeyMirror.Group b = bs.get(i);
            if (types.isSameType(a.getProperty().getType(), b.getProperty().getType()) == false) {
                target.error(b.getSource(), MessageFormat.format(
                        Messages.getString("DslBuilder.errorKeyAnnotationInconsistentGroupPropertyType"), //$NON-NLS-1$
                        first.getOwner().getSimpleName(),
                        a.getProperty().getName(),
                        target.getOwner().getSimpleName(),
                        b.getProperty().getName()));
            }
        }
        if (as.size() < bs.size()) {
            for (int i = as.size(), n = bs.size(); i < n; i++) {
                KeyMirror.Group b = bs.get(i);
                first.error(MessageFormat.format(
                        Messages.getString("DslBuilder.errorKeyAnnotationInconsistentGroupProperty"), //$NON-NLS-1$
                        target.getOwner().getSimpleName(),
                        b.getProperty().getName(),
                        first.getOwner().getSimpleName()));
            }
        } else if (as.size() > bs.size()) {
            for (int i = bs.size(), n = as.size(); i < n; i++) {
                KeyMirror.Group a = as.get(i);
                target.error(MessageFormat.format(
                        Messages.getString("DslBuilder.errorKeyAnnotationInconsistentGroupProperty"), //$NON-NLS-1$
                        first.getOwner().getSimpleName(),
                        a.getProperty().getName(),
                        target.getOwner().getSimpleName()));
            }
        }
    }

    private void validateParameterOrder() {
        Predicate<Node> side = n -> n.getAttributes().stream()
                .filter(v -> v instanceof ObjectDescription)
                .map(v -> (ObjectDescription) v)
                .anyMatch(o -> o.getValueType().equals(TYPE_VIEW_INFO));
        BitSet inMask = toMask(inputs);
        BitSet sideMask = toMask(inputs, side);
        BitSet outMask = toMask(outputs);
        BitSet argMask = toMask(arguments);

        outMask.andNot(inMask); // don't consider input&output parameters
        if (sideMask.isEmpty() == false) {
            BitSet mainMask = (BitSet) inMask.clone();
            mainMask.andNot(sideMask);
            forEach(mainMask, sideMask.nextSetBit(0), i -> {
                parameterRefs.get(i).error(Messages.getString("DslBuilder.errorInputAfterSide")); //$NON-NLS-1$
            });
        }
        if (outMask.isEmpty() == false) {
            forEach(inMask, outMask.nextSetBit(0), i -> {
                parameterRefs.get(i).error(Messages.getString("DslBuilder.errorInputAfterOutput")); //$NON-NLS-1$
            });
        }
        if (argMask.isEmpty() == false) {
            forEach(inMask, argMask.nextSetBit(0), i -> {
                parameterRefs.get(i).error(Messages.getString("DslBuilder.errorInputAfterArgument")); //$NON-NLS-1$
            });
            forEach(outMask, argMask.nextSetBit(0), i -> {
                parameterRefs.get(i).error(Messages.getString("DslBuilder.errorOutputAfterArgument")); //$NON-NLS-1$
            });
        }
    }

    private static BitSet toMask(List<Node> nodes, Predicate<Node> predicate) {
        BitSet results = new BitSet();
        nodes.stream()
            .filter(predicate)
            .map(n -> n.getReference())
            .filter(r -> r.getKind() == Kind.PARAMETER)
            .map(ParameterReference.class::cast)
            .forEach(r -> results.set(r.getLocation()));
        return results;
    }

    private static BitSet toMask(List<Node> nodes) {
        return toMask(nodes, n -> true);
    }

    private static void forEach(BitSet bits, int start, IntConsumer body) {
        for (int i = bits.nextSetBit(start); i >= 0; i = bits.nextSetBit(i + 1)) {
            body.accept(i);
        }
    }

    private EnumConstantDescription computeObservationCount() {
        boolean isVolatile = false;
        boolean isSticky = false;
        DeclaredType volatileType = environment.findDeclaredType(TYPE_VOLATILE);
        DeclaredType stickyType = environment.findDeclaredType(TYPE_STICKY);
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            DeclaredType annotationType = mirror.getAnnotationType();
            if (isVolatile == false && types.isSameType(annotationType, volatileType)) {
                isVolatile = true;
            }
            if (isSticky == false && types.isSameType(annotationType, stickyType)) {
                isSticky = true;
            }
        }
        String name;
        if (isVolatile && isSticky) {
            name = "EXACTLY_ONCE"; //$NON-NLS-1$
        } else if (isVolatile) {
            name = "AT_MOST_ONCE"; //$NON-NLS-1$
        } else if (isSticky) {
            name = "AT_LEAST_ONCE"; //$NON-NLS-1$
        } else {
            name = "DONT_CARE"; //$NON-NLS-1$
        }
        return new EnumConstantDescription(TYPE_OBSERVATION_COUNT, name);
    }

    public ElementRef method() {
        return methodRef;
    }

    public ElementRef parameter(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (index >= parameterRefs.size()) {
            return unknownRef;
        }
        return parameterRefs.get(index);
    }

    public ElementRef result() {
        return resultRef;
    }

    public List<ElementRef> parameters() {
        return parameters(0, parameterRefs.size());
    }

    public List<ElementRef> parametersFrom(int from) {
        return parameters(from, parameterRefs.size());
    }

    public List<ElementRef> parameters(int from, int to) {
        if (0 <= from && from < to && to <= parameterRefs.size()) {
            return parameterRefs.subList(from, to);
        } else {
            return Collections.emptyList();
        }
    }

    public AnnotationRef annotation() {
        return annotationRef;
    }

    public void consumeExtraParameter(ElementRef parameter) {
        TypeRef type = parameter.type();
        if (type.isBasic()) {
            addArgument(parameter.document(), parameter.name(), type.mirror(), parameter.reference());
        } else if (type.isGroupView()) {
            TypeRef arg = type.arg(0);
            if (arg.isDataModel()) {
                KeyRef key = parameter.resolveKey(arg);
                ValueDescription info = key.toTableInfo();
                addInput(parameter.document(), parameter.name(), arg.mirror(), null, parameter.reference(), info);
            } else {
                parameter.error(Messages.getString("DslBuilder.errorGroupViewNotDataModelType")); //$NON-NLS-1$
            }
        } else if (type.isFlatView()) {
            TypeRef arg = type.arg(0);
            if (arg.isDataModel()) {
                // FIXME warn if @Key is declared
                ValueDescription info = ObjectDescription.of(TYPE_VIEW_INFO, NAME_FLAT_VIEW_INFO_FACTORY);
                addInput(parameter.document(), parameter.name(), arg.mirror(), null, parameter.reference(), info);
            } else {
                parameter.error(Messages.getString("DslBuilder.errorFlatViewNotDataModelType")); //$NON-NLS-1$
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isGeneric() {
        return method.getTypeParameters().isEmpty() == false;
    }

    interface ElementRef {

        boolean exists();

        Element get();

        TypeRef type();

        Document document();

        Reference reference();

        String name();

        default Set<Modifier> modifiers() {
            return Collections.emptySet();
        }

        default AnnotationRef annotation(ClassDescription type) {
            return null;
        }

        default KeyRef resolveKey(TypeRef modelType) {
            throw new IllegalStateException();
        }

        default KeyRef resolveKey(TypeRef modelType, AnnotationMirror annotation) {
            throw new IllegalStateException();
        }

        void error(String string);
    }

    private class MissingElementRef implements ElementRef {

        private final Element owner;

        MissingElementRef(Element owner) {
            assert owner != null;
            this.owner = owner;
        }

        @Override
        public Element get() {
            return owner;
        }

        @Override
        public boolean exists() {
            return false;
        }

        @Override
        public TypeRef type() {
            return new TypeRef();
        }

        @Override
        public Document document() {
            return Document.text("MISSING"); //$NON-NLS-1$
        }

        @Override
        public Reference reference() {
            return Reference.special("UNKWON"); //$NON-NLS-1$
        }

        @Override
        public String name() {
            return "MISSING"; //$NON-NLS-1$
        }

        @Override
        public void error(String message) {
            errorSink.set(true);
            Messager messager = environment.getProcessingEnvironment().getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, message, owner);
        }
    }

    private class GeneralElementRef implements ElementRef {

        final Element element;

        private final Document document;

        private final Reference reference;

        GeneralElementRef(Element element, Reference reference) {
            this(element, Document.reference(reference), reference);
        }

        GeneralElementRef(Element element, Document document, Reference reference) {
            assert element != null;
            assert document != null;
            assert reference != null;
            this.element = element;
            this.document = document;
            this.reference = reference;
        }

        @Override
        public Element get() {
            return element;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public TypeRef type() {
            return new TypeRef(element.asType());
        }

        @Override
        public Document document() {
            return document;
        }

        @Override
        public Reference reference() {
            return reference;
        }

        @Override
        public String name() {
            return element.getSimpleName().toString();
        }

        @Override
        public Set<Modifier> modifiers() {
            return element.getModifiers();
        }

        @Override
        public AnnotationRef annotation(ClassDescription type) {
            TypeElement annotationType = environment.findTypeElement(type);
            if (annotationType == null) {
                return null;
            }
            AnnotationMirror annotation = AnnotationHelper.findAnnotation(environment, annotationType, element);
            if (annotation == null) {
                return null;
            }
            return new AnnotationRef(element, annotation);
        }

        @Override
        public KeyRef resolveKey(TypeRef dataModelType) {
            TypeElement annotationType = environment.findTypeElement(Constants.TYPE_KEY);
            if (annotationType == null) {
                errorSink.set(true);
                return null;
            }
            AnnotationMirror annotation = AnnotationHelper.findAnnotation(environment, annotationType, element);
            if (annotation == null) {
                error(Messages.getString("DslBuilder.errorElementMissingKeyAnnotation")); //$NON-NLS-1$
                return null;
            }
            return resolveKey(dataModelType, annotation);
        }

        @Override
        public KeyRef resolveKey(TypeRef modelType, AnnotationMirror annotation) {
            DataModelMirror dataModel = environment.findDataModel(modelType.mirror());
            if (dataModel == null) {
                errorSink.set(true);
                return null;
            }
            KeyMirror model = KeyMirror.parse(environment, annotation, element, dataModel);
            if (model == null) {
                errorSink.set(true);
                return null;
            }
            return new KeyRef(element, model);
        }

        @Override
        public void error(String message) {
            errorSink.set(true);
            Messager messager = environment.getProcessingEnvironment().getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
        }
    }

    class ResultElementRef implements ElementRef {

        private final ExecutableElement element;

        ResultElementRef(ExecutableElement element) {
            assert element != null;
            this.element = element;
        }

        @Override
        public Element get() {
            return element;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public TypeRef type() {
            return new TypeRef(element.getReturnType());
        }

        @Override
        public Document document() {
            return Document.reference(reference());
        }

        @Override
        public Reference reference() {
            return Reference.returns();
        }

        @Override
        public String name() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void error(String message) {
            errorSink.set(true);
            Messager messager = environment.getProcessingEnvironment().getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
        }
    }

    class TypeRef {

        private final TypeMirror mirror;

        TypeRef() {
            this(environment.getProcessingEnvironment().getTypeUtils().getNoType(TypeKind.NONE));
        }

        TypeRef(TypeMirror mirror) {
            assert mirror != null;
            this.mirror = mirror;
        }

        private Types types() {
            return environment.getProcessingEnvironment().getTypeUtils();
        }

        public boolean exists() {
            return mirror.getKind() != TypeKind.NONE;
        }

        public boolean isVoid() {
            return mirror.getKind() == TypeKind.VOID;
        }

        public boolean isExtra() {
            return isPrimitive() || isString() || isViewLike();
        }

        public boolean isViewLike() {
            return isFlatView() || isGroupView();
        }

        public boolean isFlatView() {
            return isErasureEqualTo(environment.findDeclaredType(Constants.TYPE_VIEW));
        }

        public boolean isGroupView() {
            return isErasureEqualTo(environment.findDeclaredType(Constants.TYPE_GROUP_VIEW));
        }

        public boolean isBasic() {
            return isPrimitive() || isString();
        }

        public boolean isBoolean() {
            return mirror.getKind() == TypeKind.BOOLEAN;
        }

        public boolean isPrimitive() {
            return mirror.getKind().isPrimitive();
        }

        public boolean isString() {
            return types().isSameType(mirror, environment.findDeclaredType(TYPE_STRING));
        }

        public boolean isEnum() {
            return types().isSubtype(mirror, environment.findDeclaredType(TYPE_ENUM));
        }

        public boolean isIterable() {
            return isErasureEqualTo(environment.findDeclaredType(TYPE_ITERABLE));
        }

        public boolean isList() {
            return isErasureEqualTo(environment.findDeclaredType(TYPE_LIST));
        }

        public boolean isResult() {
            return isErasureEqualTo(environment.findDeclaredType(Constants.TYPE_RESULT));
        }

        public TypeRef arg(int index) {
            if (mirror.getKind() == TypeKind.DECLARED) {
                List<? extends TypeMirror> typeArguments = ((DeclaredType) mirror).getTypeArguments();
                if (0 <= index && index < typeArguments.size()) {
                    return new TypeRef(typeArguments.get(index));
                }
            }
            return new TypeRef();
        }

        public boolean isDataModel() {
            DataModelMirror dataModel = environment.findDataModel(mirror);
            return dataModel != null;
        }

        public boolean isEqualTo(TypeRef other) {
            return types().isSameType(mirror, other.mirror);
        }

        private boolean isErasureEqualTo(TypeMirror other) {
            return types().isSameType(environment.getErasure(mirror), environment.getErasure(other));

        }

        public TypeMirror mirror() {
            return mirror;
        }

        public DataModelMirror dataModel() {
            DataModelMirror dataModel = environment.findDataModel(mirror);
            if (isDataModel() == false) {
                throw new IllegalStateException();
            }
            return dataModel;
        }

        public List<ElementRef> enumConstants() {
            if (isEnum() == false) {
                throw new IllegalStateException();
            }
            TypeElement type = (TypeElement) ((DeclaredType) mirror).asElement();
            List<ElementRef> results = new ArrayList<>();
            for (VariableElement var : ElementFilter.fieldsIn(type.getEnclosedElements())) {
                if (var.getKind() == ElementKind.ENUM_CONSTANT) {
                    Document document = Document.external(var);
                    Reference reference = Reference.special(var.getSimpleName().toString());
                    results.add(new GeneralElementRef(var, document, reference));
                }
            }
            return results;
        }

        public AnnotationRef annotation(ClassDescription annotationType) {
            if (mirror.getKind() != TypeKind.DECLARED) {
                return null;
            }
            TypeElement declaredType = (TypeElement) ((DeclaredType) mirror).asElement();
            if (declaredType == null) {
                return null;
            }
            TypeElement type = environment.findTypeElement(annotationType);
            if (type == null) {
                return null;
            }
            AnnotationMirror annotation = AnnotationHelper.findAnnotation(environment, type, declaredType);
            if (annotation == null) {
                return null;
            }
            return new AnnotationRef(declaredType, annotation);
        }

        @Override
        public String toString() {
            return mirror.toString();
        }
    }

    class AnnotationRef {

        private final AnnotationMirror annotation;

        private Map<String, AnnotationValue> values;

        private final Element holder;

        AnnotationRef(Element holder, AnnotationMirror annotation) {
            assert holder != null;
            assert annotation != null;
            this.holder = holder;
            this.annotation = annotation;
        }

        public AnnotationMirror get() {
            return annotation;
        }

        public AnnotationValue value(String name) {
            return getHolder(name);
        }

        public EnumConstantDescription constant(String name) {
            AnnotationValue valueHolder = getHolder(name);
            Object value = valueHolder.getValue();
            if (value instanceof VariableElement) {
                VariableElement var = (VariableElement) value;
                TypeElement type = (TypeElement) var.getEnclosingElement();
                return new EnumConstantDescription(
                        new ClassDescription(type.getQualifiedName().toString()),
                        var.getSimpleName().toString());
            }
            return null;
        }

        public String string(String name) {
            AnnotationValue valueHolder = getHolder(name);
            Object value = valueHolder.getValue();
            if (value instanceof String) {
                return (String) value;
            }
            return null;
        }

        public TypeRef type(String name) {
            AnnotationValue valueHolder = getHolder(name);
            Object value = valueHolder.getValue();
            if (value instanceof TypeMirror) {
                return new TypeRef((TypeMirror) value);
            }
            return null;
        }

        public AnnotationRef annotation(String name) {
            AnnotationValue valueHolder = getHolder(name);
            Object value = valueHolder.getValue();
            if (value instanceof AnnotationMirror) {
                return new AnnotationRef(holder, (AnnotationMirror) value);
            }
            return null;
        }

        public List<AnnotationRef> annotations(String name) {
            AnnotationValue valueHolder = getHolder(name);
            List<AnnotationValue> valueHolderList = AnnotationHelper.toValueList(environment, valueHolder);
            List<AnnotationMirror> annotations =
                    AnnotationHelper.extractList(environment, AnnotationMirror.class, valueHolderList);
            List<AnnotationRef> results = new ArrayList<>();
            for (AnnotationMirror component : annotations) {
                results.add(new AnnotationRef(method, component));
            }
            return results;
        }

        private AnnotationValue getHolder(String name) {
            AnnotationValue valueHolder = values().get(name);
            if (valueHolder == null) {
                throw new IllegalArgumentException(name);
            }
            return valueHolder;
        }

        private synchronized Map<String, AnnotationValue> values() {
            if (values == null) {
                values = AnnotationHelper.getValues(environment, annotation);
            }
            return values;
        }

        public void error(String message) {
            errorSink.set(true);
            Messager messager = environment.getProcessingEnvironment().getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, message, holder, annotation);
        }

        public void error(String elementName, String message) {
            AnnotationValue value = AnnotationHelper.getValue(environment, annotation, elementName);
            if (value == null) {
                error(message);
            } else {
                errorSink.set(true);
                Messager messager = environment.getProcessingEnvironment().getMessager();
                messager.printMessage(Diagnostic.Kind.ERROR, message, holder, annotation, value);
            }
        }
    }

    class KeyRef {

        private final Element owner;

        private final KeyMirror model;

        KeyRef(Element owner, KeyMirror model) {
            this.owner = owner;
            this.model = model;
        }

        public Element getOwner() {
            return owner;
        }

        public KeyMirror getModel() {
            return model;
        }

        public void error(String message) {
            errorSink.set(true);
            Messager messager = environment.getProcessingEnvironment().getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, message, owner, model.getSource());
        }

        public void error(AnnotationValue value, String message) {
            errorSink.set(true);
            Messager messager = environment.getProcessingEnvironment().getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, message, owner, model.getSource(), value);
        }

        public ValueDescription toTableInfo() {
            ObjectDescription info = ObjectDescription.of(TYPE_VIEW_INFO, NAME_GROUP_VIEW_INFO_FACTORY, terms());
            return info;
        }

        public List<ValueDescription> terms() {
            return model.toTerms().stream()
                    .sequential()
                    .map(Descriptions::valueOf)
                    .collect(Collectors.toList());
        }
    }
}
