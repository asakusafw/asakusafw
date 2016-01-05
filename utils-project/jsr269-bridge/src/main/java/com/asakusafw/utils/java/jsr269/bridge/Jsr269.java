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
package com.asakusafw.utils.java.jsr269.bridge;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.tools.JavaFileObject;

import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.BasicType;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Model;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.Wildcard;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.Emitter;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Converts model objects defined in {@code JSR-269} into the {@link Model the Java DOM} objects.
 * @since 0.1.0
 * @version 0.7.0
 */
public class Jsr269 {

    private static final Element[] EMPTY_ELEMENTS = new Element[0];

    private static final EnumMap<javax.lang.model.element.Modifier, ModifierKind> MODIFIERS;
    static {
        MODIFIERS = new EnumMap<>(javax.lang.model.element.Modifier.class);
        MODIFIERS.put(javax.lang.model.element.Modifier.ABSTRACT, ModifierKind.ABSTRACT);
        MODIFIERS.put(javax.lang.model.element.Modifier.FINAL, ModifierKind.FINAL);
        MODIFIERS.put(javax.lang.model.element.Modifier.NATIVE, ModifierKind.NATIVE);
        MODIFIERS.put(javax.lang.model.element.Modifier.PRIVATE, ModifierKind.PRIVATE);
        MODIFIERS.put(javax.lang.model.element.Modifier.PROTECTED, ModifierKind.PROTECTED);
        MODIFIERS.put(javax.lang.model.element.Modifier.PUBLIC, ModifierKind.PUBLIC);
        MODIFIERS.put(javax.lang.model.element.Modifier.STATIC, ModifierKind.STATIC);
        MODIFIERS.put(javax.lang.model.element.Modifier.STRICTFP, ModifierKind.STRICTFP);
        MODIFIERS.put(javax.lang.model.element.Modifier.SYNCHRONIZED, ModifierKind.SYNCHRONIZED);
        MODIFIERS.put(javax.lang.model.element.Modifier.TRANSIENT, ModifierKind.TRANSIENT);
        MODIFIERS.put(javax.lang.model.element.Modifier.VOLATILE, ModifierKind.VOLATILE);
    }

    private final ModelFactory factory;

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Jsr269(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        this.factory = factory;
    }

