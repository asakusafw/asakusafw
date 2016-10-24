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
package com.asakusafw.operator.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.description.AnnotationDescription;
import com.asakusafw.operator.description.ArrayDescription;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.description.EnumConstantDescription;
import com.asakusafw.operator.description.TypeDescription;
import com.asakusafw.operator.description.ValueDescription;
import com.asakusafw.operator.model.ExternMirror;
import com.asakusafw.operator.model.KeyMirror;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.ParameterReference;
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.operator.model.OperatorDescription.Reference.Kind;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Common helper methods about elements.
 */
public final class ElementHelper {

    static final ClassDescription TYPE_TYPE = ClassDescription.of(java.lang.reflect.Type.class);

    static final ClassDescription TYPE_CLASS = ClassDescription.of(Class.class);

    static final ClassDescription TYPE_FACTORY = classOf("operator.OperatorFactory"); //$NON-NLS-1$

    static final ClassDescription TYPE_INFO = classOf("operator.OperatorInfo"); //$NON-NLS-1$

    static final ClassDescription TYPE_KEY = classOf("operator.KeyInfo"); //$NON-NLS-1$

    static final ClassDescription TYPE_HELPER = classOf("flow.graph.OperatorHelper"); //$NON-NLS-1$

    static final ClassDescription TYPE_INFO_INPUT = classOf(TYPE_INFO, "Input"); //$NON-NLS-1$

    static final ClassDescription TYPE_INFO_OUTPUT = classOf(TYPE_INFO, "Output"); //$NON-NLS-1$

    static final ClassDescription TYPE_INFO_PARAMETER = classOf(TYPE_INFO, "Parameter"); //$NON-NLS-1$

    static final ClassDescription TYPE_KEY_GROUP = classOf(TYPE_KEY, "Group"); //$NON-NLS-1$

    static final ClassDescription TYPE_KEY_ORDER = classOf(TYPE_KEY, "Order"); //$NON-NLS-1$

    static final ClassDescription TYPE_KEY_DIRECTION = classOf(TYPE_KEY, "Direction"); //$NON-NLS-1$

    static final ClassDescription TYPE_EDITOR = classOf("flow.builder.FlowElementEditor"); //$NON-NLS-1$

    static final ClassDescription TYPE_ATTRIBUTES = classOf("flow.builder.Attributes"); //$NON-NLS-1$

    private static ClassDescription classOf(String name) {
        return new ClassDescription("com.asakusafw.vocabulary." + name); //$NON-NLS-1$
    }

    private static ClassDescription classOf(ClassDescription owner, String name) {
        return new ClassDescription(owner.getBinaryName() + '$' + name);
    }

