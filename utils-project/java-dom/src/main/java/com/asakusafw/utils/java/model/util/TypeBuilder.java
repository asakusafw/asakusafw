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
package com.asakusafw.utils.java.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.ClassBody;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * 型に関連する要素を構築する。
 * <p>
 * このクラスのオブジェクトは、自身を破壊的に変更して型を構築する。
 * 特定の状態のビルダーを再利用する場合、{@link #copy()}を利用すること。
 * </p>
 */
public class TypeBuilder {

    private ModelFactory f;

    private Type context;

    /**
     * インスタンスを生成する。
     * @param factory 利用するファクトリ
     * @param context 対象の型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public TypeBuilder(ModelFactory factory, Type context) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.f = factory;
        this.context = context;
    }

    /**
     * 現在のビルダーと同等の内容を持つビルダーを新しく作成して返す。
     * @return コピーしたビルダー
     */
    public TypeBuilder copy() {
        return new TypeBuilder(f, context);
    }

    /**
     * このビルダーで構築した型を返す。
     * @return このビルダーで構築した型
     */
    public Type toType() {
        return context;
    }

    /**
     * このビルダーで構築した型を、名前型として返す。
     * @return このビルダーで構築した型
     * @throws IllegalStateException 構築した型が名前型でない場合
     */
    public NamedType toNamedType() {
        if (context.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalStateException("context type must be a named type"); //$NON-NLS-1$
        }
        return (NamedType) context;
    }

    /**
     * このビルダーで構築した型を、配列型として返す。
     * @return このビルダーで構築した型
     * @throws IllegalStateException 構築した型が配列型でない場合
     */
    public ArrayType toArrayType() {
        if (context.getModelKind() != ModelKind.ARRAY_TYPE) {
            throw new IllegalStateException("context type must be an array type"); //$NON-NLS-1$
        }
        return (ArrayType) context;
    }

    /**
     * このビルダーで構築した型に型引数を適用して返す。
     * @param typeArguments 型引数の一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public TypeBuilder parameterize(Type... typeArguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        return parameterize(Arrays.asList(typeArguments));
    }

    /**
     * このビルダーで構築した型に型引数を適用して返す。
     * @param typeArguments 型引数の一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public TypeBuilder parameterize(List<? extends Type> typeArguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (typeArguments.isEmpty()) {
            throw new IllegalArgumentException("typeArguments must have one or more elements"); //$NON-NLS-1$
        }
        return chain(f.newParameterizedType(context, typeArguments));
    }

    /**
     * このビルダーで構築した型に型引数を適用して返す。
     * @param typeArguments 型引数の一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public TypeBuilder parameterize(java.lang.reflect.Type... typeArguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        List<Type> args = new ArrayList<Type>();
        for (java.lang.reflect.Type type : typeArguments) {
            args.add(Models.toType(f, type));
        }
        return parameterize(args);
    }

    /**
     * このビルダーで構築した型を限定子として、さらに名前を続けた型を構築する。
     * @param name 続ける名前
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public TypeBuilder enclose(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (context.getModelKind() == ModelKind.NAMED_TYPE) {
            Name enclosed = Models.append(f, toNamedType().getName(), name);
            return chain(f.newNamedType(enclosed));
        }
        else {
            Type current = context;
            for (SimpleName segment : Models.toList(name)) {
                current = f.newQualifiedType(current, segment);
            }
            return chain(current);
        }
    }

    /**
     * このビルダーで構築した型を限定子として、さらに名前を続けた型を構築する。
     * @param name 続ける名前
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public TypeBuilder enclose(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return enclose(Models.toName(f, name));
    }

    /**
     * このビルダーで構築した型を要素型として、指定の次元数の配列型を構築する。
     * @param dimensions 次元数
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に負の値が指定された場合
     */
    public TypeBuilder array(int dimensions) {
        if (dimensions < 0) {
            throw new IllegalArgumentException("dimensions must be greater than or equal to 0"); //$NON-NLS-1$
        }
        Type current = context;
        for (int i = 0; i < dimensions; i++) {
            current = f.newArrayType(current);
        }
        return chain(current);
    }

    /**
     * このビルダーで構築した型に対するクラスリテラルを構築する。
     * @return 適用した結果をさらに操作するビルダー
     */
    public ExpressionBuilder dotClass() {
        return expr(f.newClassLiteral(context));
    }

    /**
     * このビルダーで構築した配列型に対する配列オブジェクトを構築する。
     * @param dimensions 次元ごとの要素数一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が配列型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder newArray(int... dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions must not be null"); //$NON-NLS-1$
        }
        List<Expression> exprs = new ArrayList<Expression>();
        for (int dim : dimensions) {
            exprs.add(Models.toLiteral(f, dim));
        }
        return newArray(exprs);
    }

    /**
     * このビルダーで構築した配列型に対する配列オブジェクトを構築する。
     * @param dimensions 次元ごとの要素数一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が配列型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder newArray(Expression... dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions must not be null"); //$NON-NLS-1$
        }
        return newArray(Arrays.asList(dimensions));
    }

    /**
     * このビルダーで構築した配列型に対する配列オブジェクトを構築する。
     * @param dimensions 次元ごとの要素数一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が配列型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder newArray(List<? extends Expression> dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions must not be null"); //$NON-NLS-1$
        }
        return expr(f.newArrayCreationExpression(
                toArrayType(),
                dimensions,
                null));
    }

    /**
     * このビルダーで構築した配列型に対する配列オブジェクトを構築する。
     * @param initializer 配列初期化子
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が配列型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder newArray(ArrayInitializer initializer) {
        if (initializer == null) {
            throw new IllegalArgumentException("initializer must not be null"); //$NON-NLS-1$
        }
        return expr(f.newArrayCreationExpression(
                toArrayType(),
                Collections.<Expression>emptyList(),
                initializer));
    }

    /**
     * このビルダーで構築した型に対するクラスインスタンスを構築する。
     * @param arguments コンストラクタの引数一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder newObject(Expression... arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return newObject(Arrays.asList(arguments), null);
    }

    /**
     * このビルダーで構築した型に対するクラスインスタンスを構築する。
     * @param arguments コンストラクタの引数一覧
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder newObject(List<? extends Expression> arguments) {
        return newObject(arguments, null);
    }

    /**
     * このビルダーで構築した型に対するクラスインスタンスを構築する。
     * @param arguments コンストラクタの引数一覧
     * @param anonymousClassBlock 匿名クラスブロック
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder newObject(
            List<? extends Expression> arguments,
            ClassBody anonymousClassBlock) {
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return expr(f.newClassInstanceCreationExpression(
                null,
                Collections.<Type>emptyList(),
                context,
                arguments,
                anonymousClassBlock));
    }

    /**
     * このビルダーで構築した型名を限定子に取るフィールド参照式を返す。
     * @param name 参照するフィールドの名前
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder field(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return field(f.newSimpleName(name));
    }

    /**
     * このビルダーで構築した型名を限定子に取るフィールド参照式を返す。
     * @param name 参照するフィールドの名前
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder field(SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return expr(f.newQualifiedName(toNamedType().getName(), name));
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            String name,
            Expression... arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(
                Collections.<Type>emptyList(),
                name,
                Arrays.asList(arguments));
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            String name,
            Expression... arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(
                typeArguments,
                name,
                Arrays.asList(arguments));
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            String name,
            List<? extends Expression> arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(
                Collections.<Type>emptyList(),
                name,
                arguments);
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            String name,
            List<? extends Expression> arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(
                typeArguments,
                f.newSimpleName(name),
                arguments);
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            SimpleName name,
            Expression... arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(
                Collections.<Type>emptyList(),
                name,
                Arrays.asList(arguments));
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            SimpleName name,
            Expression... arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(
                typeArguments,
                name,
                Arrays.asList(arguments));
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            SimpleName name,
            List<? extends Expression> arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(
                Collections.<Type>emptyList(),
                name,
                arguments);
    }

    /**
     * このビルダーで構築した型名を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
     * @throws IllegalStateException 構築した型が名前のみからなる型でない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            SimpleName name,
            List<? extends Expression> arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return expr(f.newMethodInvocationExpression(
                toNamedType().getName(),
                typeArguments,
                name,
                arguments));
    }

    private TypeBuilder chain(Type type) {
        assert type != null;
        this.context = type;
        return this;
    }

    private ExpressionBuilder expr(Expression expression) {
        assert expression != null;
        return new ExpressionBuilder(f, expression);
    }
}
