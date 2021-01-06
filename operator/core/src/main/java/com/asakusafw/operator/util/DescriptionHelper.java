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
package com.asakusafw.operator.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.description.AnnotationDescription;
import com.asakusafw.operator.description.ArrayDescription;
import com.asakusafw.operator.description.ArrayTypeDescription;
import com.asakusafw.operator.description.BasicTypeDescription;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.EnumConstantDescription;
import com.asakusafw.operator.description.ImmediateDescription;
import com.asakusafw.operator.description.ObjectDescription;
import com.asakusafw.operator.description.ReifiableTypeDescription;
import com.asakusafw.operator.description.TypeDescription;
import com.asakusafw.operator.description.ValueDescription;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.ArrayCreationExpression;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Common helper methods about descriptions.
 * @since 0.9.0
 * @version 0.9.1
 */
public final class DescriptionHelper {

    private DescriptionHelper() {
        return;
    }

    /**
     * Returns the description.
     * @param environment the current environment
     * @param type the target type
     * @return the corresponded description
     */
    public static TypeDescription toDescription(CompileEnvironment environment, TypeMirror type) {
        switch (type.getKind()) {
        case DECLARED:
            return toDescription(environment, (javax.lang.model.type.DeclaredType) type);
        case ARRAY:
            return toDescription(environment, (javax.lang.model.type.ArrayType) type);
        case BOOLEAN:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.BOOLEAN);
        case BYTE:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.BYTE);
        case SHORT:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.SHORT);
        case INT:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.INT);
        case LONG:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.LONG);
        case FLOAT:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.FLOAT);
        case DOUBLE:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.DOUBLE);
        case CHAR:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.CHAR);
        case VOID:
            return new BasicTypeDescription(BasicTypeDescription.BasicTypeKind.VOID);
        default:
            throw new AssertionError(MessageFormat.format(
                    "unsupported type: {0}", //$NON-NLS-1$
                    type));
        }
    }

    /**
     * Returns the description.
     * @param environment the current environment
     * @param element the target element
     * @return the corresponded description
     */
    public static ClassDescription toDescription(CompileEnvironment environment, TypeElement element) {
        Name name = environment.getProcessingEnvironment().getElementUtils().getBinaryName(element);
        return new ClassDescription(name.toString());
    }

    /**
     * Returns the description.
     * @param environment the current environment
     * @param type the target type
     * @return the corresponded description
     */
    public static ClassDescription toDescription(
            CompileEnvironment environment,
            javax.lang.model.type.DeclaredType type) {
        TypeElement element = (TypeElement) type.asElement();
        return toDescription(environment, element);
    }

    private static TypeDescription toDescription(
            CompileEnvironment environment,
            javax.lang.model.type.ArrayType type) {
        TypeDescription element = toDescription(environment, type.getComponentType());
        return new ArrayTypeDescription(element);
    }

    /**
     * Returns the resolved type.
     * @param importer the current import builder
     * @param type the target class description
     * @return the resolved type
     */
    public static Type resolve(ImportBuilder importer, TypeDescription type) {
        switch (type.getTypeKind()) {
        case BASIC:
            return resolve((BasicTypeDescription) type);
        case CLASS:
            return resolve(importer, (ClassDescription) type);
        case ARRAY:
            return resolve(importer, (ArrayTypeDescription) type);
        default:
            throw new AssertionError(type);
        }
    }

    private static Type resolve(BasicTypeDescription type) {
        ModelFactory factory = Models.getModelFactory();
        switch (type.getBasicTypeKind()) {
        case BOOLEAN:
            return factory.newBasicType(BasicTypeKind.BOOLEAN);
        case BYTE:
            return factory.newBasicType(BasicTypeKind.BYTE);
        case CHAR:
            return factory.newBasicType(BasicTypeKind.CHAR);
        case DOUBLE:
            return factory.newBasicType(BasicTypeKind.DOUBLE);
        case FLOAT:
            return factory.newBasicType(BasicTypeKind.FLOAT);
        case INT:
            return factory.newBasicType(BasicTypeKind.INT);
        case LONG:
            return factory.newBasicType(BasicTypeKind.LONG);
        case SHORT:
            return factory.newBasicType(BasicTypeKind.SHORT);
        case VOID:
            return factory.newBasicType(BasicTypeKind.VOID);
        default:
            throw new AssertionError(type);
        }
    }

    private static Type resolve(ImportBuilder importer, ClassDescription type) {
        ModelFactory factory = Models.getModelFactory();
        return importer.toType(Models.toName(factory, type.getClassName()));
    }

    private static Type resolve(ImportBuilder importer, ArrayTypeDescription type) {
        int dim = 1;
        TypeDescription current = type.getComponentType();
        while (current.getTypeKind() == TypeDescription.TypeKind.ARRAY) {
            current = ((ArrayTypeDescription) current).getComponentType();
            dim++;
        }
        ModelFactory factory = Models.getModelFactory();
        Type result = resolve(importer, current);
        for (int i = 0; i < dim; i++) {
            result = factory.newArrayType(result);
        }
        return result;
    }

    /**
     * Returns the resolved value.
     * @param importer the current import builder
     * @param value the target constant value
     * @return the resolved expression
     */
    public static Expression resolveValue(ImportBuilder importer, ValueDescription value) {
        switch (value.getValueKind()) {
        case OBJECT:
            return resolveValue(importer, (ObjectDescription) value);
        case ARRAY:
            return resolveValue(importer, (ArrayDescription) value);
        default:
            return resolveConstant(importer, value);
        }
    }

    /**
     * Returns the resolved constant value.
     * @param importer the current import builder
     * @param constant the target constant value
     * @return the resolved expression
     */
    public static Expression resolveConstant(ImportBuilder importer, ValueDescription constant) {
        switch (constant.getValueKind()) {
        case IMMEDIATE:
            return resolveConstant((ImmediateDescription) constant);
        case ENUM_CONSTANT:
            return resolveConstant(importer, (EnumConstantDescription) constant);
        case TYPE:
            return resolveConstant(importer, (ReifiableTypeDescription) constant);
        case ARRAY:
            return resolveConstant(importer, (ArrayDescription) constant);
        default:
            throw new IllegalArgumentException(MessageFormat.format(
                    "not a constant value: {0}", //$NON-NLS-1$
                    constant));
        }
    }

    private static Expression resolveConstant(ImmediateDescription constant) {
        ModelFactory factory = Models.getModelFactory();
        Object value = constant.getValue();
        if (value == null) {
            return Models.toNullLiteral(factory);
        } else {
            return Models.toLiteral(factory, value);
        }
    }

    private static Expression resolveConstant(ImportBuilder importer, EnumConstantDescription constant) {
        ModelFactory factory = Models.getModelFactory();
        Type type = resolve(importer, constant.getDeclaringClass());
        return new TypeBuilder(factory, type).field(constant.getName()).toExpression();
    }

    private static ClassLiteral resolveConstant(ImportBuilder importer, ReifiableTypeDescription constant) {
        ModelFactory factory = Models.getModelFactory();
        return factory.newClassLiteral(resolve(importer, constant));
    }

    private static ArrayCreationExpression resolveConstant(ImportBuilder importer, ArrayDescription constant) {
        ModelFactory factory = Models.getModelFactory();
        ArrayType type = (ArrayType) resolve(importer, constant.getValueType());
        List<Expression> elements = new ArrayList<>();
        for (ValueDescription value : constant.getElements()) {
            elements.add(resolveConstant(importer, value));
        }
        return factory.newArrayCreationExpression(type, factory.newArrayInitializer(elements));
    }

    private static Expression resolveValue(ImportBuilder importer, ObjectDescription value) {
        ModelFactory factory = Models.getModelFactory();
        Type type = resolve(importer, value.getValueType());
        List<Expression> arguments = new ArrayList<>();
        for (ValueDescription argValue : value.getArguments()) {
            arguments.add(resolveValue(importer, argValue));
        }
        return Optional.ofNullable(value.getMethodName())
                .map(name -> new TypeBuilder(factory, type).method(name, arguments))
                .orElseGet(() -> new TypeBuilder(factory, type).newObject(arguments))
                .toExpression();
    }

    private static ArrayCreationExpression resolveValue(ImportBuilder importer, ArrayDescription value) {
        ModelFactory factory = Models.getModelFactory();
        ArrayType type = (ArrayType) resolve(importer, value.getValueType());
        List<Expression> elements = new ArrayList<>();
        for (ValueDescription elemValue : value.getElements()) {
            elements.add(resolveValue(importer, elemValue));
        }
        return factory.newArrayCreationExpression(type, factory.newArrayInitializer(elements));
    }

    /**
     * Returns the resolved annotation.
     * @param importer the current import builder
     * @param annotation the target annotation description
     * @return the resolved annotation
     */
    public static Annotation resolveAnnotation(ImportBuilder importer, AnnotationDescription annotation) {
        ModelFactory factory = Models.getModelFactory();
        NamedType type = (NamedType) resolve(importer, annotation.getDeclaringClass());
        Map<String, Expression> elements = new LinkedHashMap<>();
        for (Map.Entry<String, ValueDescription> entry : annotation.getElements().entrySet()) {
            String name = entry.getKey();
            Expression value = resolveAnnotationValue(importer, entry.getValue());
            elements.put(name, value);
        }
        if (elements.isEmpty()) {
            return factory.newMarkerAnnotation(type);
        } else if (elements.size() == 1 && elements.containsKey("value")) { //$NON-NLS-1$
            return factory.newSingleElementAnnotation(type, elements.get("value")); //$NON-NLS-1$
        } else {
            List<AnnotationElement> elementList = new ArrayList<>();
            for (Map.Entry<String, Expression> entry : elements.entrySet()) {
                String name = entry.getKey();
                Expression value = entry.getValue();
                elementList.add(factory.newAnnotationElement(factory.newSimpleName(name), value));
            }
            return factory.newNormalAnnotation(type, elementList);
        }
    }

    private static Expression resolveAnnotationValue(ImportBuilder importer, ValueDescription value) {
        switch (value.getValueKind()) {
        case IMMEDIATE:
        case TYPE:
        case ENUM_CONSTANT:
            return resolveConstant(importer, value);
        case ARRAY: {
            ModelFactory factory = Models.getModelFactory();
            List<Expression> elements = new ArrayList<>();
            for (ValueDescription element : ((ArrayDescription) value).getElements()) {
                elements.add(resolveAnnotationValue(importer, element));
            }
            if (elements.size() == 1) {
                return elements.get(0);
            }
            return factory.newArrayInitializer(elements);
        }
        case ANNOTATION:
            return resolveAnnotation(importer, (AnnotationDescription) value);
        default:
            throw new IllegalArgumentException(MessageFormat.format(
                    "not an annotation value: {0}", //$NON-NLS-1$
                    value));
        }
    }
}