    /**
     * Validates {@link OperatorDescription} and raises errors when it is not valid.
     * @param environment current environment
     * @param element target element
     * @param description target description
     * @return {@code true} if is valid, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static boolean validate(
            CompileEnvironment environment,
            ExecutableElement element,
            OperatorDescription description) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(element, "element must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(description, "description must not be null"); //$NON-NLS-1$
        boolean valid = true;
        valid &= validateParameterNames(environment, element, description);
        valid &= validateOutputNames(environment, element, description);
        valid &= validateOutputType(environment, element, description);
        return valid;
    }

    private static boolean validateParameterNames(
            CompileEnvironment environment,
            ExecutableElement element,
            OperatorDescription description) {
        assert environment != null;
        assert element != null;
        assert description != null;
        boolean valid = true;
        Map<String, Node> names = new HashMap<>();
        for (Node node : description.getParameters()) {
            if (names.containsKey(node.getName())) {
                Node other = names.get(node.getName());
                environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        MessageFormat.format(
                                Messages.getString("ElementHelper.errorInputNameConflict"), //$NON-NLS-1$
                                node.getName(),
                                target(element, other.getReference())),
                        target(element, node.getReference()));
                valid = false;
            } else {
                names.put(node.getName(), node);
            }
        }
        return valid;
    }

    private static boolean validateOutputNames(
            CompileEnvironment environment,
            ExecutableElement element,
            OperatorDescription description) {
        assert environment != null;
        assert element != null;
        assert description != null;
        boolean valid = true;
        Map<String, Node> names = new HashMap<>();
        for (Node node : description.getOutputs()) {
            if (names.containsKey(node.getName())) {
                Node other = names.get(node.getName());
                environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        MessageFormat.format(
                                Messages.getString("ElementHelper.errorOutputNameConflict"), //$NON-NLS-1$
                                target(element, other.getReference())),
                        target(element, node.getReference()));
                valid = false;
            } else {
                names.put(node.getName(), node);
            }
        }
        return valid;
    }

    private static boolean validateOutputType(
            CompileEnvironment environment,
            ExecutableElement element,
            OperatorDescription description) {
        assert environment != null;
        assert element != null;
        assert description != null;
        boolean valid = true;
        Set<TypeVariable> vars = collectInferSources(environment, description);
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        for (Node output : description.getOutputs()) {
            if (output.getType().getKind() == TypeKind.TYPEVAR
                    && vars.stream().noneMatch(var -> types.isSameType(var, output.getType()))) {
                environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                        MessageFormat.format(
                                Messages.getString("ElementHelper.errorOutputTypeNotInferable"), //$NON-NLS-1$
                                ((TypeVariable) output.getType()).asElement().getSimpleName()),
                        target(element, output.getReference()));
                valid = false;
            }
        }
        return valid;
    }

    private static Set<TypeVariable> collectInferSources(
            CompileEnvironment environment,
            OperatorDescription description) {
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        Set<TypeVariable> vars = new HashSet<>();
        description.getInputs().stream()
                .map(Node::getType)
                .filter(type -> type.getKind() == TypeKind.TYPEVAR)
                .map(TypeVariable.class::cast)
                .forEach(vars::add);
        description.getArguments().stream() // we only use Class<T> for inferring output types
                .map(Node::getType)
                .filter(type -> type.getKind() == TypeKind.DECLARED)
                .map(DeclaredType.class::cast)
                .filter(type -> types.isSameType(
                        environment.getErasure(type),
                        environment.findDeclaredType(TYPE_CLASS)))
                .flatMap(type -> type.getTypeArguments().stream())
                .filter(type -> type.getKind() == TypeKind.TYPEVAR)
                .map(TypeVariable.class::cast)
                .forEach(vars::add);
        return vars;
    }

    private static Element target(ExecutableElement element, Reference reference) {
        assert element != null;
        assert reference != null;
        if (reference.getKind() == Reference.Kind.PARAMETER) {
            int index = ((ParameterReference) reference).getLocation();
            if (index < element.getParameters().size()) {
                return element.getParameters().get(index);
            }
        }
        return element;
    }

    /**
     * Creates type parameters about operator element.
     * @param environment current environment
     * @param typeParameters target type parameter elements
     * @param imports import builder
     * @return generated syntax model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<TypeParameterDeclaration> toTypeParameters(
            CompileEnvironment environment,
            List<? extends TypeParameterElement> typeParameters,
            ImportBuilder imports) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(typeParameters, "typeParameters must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(imports, "imports must not be null"); //$NON-NLS-1$
        ModelFactory factory = Models.getModelFactory();
        Jsr269 converter = new Jsr269(factory);
        List<TypeParameterDeclaration> results = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeParameters) {
            List<Type> bounds = new ArrayList<>();
            for (TypeMirror type : typeParameter.getBounds()) {
                bounds.add(imports.resolve(converter.convert(type)));
            }
            results.add(factory.newTypeParameterDeclaration(
                    factory.newSimpleName(typeParameter.getSimpleName().toString()),
                    bounds));
        }
        return results;
    }

    /**
     * Creates a parameterized about operator element.
     * @param environment current environment
     * @param typeParameters target type parameter elements
     * @param rawNodeType parameterization target
     * @param imports import builder
     * @return generated syntax model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Type toParameterizedType(
            CompileEnvironment environment,
            List<? extends TypeParameterElement> typeParameters,
            Type rawNodeType,
            ImportBuilder imports) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(typeParameters, "typeParameters must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(rawNodeType, "rawNodeType must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(imports, "imports must not be null"); //$NON-NLS-1$
        if (typeParameters.isEmpty()) {
            return rawNodeType;
        }
        ModelFactory factory = Models.getModelFactory();
        List<Type> typeArgs = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeParameters) {
            typeArgs.add(factory.newNamedType(factory.newSimpleName(typeParameter.getSimpleName().toString())));
        }
        return factory.newParameterizedType(rawNodeType, typeArgs);
    }

    /**
     * Creates type parameter declarations about operator element.
     * @param environment current environment
     * @param element target operator element
     * @param imports import builder
     * @return generated syntax model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<FormalParameterDeclaration> toParameters(
            CompileEnvironment environment,
            OperatorElement element,
            ImportBuilder imports) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(element, "element must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(imports, "imports must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(element.getDescription(), "element.description must not be null"); //$NON-NLS-1$
        ModelFactory factory = Models.getModelFactory();
        Jsr269 converter = new Jsr269(factory);
        List<FormalParameterDeclaration> results = new ArrayList<>();
        for (Node param : element.getDescription().getParameters()) {
            Type type;
            switch (param.getKind()) {
            case INPUT:
                type = new TypeBuilder(factory, DescriptionHelper.resolve(imports, Constants.TYPE_SOURCE))
                    .parameterize(imports.resolve(converter.convert(param.getType())))
                    .toType();
                break;
            case DATA:
                type = imports.resolve(converter.convert(param.getType()));
                break;
            default:
                throw new AssertionError(param.getKind());
            }
            List<Attribute> attributes = new ArrayList<>();
            if (param.getKey() != null) {
                attributes.add(toParameterKeyInfo(param.getKey(), imports));
            }
            results.add(factory.newFormalParameterDeclaration(
                    attributes,
                    type,
                    false,
                    factory.newSimpleName(param.getName()),
                    0));
        }
        return results;
    }

    private static Annotation toParameterKeyInfo(KeyMirror key, ImportBuilder imports) {
        List<AnnotationDescription> groups = new ArrayList<>();
        for (KeyMirror.Group group : key.getGroup()) {
            String name = group.getProperty().getName();
            groups.add(new AnnotationDescription(
                    TYPE_KEY_GROUP,
                    Collections.singletonMap("expression", Descriptions.valueOf(name)))); //$NON-NLS-1$
        }
        List<AnnotationDescription> orders = new ArrayList<>();
        for (KeyMirror.Order order : key.getOrder()) {
            String direction = order.getDirection() == KeyMirror.Direction.ASCENDANT
                    ? "ASC" //$NON-NLS-1$
                    : "DESC"; //$NON-NLS-1$
            Map<String, ValueDescription> elements = new LinkedHashMap<>();
            elements.put("direction", //$NON-NLS-1$
                    new EnumConstantDescription(TYPE_KEY_DIRECTION, direction));
            elements.put("expression", //$NON-NLS-1$
                    Descriptions.valueOf(order.getProperty().getName()));
            orders.add(new AnnotationDescription(TYPE_KEY_ORDER, elements));
        }
        Map<String, ValueDescription> elements = new LinkedHashMap<>();
        elements.put("group", //$NON-NLS-1$
                ArrayDescription.elementsOf(TYPE_KEY_GROUP, groups));
        elements.put("order", //$NON-NLS-1$
                ArrayDescription.elementsOf(TYPE_KEY_ORDER, orders));
        AnnotationDescription description = new AnnotationDescription(TYPE_KEY, elements);
        Annotation attribute = DescriptionHelper.resolveAnnotation(imports, description);
        return attribute;
    }

    /**
     * Creates arguments about operator element.
     * @param environment current environment
     * @param element target operator element
     * @param imports import builder
     * @return generated syntax model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<Expression> toArguments(
            CompileEnvironment environment,
            OperatorElement element,
            ImportBuilder imports) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(element, "element must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(imports, "imports must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(element.getDescription(), "element.description must not be null"); //$NON-NLS-1$
        ModelFactory factory = Models.getModelFactory();
        List<Expression> results = new ArrayList<>();
        for (Node param : element.getDescription().getParameters()) {
            results.add(factory.newSimpleName(param.getName()));
        }
        return results;
    }

    /**
     * Creates operator node constructor statements.
     * @param environment current environment
     * @param element target operator element
     * @param builderExpression the {@code FlowElementBuilder} instance expression
     * @param imports import builder
     * @return generated syntax model
     */
    public static List<Statement> toNodeConstructorStatements(
            CompileEnvironment environment,
            OperatorElement element,
            Expression builderExpression,
            ImportBuilder imports) {
        ModelFactory f = Models.getModelFactory();
        List<Statement> statements = new ArrayList<>();
        for (Node node : element.getDescription().getAllNodes()) {
            String method;
            List<Expression> arguments = new ArrayList<>();
            switch (node.getKind()) {
            case INPUT:
                method = "defineInput"; //$NON-NLS-1$
                arguments.add(Models.toLiteral(f, node.getName()));
                arguments.add(f.newSimpleName(node.getName()));
                break;
            case OUTPUT:
                method = "defineOutput"; //$NON-NLS-1$
                arguments.add(Models.toLiteral(f, node.getName()));
                arguments.add(resolveOutputType(environment, imports, node, element));
                break;
            case DATA:
                method = "defineData"; //$NON-NLS-1$
                arguments.add(Models.toLiteral(f, node.getName()));
                arguments.add(toLiteral(environment, node.getType(), imports));
                arguments.add(f.newSimpleName(node.getName()));
                break;
            default:
                throw new AssertionError(node.getKind());
            }
            ExpressionBuilder builder = new ExpressionBuilder(f, builderExpression).method(method, arguments);
            if (node.getKey() != null) {
                builder = builder.method("withKey", //$NON-NLS-1$
                        ElementHelper.toKeyInfo(environment, node.getKey(), imports));
            }
            if (node.getExtern() != null) {
                builder = builder.method("withExtern", //$NON-NLS-1$
                        ElementHelper.toExternInfo(environment, node.getExtern(), imports));
            }
            if (node.getReference().getKind() == Kind.PARAMETER) {
                builder = builder.method("withParameterIndex", //$NON-NLS-1$
                        Models.toLiteral(f, ((ParameterReference) node.getReference()).getLocation()));
            }
            statements.add(builder.toStatement());
        }
        for (EnumConstantDescription attribute : element.getDescription().getAttributes()) {
            statements.add(new ExpressionBuilder(f, builderExpression)
                    .method("defineAttribute", DescriptionHelper.resolveConstant(imports, attribute)) //$NON-NLS-1$
                    .toStatement());
        }
        if (element.getDescription().getSupport() != null) {
            ExecutableElement support = element.getDescription().getSupport();
            List<Expression> arguments = new ArrayList<>();
            arguments.add(Models.toLiteral(f, support.getSimpleName().toString()));
            for (VariableElement param : support.getParameters()) {
                arguments.add(toLiteral(environment, param.asType(), imports));
            }
            Expression expression = new TypeBuilder(f, DescriptionHelper.resolve(imports, TYPE_ATTRIBUTES))
                .method("support", arguments) //$NON-NLS-1$
                .toExpression();
            statements.add(new ExpressionBuilder(f, builderExpression)
                .method("defineAttribute", expression) //$NON-NLS-1$
                .toStatement());
        }
        SimpleName editorVar = f.newSimpleName("$editor$"); //$NON-NLS-1$
        statements.add(new ExpressionBuilder(f, builderExpression)
            .method("resolve") //$NON-NLS-1$
            .toLocalVariableDeclaration(
                    DescriptionHelper.resolve(imports, TYPE_EDITOR),
                    editorVar));
        for (Node node : element.getDescription().getOutputs()) {
            Expression type = resolveOutputType(environment, imports, node, element);
            statements.add(new ExpressionBuilder(f, f.newThis())
                .field(f.newSimpleName(node.getName()))
                .assignFrom(new ExpressionBuilder(f, editorVar)
                    .method("createSource", Models.toLiteral(f, node.getName()), type) //$NON-NLS-1$
                    .toExpression())
                .toStatement());
        }
        return statements;
    }

