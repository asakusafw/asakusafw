/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import java.io.Writer;
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
 * {@code JSR-269}が規定するモデルオブジェクトをこのライブラリに適した構造に変換する。
 * @since 0.1.0
 * @version 0.7.0
 */
public class Jsr269 {

    private static final Element[] EMPTY_ELEMENTS = new Element[0];

    private static final EnumMap<javax.lang.model.element.Modifier, ModifierKind> MODIFIERS;
    static {
        MODIFIERS =
            new EnumMap<javax.lang.model.element.Modifier, ModifierKind>(javax.lang.model.element.Modifier.class);
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
     * インスタンスを生成する。
     * @param factory 変換に利用するファクトリオブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Jsr269(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        this.factory = factory;
    }

    /**
     * パッケージ宣言を変換する。
     * @param packageElement 変換するパッケージ宣言
     * @return 変換後のパッケージ宣言、ドキュメンテーションや注釈は省略され、
     *     無名パッケージの場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 名前を変換する。
     * @param name 変換する名前
     * @return 変換後の名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Name convert(javax.lang.model.element.Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return Models.toName(factory, name.toString());
    }

    /**
     * 任意の型オブジェクトを変換する。
     * <p>
     * 型として適切でないオブジェクトについては変換の対象にならない。
     * ただし、{@code void}に関しては対応する型の表現を返す。
     * </p>
     * @param type 変換対象の型
     * @return 対応する型、変換対象に取れない型の場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Type convert(TypeMirror type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return convert0(type);
    }

    /**
     * 配列型のオブジェクトを変換する。
     * <p>
     * 型として適切でないオブジェクトについては変換の対象にならない。
     * </p>
     * @param type 変換対象の型
     * @return 対応する型、変換対象に取れない型の場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 宣言型およびパラメータ化型のオブジェクトを変換する。
     * <p>
     * 型として適切でないオブジェクトについては変換の対象にならない。
     * </p>
     * @param type 変換対象の型
     * @return 対応する型、変換対象に取れない型の場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
        List<Type> arguments = new ArrayList<Type>();
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
     * {@code void}を表すオブジェクトを変換する。
     * <p>
     * {@code void}以外のオブジェクトについては変換の対象にならない。
     * </p>
     * @param type 変換対象の型
     * @return 対応する型、変換対象に取れない型の場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * プリミティブ型のオブジェクトを変換する。
     * @param type 変換対象の型
     * @return 対応する型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 型変数のオブジェクトを変換する。
     * @param type 変換対象の型変数
     * @return 対応する型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public NamedType convert(TypeVariable type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        SimpleName name = asSimpleName(type.asElement().getSimpleName());
        return factory.newNamedType(name);
    }

    /**
     * ワイルドカードのオブジェクトを変換する。
     * <p>
     * ワイルドカードとして適切でないオブジェクトについては変換の対象にならない。
     * </p>
     * @param type 変換対象の型
     * @return 対応する型、変換対象に取れない型の場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 修飾子を変換する。
     * @param modifiers 対象の修飾子一覧
     * @return 変換後の修飾子一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public List<ModifierKind> convert(Collection<javax.lang.model.element.Modifier> modifiers) {
        if (modifiers == null) {
            throw new IllegalArgumentException("modifiers must not be null"); //$NON-NLS-1$
        }
        List<ModifierKind> results = new ArrayList<ModifierKind>();
        for (javax.lang.model.element.Modifier modifier : modifiers) {
            ModifierKind kind = MODIFIERS.get(modifier);
            if (kind != null) {
                results.add(kind);
            }
        }
        return results;
    }

    /**
     * 指定のファイラーを利用して、コンパイル単位の内容を出力する。
     * @param filer 利用するファイラー
     * @param unit 出力するコンパイル単位
     * @throws IOException 出力時にエラーが発生した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void emit(Filer filer, CompilationUnit unit) throws IOException {
        emit(filer, unit, EMPTY_ELEMENTS);
    }

    /**
     * 指定のファイラーを利用して、コンパイル単位の内容を出力する。
     * @param filer 利用するファイラー
     * @param unit 出力するコンパイル単位
     * @param originatingElements 生成するコンパイル単位の元になった要素
     * @throws IOException 出力時にエラーが発生した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
        Writer writer = source.openWriter();
        try {
            PrintWriter output = new PrintWriter(writer);
            Models.emit(unit, output);
            output.close();
        } finally {
            writer.close();
        }
    }
}