    /**
     * Converts the package element.
     * Note that documentation comments and annotations are ignored.
     * @param packageElement the target package element
     * @return the corresponded package declaration, or {@code null} if the target is a default (unnamed) package
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public PackageDeclaration convert(PackageElement packageElement) {
        if (packageElement == null) {
            throw new IllegalArgumentException(
                "packageElement must not be null"); //$NON-NLS-1$
        }
        if (packageElement.isUnnamed()) {
            return null;
        }
        return factory.newPackageDeclaration(
                null,
                Collections.<Annotation>emptyList(),
                convert(packageElement.getQualifiedName()));
    }

    /**
     * Converts the name.
     * @param name the target name
     * @return the corresponded name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Name convert(javax.lang.model.element.Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return Models.toName(factory, name.toString());
    }

    /**
     * Converts the type mirror.
     * This also can {@code void} pseudo-type.
     * @param type the target type
     * @return the corresponded type, or {@code null} if it is unsupported type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Type convert(TypeMirror type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return convert0(type);
    }

    /**
     * Converts the array type.
     * @param type the target type
     * @return the corresponded type, or {@code null} if it is unsupported type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ArrayType convert(javax.lang.model.type.ArrayType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        int dimensions = 1;
        TypeMirror component = type.getComponentType();
        while (component.getKind() == TypeKind.ARRAY) {
            component = ((javax.lang.model.type.ArrayType) component).getComponentType();
            dimensions++;
        }
        Type result = convert0(component);
        if (result == null) {
            return null;
        }
        assert (result instanceof ArrayType) == false;
        for (int i = 0; i < dimensions; i++) {
            result = factory.newArrayType(result);
        }
        assert result instanceof ArrayType;
        return (ArrayType) result;
    }

    /**
     * Converts the class, interface type, or its parameterized type.
     * @param type the target type
     * @return the corresponded type, or {@code null} if it is unsupported type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Type convert(DeclaredType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        NamedType raw = convertToRawType(type);
        if (raw == null) {
            return null;
        }
        List<? extends TypeMirror> typeArguments = type.getTypeArguments();
        if (typeArguments.isEmpty()) {
            return raw;
        }
        List<Type> arguments = new ArrayList<>();
        for (TypeMirror mirror : typeArguments) {
            Type argument = convert0(mirror);
            if (argument == null) {
                return null;
            }
            arguments.add(argument);
        }
        return factory.newParameterizedType(raw, arguments);
    }

    /**
     * Converts the {@code void} pseudo-type.
     * @param type the target type
     * @return the corresponded type, or {@code null} if it is unsupported type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Type convert(NoType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        switch (type.getKind()) {
        case VOID:
            return factory.newBasicType(BasicTypeKind.VOID);
        case PACKAGE:
        case NONE:
            return null;
        default:
            throw new AssertionError(type);
        }
    }

    /**
     * Converts the primitive type.
     * @param type the target type
     * @return the corresponded type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public BasicType convert(PrimitiveType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        switch (type.getKind()) {
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
        default:
            throw new AssertionError(type);
        }
    }

    /**
     * Converts the type variable.
     * @param type the target type variable
     * @return the corresponded type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public NamedType convert(TypeVariable type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        SimpleName name = asSimpleName(type.asElement().getSimpleName());
        return factory.newNamedType(name);
    }

    /**
     * Converts the wildcard type.
     * @param type the target type
     * @return the corresponded type, or {@code null} if it is unsupported wildcard format
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Wildcard convert(WildcardType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        TypeMirror upper = type.getExtendsBound();
        if (upper != null) {
            if (type.getSuperBound() != null) {
                return null;
            }
            Type bound = convert0(upper);
            if (bound == null) {
                return null;
            }
            return factory.newWildcard(WildcardBoundKind.UPPER_BOUNDED, bound);
        }
        TypeMirror lower = type.getSuperBound();
        if (lower != null) {
            Type bound = convert0(lower);
            if (bound == null) {
                return null;
            }
            return factory.newWildcard(WildcardBoundKind.LOWER_BOUNDED, bound);
        }
        return factory.newWildcard(WildcardBoundKind.UNBOUNDED, null);
    }

    private SimpleName asSimpleName(javax.lang.model.element.Name simpleName) {
        assert simpleName != null;
        return factory.newSimpleName(simpleName.toString());
    }

    private NamedType convertToRawType(DeclaredType type) {
        assert type != null;
        Element element = type.asElement();
        if (element == null) {
            return null;
        }
        switch (element.getKind()) {
        case CLASS:
        case INTERFACE:
        case ENUM:
        case ANNOTATION_TYPE: {
            Name name = asName(((TypeElement) element).getQualifiedName());
            return factory.newNamedType(name);
        }
        default:
            throw new AssertionError(type);
        }
    }

    private Name asName(javax.lang.model.element.Name qualifiedName) {
        assert qualifiedName != null;
        return Models.toName(factory, qualifiedName.toString());
    }

    private Type convert0(TypeMirror type) {
        assert type != null;
        switch (type.getKind()) {
        case ARRAY:
            return convert((javax.lang.model.type.ArrayType) type);
        case DECLARED:
        case ERROR:
            return convert((DeclaredType) type);
        case BOOLEAN:
        case BYTE:
        case CHAR:
        case DOUBLE:
        case FLOAT:
        case INT:
        case LONG:
        case SHORT:
            return convert((PrimitiveType) type);
        case VOID:
        case PACKAGE:
        case NONE:
            return convert((NoType) type);
        case TYPEVAR:
            return convert((TypeVariable) type);
        case WILDCARD:
            return convert((WildcardType) type);
        default:
            return null;
        }
    }

    /**
     * Converts the modifiers.
     * @param modifiers the target modifiers
     * @return the converted modifier kinds
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<ModifierKind> convert(Collection<javax.lang.model.element.Modifier> modifiers) {
        if (modifiers == null) {
            throw new IllegalArgumentException("modifiers must not be null"); //$NON-NLS-1$
        }
        List<ModifierKind> results = new ArrayList<>();
        for (javax.lang.model.element.Modifier modifier : modifiers) {
            ModifierKind kind = MODIFIERS.get(modifier);
            if (kind != null) {
                results.add(kind);
            }
        }
        return results;
    }

    /**
     * Emits the target compilation unit using the filer.
     * @param filer the filer
     * @param unit the target compilation unit
     * @throws IOException if error was occurred while emitting the target  compilation unit
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public void emit(Filer filer, CompilationUnit unit) throws IOException {
        emit(filer, unit, EMPTY_ELEMENTS);
    }

    /**
     * Emits the target compilation unit using the filer.
     * @param filer the filer
     * @param unit the target compilation unit
     * @param originatingElements the original elements
     * @throws IOException if error was occurred while emitting the target  compilation unit
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.7.0
     */
    public void emit(Filer filer, CompilationUnit unit, Element... originatingElements) throws IOException {
        if (filer == null) {
            throw new IllegalArgumentException("filer must not be null"); //$NON-NLS-1$
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null"); //$NON-NLS-1$
        }
        StringBuilder name = new StringBuilder();
        if (unit.getPackageDeclaration() != null) {
            name.append(unit.getPackageDeclaration().getName());
            name.append('.');
        }
        TypeDeclaration primary = Emitter.findPrimaryType(unit);
        if (primary == null) {
            name.append("package-info"); //$NON-NLS-1$
        } else {
            name.append(primary.getName());
        }
        JavaFileObject source = filer.createSourceFile(name, originatingElements);
        try (PrintWriter output = new PrintWriter(source.openWriter())) {
            Models.emit(unit, output);
        }
    }
}