    private static Expression resolveOutputType(
            CompileEnvironment environment,
            ImportBuilder imports,
            Node node,
            OperatorElement element) {
        assert node != null;
        assert element != null;
        TypeMirror type = node.getType();
        if (type.getKind() != TypeKind.TYPEVAR) {
            return toLiteral(environment, type, imports);
        } else {
            Types types = environment.getProcessingEnvironment().getTypeUtils();
            Optional<Node> in = element.getDescription().getInputs().stream()
                .filter(n -> types.isSameType(n.getType(), type))
                .findFirst();
            if (in.isPresent()) {
                return in.map(n -> Models.getModelFactory().newSimpleName(n.getName())).get();
            }
            Optional<Node> arg = element.getDescription().getArguments().stream()
                    .filter(n -> types.isSubtype(n.getType(), environment.findDeclaredType(TYPE_TYPE)))
                    .findFirst();
            if (arg.isPresent()) {
                return arg.map(n -> Models.getModelFactory().newSimpleName(n.getName())).get();
            }
            throw new IllegalStateException(node.getName());
        }
    }

    private static Expression toKeyInfo(
            CompileEnvironment environment,
            KeyMirror element,
            ImportBuilder imports) {
        ModelFactory factory = Models.getModelFactory();
        ExpressionBuilder builder =
                new TypeBuilder(factory, DescriptionHelper.resolve(imports, TYPE_ATTRIBUTES))
                    .method("key"); //$NON-NLS-1$
        for (KeyMirror.Group group : element.getGroup()) {
            builder = builder.method("group", Models.toLiteral(factory, group.getProperty().getName())); //$NON-NLS-1$
        }
        for (KeyMirror.Order order : element.getOrder()) {
            String direction = order.getDirection().name().toLowerCase(Locale.ENGLISH);
            builder = builder.method(direction, Models.toLiteral(factory, order.getProperty().getName()));
        }
        return builder.toExpression();
    }

