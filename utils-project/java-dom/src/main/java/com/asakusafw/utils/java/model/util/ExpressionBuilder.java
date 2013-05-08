/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.LocalVariableDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.PostfixOperator;
import com.asakusafw.utils.java.model.syntax.ReturnStatement;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.ThrowStatement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.UnaryOperator;

/**
 * 式を構築するビルダー。
 * <p>
 * このクラスのオブジェクトは、自身を破壊的に変更して式を構築する。
 * 特定の状態のビルダーを再利用する場合、{@link #copy()}を利用すること。
 * </p>
 */
public class ExpressionBuilder {

    private ModelFactory f;

    private Expression context;

    /**
     * インスタンスを生成する。
     * @param factory モデルの構築に利用するファクトリー
     * @param context 文脈として利用する式
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder(ModelFactory factory, Expression context) {
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
    public ExpressionBuilder copy() {
        return new ExpressionBuilder(f, context);
    }

    /**
     * このビルダーで構築した式を返す。
     * @return このビルダーで構築した式
     */
    public Expression toExpression() {
        return context;
    }

    /**
     * このビルダーで構築した式を式文に変換して返す。
     * @return このビルダーで構築した式の式文
     */
    public ExpressionStatement toStatement() {
        return f.newExpressionStatement(toExpression());
    }

    /**
     * このビルダーで構築した式を{@code throw}文に変換して返す。
     * @return このビルダーで構築した式をスローする{@code throw}文
     */
    public ThrowStatement toThrowStatement() {
        return f.newThrowStatement(toExpression());
    }

    /**
     * このビルダーで構築した式を{@code return}文に変換して返す。
     * @return このビルダーで構築した式を返す{@code return}文
     */
    public ReturnStatement toReturnStatement() {
        return f.newReturnStatement(toExpression());
    }

    /**
     * このビルダーで構築した式を初期化式に持つローカル変数宣言を生成して返す。
     * @param type ローカル変数宣言の式
     * @param name 生成するローカル変数の名前
     * @return 生成したローカル変数宣言
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public LocalVariableDeclaration toLocalVariableDeclaration(
            Type type,
            String name) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return toLocalVariableDeclaration(type, f.newSimpleName(name));
    }

    /**
     * このビルダーで構築した式を初期化式に持つローカル変数宣言を生成して返す。
     * @param type ローカル変数宣言の式
     * @param name 生成するローカル変数の名前
     * @return 生成したローカル変数宣言
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public LocalVariableDeclaration toLocalVariableDeclaration(
            Type type,
            SimpleName name) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return f.newLocalVariableDeclaration(type, name, context);
    }

    /**
     * このビルダーで構築した式に中値演算を適用して返す。
     * @param operator 演算子
     * @param right 右項
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder apply(InfixOperator operator, Expression right) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        if (right == null) {
            throw new IllegalArgumentException("right must not be null"); //$NON-NLS-1$
        }
        return chain(f.newInfixExpression(context, operator, right));
    }

    /**
     * このビルダーで構築した式に前置演算を適用して返す。
     * @param operator 演算子
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder apply(UnaryOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        return chain(f.newUnaryExpression(operator, context));
    }

    /**
     * このビルダーで構築した式に後置演算を適用して返す。
     * @param operator 演算子
     * @return 適用した結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder apply(PostfixOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        return chain(f.newPostfixExpression(context, operator));
    }

    /**
     * このビルダーで構築した式を変数とみなし、指定の右辺式を代入する式を返す。
     * @param rightHandSide 右辺式
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder assignFrom(Expression rightHandSide) {
        if (rightHandSide == null) {
            throw new IllegalArgumentException("rightHandSide must not be null"); //$NON-NLS-1$
        }
        return assignFrom(InfixOperator.ASSIGN, rightHandSide);
    }

    /**
     * このビルダーで構築した式を変数とみなし、指定の右辺式を代入する式を返す。
     * @param operator 複合代入演算子
     * @param rightHandSide 右辺式
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder assignFrom(
            InfixOperator operator,
            Expression rightHandSide) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        if (rightHandSide == null) {
            throw new IllegalArgumentException("rightHandSide must not be null"); //$NON-NLS-1$
        }
        return chain(f.newAssignmentExpression(
                context,
                operator,
                rightHandSide));
    }

    /**
     * このビルダーで構築した式を指定の型に変換して返す。
     * @param type 変換先の型
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder castTo(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return chain(f.newCastExpression(
                type,
                context));
    }

    /**
     * このビルダーで構築した式を指定の型に変換して返す。
     * @param type 変換先の型
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder castTo(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return castTo(Models.toType(f, type));
    }

    /**
     * このビルダーで構築した式に{@code instanceof}演算を適用する式を返す。
     * @param type 変換先の型
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder instanceOf(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return chain(f.newInstanceofExpression(context, type));
    }

    /**
     * このビルダーで構築した式に{@code instanceof}演算を適用する式を返す。
     * @param type 変換先の型
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder instanceOf(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return instanceOf(Models.toType(f, type));
    }

    /**
     * このビルダーで構築した式に、フィールド参照を適用した式を返す。
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
     * このビルダーで構築した式に、フィールド参照を適用した式を返す。
     * @param name 参照するフィールドの名前
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder field(SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return chain(f.newFieldAccessExpression(context, name));
    }

    /**
     * このビルダーで構築した式を配列式とみなし、それに配列参照を適用した式を返す。
     * @param index 配列の添え字
     * @return 結果をさらに操作するビルダー
     */
    public ExpressionBuilder array(int index) {
        return array(Models.toLiteral(f, index));
    }

    /**
     * このビルダーで構築した式を配列式とみなし、それに配列参照を適用した式を返す。
     * @param index 配列の添え字をあらわす式名
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder array(String index) {
        if (index == null) {
            throw new IllegalArgumentException("index must not be null"); //$NON-NLS-1$
        }
        return array(Models.toName(f, index));
    }

    /**
     * このビルダーで構築した式を配列式とみなし、それに配列参照を適用した式を返す。
     * @param index 配列の添え字式
     * @return 結果をさらに操作するビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ExpressionBuilder array(Expression index) {
        if (index == null) {
            throw new IllegalArgumentException("index must not be null"); //$NON-NLS-1$
        }
        return chain(f.newArrayAccessExpression(context, index));
    }

    /**
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
     * このビルダーで構築した式を限定子に取るメソッド起動式を返す。
     * @param typeArguments 型変数の一覧
     * @param name 起動するメソッドの名前
     * @param arguments 起動引数の一覧
     * @return 結果をさらに操作するビルダー
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
        return chain(f.newMethodInvocationExpression(
                context,
                typeArguments,
                name,
                arguments));
    }

    private ExpressionBuilder chain(Expression expression) {
        assert expression != null;
        context = expression;
        return this;
    }
}
