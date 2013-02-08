/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.operator.OperatorInfo;

/**
 * JavaのDOMを構築する際のユーティリティ。
 * @since 0.1.0
 * @version 0.5.0
 */
public class GeneratorUtil {

    private final OperatorCompilingEnvironment environment;

    private final ModelFactory factory;

    private final ImportBuilder importer;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @param factory DOMを構築するためのファクトリ
     * @param importer インポート宣言を構築するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 指定の型に対する演算子ファクトリークラスの単純名を返す。
     * @param type 対象の型
     * @return 対応する単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public final SimpleName getFactoryName(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return factory.newSimpleName(MessageFormat.format(
                "{0}{1}",
                type.getSimpleName(),
                "Factory"));
    }

    /**
     * 指定の型に対する演算子実装クラスの単純名を返す。
     * @param type 対象の型
     * @return 対応する単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public final SimpleName getImplementorName(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return factory.newSimpleName(getImplmentorName(
                type.getSimpleName().toString()));
    }

    /**
     * 指定の型に対する演算子実装クラスの名前を返す。
     * @param typeName 対象の型名
     * @return 対応する単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static final String getImplmentorName(String typeName) {
        Precondition.checkMustNotBeNull(typeName, "typeName"); //$NON-NLS-1$
        return MessageFormat.format(
                "{0}{1}",
                typeName,
                "Impl");
    }

    /**
     * 指定の型に対応する型のモデルを返す。
     * @param type 対象の型
     * @return 型のモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public final Type t(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return importer.resolve(new Jsr269(factory).convert(type));
    }

    /**
     * 指定の型に対応する型のモデルを返す。
     * @param type 対象の型
     * @return 型のモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public final Type t(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        DeclaredType t = environment.getTypeUtils().getDeclaredType(type);
        return importer.resolve(new Jsr269(factory).convert(t));
    }

    /**
     * 指定の型に対応する型のモデルを返す。
     * @param type 対象の型
     * @return 型のモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public final Type t(java.lang.reflect.Type type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return importer.toType(type);
    }

    /**
     * 指定の値に対応するリテラルのモデルを返す。
     * @param value 対象の値
     * @return 指定の値に対応するリテラルのモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Literal v(String value) {
        Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
        return Models.toLiteral(factory, value);
    }

    /**
     * 指定の値に対応するリテラルのモデルを返す。
     * @param pattern {@link MessageFormat}のパターン
     * @param arguments {@link MessageFormat}の引数一覧
     * @return 指定の値に対応するリテラルのモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Literal v(String pattern, Object... arguments) {
        Precondition.checkMustNotBeNull(pattern, "pattern"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(arguments, "arguments"); //$NON-NLS-1$
        return Models.toLiteral(factory, MessageFormat.format(pattern, arguments));
    }

    /**
     * 指定のデータの種類に対する{@link Source 結果型}を返す。
     * @param type 対象データの種類
     * @return 対応する結果型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Type toSourceType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(Source.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }

    /**
     * 指定のデータの種類に対する{@link In 入力型}を返す。
     * @param type 対象データの種類
     * @return 対応する入力型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Type toInType(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Type source = t(In.class);
        Type modelType = t(type);
        return factory.newParameterizedType(source, Collections.singletonList(modelType));
    }

    /**
     * 指定のデータの種類に対する{@link Out 出力型}を返す。
     * @param type 対象データの種類
     * @return 対応する出力型
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<TypeParameterDeclaration> toTypeParameters(ExecutableElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeParameters(element.getTypeParameters());
    }

    /**
     * Returns the representation for type parameter declarations of the type element.
     * @param element target element
     * @return the corresponded representation
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<TypeParameterDeclaration> toTypeParameters(TypeElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeParameters(element.getTypeParameters());
    }

    private List<TypeParameterDeclaration> toTypeParameters(
            List<? extends TypeParameterElement> typeParameters) {
        assert typeParameters != null;
        List<TypeParameterDeclaration> results = Lists.create();
        for (TypeParameterElement typeParameter : typeParameters) {
            SimpleName name = factory.newSimpleName(typeParameter.getSimpleName().toString());
            List<Type> typeBounds = Lists.create();
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
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Type> toTypeVariables(ExecutableElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeVariables(element.getTypeParameters());
    }

    /**
     * Returns the representation for type variables of the type element.
     * @param element target element
     * @return the corresponded representation
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<Type> toTypeVariables(TypeElement element) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        return toTypeVariables(element.getTypeParameters());
    }

    private List<Type> toTypeVariables(List<? extends TypeParameterElement> typeParameters) {
        List<Type> results = Lists.create();
        for (TypeParameterElement typeParameter : typeParameters) {
            SimpleName name = factory.newSimpleName(typeParameter.getSimpleName().toString());
            results.add(factory.newNamedType(name));
        }
        return results;
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
        List<AnnotationElement> members = Lists.create();
        members.add(factory.newAnnotationElement(
                factory.newSimpleName("name"),
                Models.toLiteral(factory, var.getName())));
        members.add(factory.newAnnotationElement(
                factory.newSimpleName("type"),
                factory.newClassLiteral(t(environment.getErasure(representation)))));
        if (var.getKind() == OperatorPortDeclaration.Kind.INPUT) {
            type = (NamedType) t(OperatorInfo.Input.class);
            members.add(factory.newAnnotationElement(
                    factory.newSimpleName("position"),
                    Models.toLiteral(factory, position)));
            String typeVariable = getTypeVariableName(representation);
            if (typeVariable != null) {
                members.add(factory.newAnnotationElement(
                        factory.newSimpleName("typeVariable"),
                        Models.toLiteral(factory, typeVariable)));
            }
        } else if (var.getKind() == OperatorPortDeclaration.Kind.OUTPUT) {
            type = (NamedType) t(OperatorInfo.Output.class);
            String typeVariable = getTypeVariableName(representation);
            if (typeVariable != null) {
                members.add(factory.newAnnotationElement(
                        factory.newSimpleName("typeVariable"),
                        Models.toLiteral(factory, typeVariable)));
            }
        } else if (var.getKind() == OperatorPortDeclaration.Kind.CONSTANT) {
            type = (NamedType) t(OperatorInfo.Parameter.class);
            members.add(factory.newAnnotationElement(
                    factory.newSimpleName("position"),
                    Models.toLiteral(factory, position)));
            String typeVariable = getTypeVariableNameInClass(representation);
            if (typeVariable != null) {
                members.add(factory.newAnnotationElement(
                        factory.newSimpleName("typeVariable"),
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