    private static Expression toExternInfo(
            CompileEnvironment environment,
            ExternMirror element,
            ImportBuilder imports) {
        ModelFactory factory = Models.getModelFactory();
        return new TypeBuilder(factory, DescriptionHelper.resolve(imports, TYPE_ATTRIBUTES))
            .method("extern", //$NON-NLS-1$
                    Models.toLiteral(factory, element.getName()),
                    toLiteral(environment, element.getDescription(), imports))
            .toExpression();
    }

    private static ClassLiteral toLiteral(
            CompileEnvironment environment,
            TypeMirror element,
            ImportBuilder imports) {
        ModelFactory factory = Models.getModelFactory();
        Jsr269 converter = new Jsr269(factory);
        return factory.newClassLiteral(imports.resolve(converter.convert(environment.getErasure(element))));
    }

    private ElementHelper() {
        return;
    }

    /**
     * Returns annotation for the operator factory classes.
     * @param environment current environment
     * @param element target operator class
     * @param imports import builder
     * @return generated syntax model
     */
    public static Annotation toOperatorFactoryAnnotation(
            CompileEnvironment environment,
            OperatorClass element,
            ImportBuilder imports) {
        AnnotationDescription description = new AnnotationDescription(
                TYPE_FACTORY,
                DescriptionHelper.toDescription(environment, element.getDeclaration()));
        return DescriptionHelper.resolveAnnotation(imports, description);
    }

