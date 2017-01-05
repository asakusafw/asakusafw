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
package com.asakusafw.compiler.operator.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.compiler.operator.OperatorPortDeclaration;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.vocabulary.operator.KeyInfo;
import com.asakusafw.vocabulary.operator.OperatorInfo;

/**
 * Utilities about building Java DOM.
 * @since 0.1.0
 * @version 0.5.0
 */
public class GeneratorUtil {

    private final OperatorCompilingEnvironment environment;

    private final ModelFactory factory;

    private final ImportBuilder importer;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param factory the Java DOM factory
     * @param importer the current import declaration builder
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public GeneratorUtil(
            OperatorCompilingEnvironment environment,
            ModelFactory factory,
            ImportBuilder importer) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
        this.environment = environment;
        this.factory = factory;
        this.importer = importer;
    }

    /**
     * Returns a simple name of the operator factory class for the specified type.
     * @param type the target type
     * @return the corresponded simple name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public final SimpleName getFactoryName(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return factory.newSimpleName(MessageFormat.format(
                "{0}{1}", //$NON-NLS-1$
                type.getSimpleName(),
                "Factory")); //$NON-NLS-1$
    }

    /**
     * Returns a simple name of the operator implementation class for the specified type.
     * @param type the target type
     * @return the corresponded simple name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public final SimpleName getImplementorName(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return factory.newSimpleName(getImplmentorName(
                type.getSimpleName().toString()));
    }

    /**
     * Returns a simple name of the operator factory class for the specified type.
     * @param typeName the target type name
     * @return the corresponded simple name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static final String getImplmentorName(String typeName) {
        Precondition.checkMustNotBeNull(typeName, "typeName"); //$NON-NLS-1$
        return MessageFormat.format(
                "{0}{1}", //$NON-NLS-1$
                typeName,
                "Impl"); //$NON-NLS-1$
    }

    /**
     * Returns the type model for the specified type.
     * @param type the target type
     * @return the type model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public final Type t(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return importer.resolve(new Jsr269(factory).convert(type));
    }

    /**
     * Returns the type model for the specified type.
     * @param type the target type
     * @return the type model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public final Type t(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        DeclaredType t = environment.getTypeUtils().getDeclaredType(type);
        return importer.resolve(new Jsr269(factory).convert(t));
    }

    /**
     * Returns the type model for the specified type.
     * @param type the target type
     * @return the type model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public final Type t(java.lang.reflect.Type type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return importer.toType(type);
    }

    /**
     * Returns the literal model for the specified value.
     * @param value the target value
     * @return the literal model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Literal v(String value) {
        Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
        return Models.toLiteral(factory, value);
    }

    /**
     * Returns the literal model for the specified value.
     * @param pattern the string patter ({@link MessageFormat} style)
     * @param arguments the pattern arguments ({@link MessageFormat} style)
     * @return the literal model
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Literal v(String pattern, Object... arguments) {
        Precondition.checkMustNotBeNull(pattern, "pattern"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arguments, "arguments"); //$NON-NLS-1$
        return Models.toLiteral(factory, MessageFormat.format(pattern, arguments));
    }

    /**
     * Returns the {@link Source operator output} for the corresponding data type.
     * @param type the target data type
     * @return the corresponded operator output type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Type toSourceType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(Source.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }

    /**
     * Returns the {@link In flow input} for the corresponding data type.
     * @param type the target data type
     * @return the corresponded flow input type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Type toInType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(In.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }

    /**
     * Returns the {@link Out flow output type} for the corresponding data type.
     * @param type the target data type
     * @return the corresponded flow output type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Type toOutType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(Out.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }

    /**
     * Returns the representation for type parameter declarations of the executable element.
     * @param element target element
     * @return the corresponded representation
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<TypeParameterDeclaration> toTypeParameters(ExecutableElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeParameters(element.getTypeParameters());
    }

    /**
     * Returns the representation for type parameter declarations of the type element.
     * @param element target element
     * @return the corresponded representation
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<TypeParameterDeclaration> toTypeParameters(TypeElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeParameters(element.getTypeParameters());
    }

    private List<TypeParameterDeclaration> toTypeParameters(
            List<? extends TypeParameterElement> typeParameters) {
        assert typeParameters != null;
        List<TypeParameterDeclaration> results = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeParameters) {
            SimpleName name = factory.newSimpleName(typeParameter.getSimpleName().toString());
            List<Type> typeBounds = new ArrayList<>();
            for (TypeMirror typeBound : typeParameter.getBounds()) {
                typeBounds.add(t(typeBound));
            }
            results.add(factory.newTypeParameterDeclaration(name, typeBounds));
        }
        return results;
    }

    /**
     * Returns the representation for type variables of the executable element.
     * @param element target element
     * @return the corresponded representation
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Type> toTypeVariables(ExecutableElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeVariables(element.getTypeParameters());
    }

    /**
     * Returns the representation for type variables of the type element.
     * @param element target element
     * @return the corresponded representation
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<Type> toTypeVariables(TypeElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeVariables(element.getTypeParameters());
    }

    private List<Type> toTypeVariables(List<? extends TypeParameterElement> typeParameters) {
        List<Type> results = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeParameters) {
            SimpleName name = factory.newSimpleName(typeParameter.getSimpleName().toString());
            results.add(factory.newNamedType(name));
        }
        return results;
    }

    /**
     * Converts {@link OperatorPortDeclaration} into an operator factory method parameter.
     * @param var target port declaration
     * @param name the actual parameter name
     * @return related parameter declaration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FormalParameterDeclaration toFactoryMethodInput(OperatorPortDeclaration var, SimpleName name) {
        Precondition.checkMustNotBeNull(var, "var"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        AttributeBuilder attributes = new AttributeBuilder(factory);
        ShuffleKey key = var.getShuffleKey();
        if (key != null) {
            List<Expression> group = new ArrayList<>();
            for (String entry : key.getGroupProperties()) {
                group.addAll(new AttributeBuilder(factory)
                        .annotation(
                                t(KeyInfo.Group.class),
                                "expression", //$NON-NLS-1$
                                Models.toLiteral(factory, entry))
                        .toAnnotations());
            }
            List<Expression> order = new ArrayList<>();
            for (ShuffleKey.Order entry : key.getOrderings()) {
                order.addAll(new AttributeBuilder(factory)
                        .annotation(
                                t(KeyInfo.Order.class),
                                "direction", new TypeBuilder(factory, t(KeyInfo.Direction.class)) //$NON-NLS-1$
                                        .field(entry.getDirection().name())
                                        .toExpression(),
                                "expression", Models.toLiteral(factory, entry.getProperty())) //$NON-NLS-1$
                        .toAnnotations());
            }

            attributes.annotation(
                    t(KeyInfo.class),
                    "group", factory.newArrayInitializer(group), //$NON-NLS-1$
                    "order", factory.newArrayInitializer(order)); //$NON-NLS-1$
        }
        return factory.newFormalParameterDeclaration(
                attributes.toAttributes(),
                toSourceType(var.getType().getRepresentation()),
                false,
                name,
                0);
    }

    /**
     * Converts {@link OperatorPortDeclaration} into a member of {@link OperatorInfo}.
     * @param var target port declaration
     * @param position the factory parameter position
     * @return related meta-data
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Expression toMetaData(OperatorPortDeclaration var, int position) {
        Precondition.checkMustNotBeNull(var, "var"); //$NON-NLS-1$
        NamedType type;
        TypeMirror representation = var.getType().getRepresentation();
        List<AnnotationElement> members = new ArrayList<>();
        members.add(factory.newAnnotationElement(
                factory.newSimpleName("name"), //$NON-NLS-1$
                Models.toLiteral(factory, var.getName())));
        members.add(factory.newAnnotationElement(
                factory.newSimpleName("type"), //$NON-NLS-1$
                factory.newClassLiteral(t(environment.getErasure(representation)))));
        if (var.getKind() == OperatorPortDeclaration.Kind.INPUT) {
            type = (NamedType) t(OperatorInfo.Input.class);
            members.add(factory.newAnnotationElement(
                    factory.newSimpleName("position"), //$NON-NLS-1$
                    Models.toLiteral(factory, position)));
            String typeVariable = getTypeVariableName(representation);
            if (typeVariable != null) {
                members.add(factory.newAnnotationElement(
                        factory.newSimpleName("typeVariable"), //$NON-NLS-1$
                        Models.toLiteral(factory, typeVariable)));
            }
        } else if (var.getKind() == OperatorPortDeclaration.Kind.OUTPUT) {
            type = (NamedType) t(OperatorInfo.Output.class);
            String typeVariable = getTypeVariableName(representation);
            if (typeVariable != null) {
                members.add(factory.newAnnotationElement(
                        factory.newSimpleName("typeVariable"), //$NON-NLS-1$
                        Models.toLiteral(factory, typeVariable)));
            }
        } else if (var.getKind() == OperatorPortDeclaration.Kind.CONSTANT) {
            type = (NamedType) t(OperatorInfo.Parameter.class);
            members.add(factory.newAnnotationElement(
                    factory.newSimpleName("position"), //$NON-NLS-1$
                    Models.toLiteral(factory, position)));
            String typeVariable = getTypeVariableNameInClass(representation);
            if (typeVariable != null) {
                members.add(factory.newAnnotationElement(
                        factory.newSimpleName("typeVariable"), //$NON-NLS-1$
                        Models.toLiteral(factory, typeVariable)));
            }
        } else {
            throw new AssertionError(var);
        }
        return factory.newNormalAnnotation(
                type,
                members);
    }

    private String getTypeVariableName(TypeMirror representation) {
        if (representation.getKind() == TypeKind.TYPEVAR) {
            return ((TypeVariable) representation).asElement().getSimpleName().toString();
        }
        return null;
    }

    private String getTypeVariableNameInClass(TypeMirror representation) {
        if (representation.getKind() != TypeKind.DECLARED) {
            return null;
        }
        List<? extends TypeMirror> typeArgs = ((DeclaredType) representation).getTypeArguments();
        if (typeArgs.size() != 1) {
            return null;
        }
        DeclaredType raw = (DeclaredType) environment.getErasure(representation);
        if (environment.getTypeUtils().isSameType(raw, environment.getDeclaredType(Class.class)) == false) {
            return null;
        }
        return getTypeVariableName(typeArgs.get(0));
    }
}