    /**
     * Returns annotation for the operator factory methods.
     * @param environment current environment
     * @param element target operator element
     * @param imports import builder
     * @return generated syntax model
     */
    public static Annotation toOperatorInfoAnnotation(
            CompileEnvironment environment,
            OperatorElement element,
            ImportBuilder imports) {
        List<AnnotationDescription> inputs = new ArrayList<>();
        List<AnnotationDescription> outputs = new ArrayList<>();
        List<AnnotationDescription> arguments = new ArrayList<>();
        for (OperatorDescription.Node node : element.getDescription().getAllNodes()) {
            TypeMirror type = node.getType();
            Map<String, ValueDescription> elements = new LinkedHashMap<>();
            elements.put("name", Descriptions.valueOf(node.getName())); //$NON-NLS-1$
            elements.put("type", //$NON-NLS-1$
                    (ValueDescription) DescriptionHelper.toDescription(environment, environment.getErasure(type)));
            if (node.getKind() == OperatorDescription.Node.Kind.INPUT) {
                putTypeVariable(type, elements);
                elements.put("position", Descriptions.valueOf(inputs.size() + arguments.size())); //$NON-NLS-1$
                inputs.add(new AnnotationDescription(TYPE_INFO_INPUT, elements));
            } else if (node.getKind() == OperatorDescription.Node.Kind.DATA) {
                putTypeVariableIfClass(environment, type, elements);
                elements.put("position", Descriptions.valueOf(inputs.size() + arguments.size())); //$NON-NLS-1$
                arguments.add(new AnnotationDescription(TYPE_INFO_PARAMETER, elements));
            } else if (node.getKind() == OperatorDescription.Node.Kind.OUTPUT) {
                putTypeVariable(type, elements);
                outputs.add(new AnnotationDescription(TYPE_INFO_OUTPUT, elements));
            } else {
                throw new AssertionError(node);
            }
        }
        Map<String, ValueDescription> elements = new LinkedHashMap<>();
        elements.put("kind", //$NON-NLS-1$
                DescriptionHelper.toDescription(environment, element.getAnnotation().getAnnotationType()));
        elements.put("input", //$NON-NLS-1$
                ArrayDescription.elementsOf(TYPE_INFO_INPUT, inputs));
        elements.put("output", //$NON-NLS-1$
                ArrayDescription.elementsOf(TYPE_INFO_OUTPUT, outputs));
        elements.put("parameter", //$NON-NLS-1$
                ArrayDescription.elementsOf(TYPE_INFO_PARAMETER, arguments));
        AnnotationDescription description = new AnnotationDescription(TYPE_INFO, elements);
        return DescriptionHelper.resolveAnnotation(imports, description);
    }

    private static void putTypeVariable(TypeMirror type, Map<String, ValueDescription> elements) {
        if (type.getKind() != TypeKind.TYPEVAR) {
            return;
        }
        putTypeVariable((TypeVariable) type, elements);
    }

    private static void putTypeVariableIfClass(
            CompileEnvironment environment, TypeMirror type, Map<String, ValueDescription> elements) {
        if (type.getKind() != TypeKind.DECLARED) {
            return;
        }
        DeclaredType d = (DeclaredType) type;
        List<? extends TypeMirror> args = d.getTypeArguments();
        if (args.size() != 1 || args.get(0).getKind() != TypeKind.TYPEVAR) {
            return;
        }
        TypeDescription erasure = DescriptionHelper.toDescription(environment, environment.getErasure(d));
        if (erasure.equals(Descriptions.classOf(Class.class)) == false) {
            return;
        }
        putTypeVariable((TypeVariable) args.get(0), elements);
    }

    private static void putTypeVariable(TypeVariable typeVar, Map<String, ValueDescription> elements) {
        String var = typeVar.asElement().getSimpleName().toString();
        elements.put("typeVariable", Descriptions.valueOf(var)); //$NON-NLS-1$
    }
}
