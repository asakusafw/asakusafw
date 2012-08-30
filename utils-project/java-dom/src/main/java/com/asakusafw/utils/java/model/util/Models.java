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

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.java.internal.model.syntax.ModelFactoryImpl;
import com.asakusafw.utils.java.internal.model.util.LiteralAnalyzer;
import com.asakusafw.utils.java.internal.model.util.ModelEmitter;
import com.asakusafw.utils.java.internal.model.util.ReflectionTypeMapper;
import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.Model;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * {@link Model}に関するユーティリティ群。
 */
public final class Models {

    private static final Map<Class<?>, BasicTypeKind> WRAPPER_TYPE_KINDS;
    static {
        Map<Class<?>, BasicTypeKind> map = new HashMap<Class<?>, BasicTypeKind>();
        map.put(Byte.class, BasicTypeKind.BYTE);
        map.put(Short.class, BasicTypeKind.SHORT);
        map.put(Integer.class, BasicTypeKind.INT);
        map.put(Long.class, BasicTypeKind.LONG);
        map.put(Float.class, BasicTypeKind.FLOAT);
        map.put(Double.class, BasicTypeKind.DOUBLE);
        map.put(Character.class, BasicTypeKind.CHAR);
        map.put(Boolean.class, BasicTypeKind.BOOLEAN);
        WRAPPER_TYPE_KINDS = map;
    }

    /**
     * {@link Model}の実装を生成するためのファクトリを返す。
     * @return {@link Model}の実装を生成するためのファクトリ
     */
    public static ModelFactory getModelFactory() {
        return new ModelFactoryImpl();
    }

    /**
     * 指定の名前を単純名のリストに変換して返す。
     * <p>
     * 返されるリストは、表記と同様の順序に整列される。
     * </p>
     * @param name 変換する名前
     * @return 変換後の名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static List<SimpleName> toList(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        ModelKind kind = name.getModelKind();
        if (kind == ModelKind.SIMPLE_NAME) {
            return Collections.singletonList((SimpleName) name);
        } else {
            LinkedList<SimpleName> result = new LinkedList<SimpleName>();
            Name current = name;
            do {
                QualifiedName qname = (QualifiedName) current;
                result.addFirst(qname.getSimpleName());
                current = qname.getQualifier();
            } while (current.getModelKind() == ModelKind.QUALIFIED_NAME);

            assert current.getModelKind() == ModelKind.SIMPLE_NAME;
            result.addFirst((SimpleName) current);

            return result;
        }
    }

    /**
     * 指定の名前の末尾に、指定の文字列を名前とみなして末尾に結合して返す。
     * <p>
     * 指定された文字列が限定名を表現する場合、限定名とみなして結合する。
     * </p>
     * @param factory 利用するファクトリ
     * @param prefix 結合される名前の先頭の名前
     * @param rest 末尾に結合される名前を表す文字列
     * @return 結合された名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Name append(ModelFactory factory, Name prefix, String rest) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        if (rest == null) {
            throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
        }
        Name name = Models.toName(factory, rest);
        return append(factory, prefix, name);
    }

    /**
     * 指定された名前の一覧を順に結合して返す。
     * @param factory 利用するファクトリ
     * @param names 結合される名前のリスト
     * @return 結合された名前
     * @throws IllegalArgumentException 名前が指定されない場合、
     *     または引数に{@code null}が含まれる場合
     */
    public static Name append(ModelFactory factory, Name... names) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (names == null) {
            throw new IllegalArgumentException("names must not be null"); //$NON-NLS-1$
        }
        if (names.length == 0) {
            throw new IllegalArgumentException("names must have elements"); //$NON-NLS-1$
        }
        if (names.length == 1) {
            return names[0];
        }
        Name current = names[0];
        for (int i = 1; i < names.length; i++) {
            for (SimpleName segment : toList(names[i])) {
                current = factory.newQualifiedName(current, segment);
            }
        }
        return current;
    }

    /**
     * モデルを指定の出力先に文字列として書き出す。
     * <p>
     * 対象のモデルおよびそれに含まれるモデルのいずれかが
     * {@link CommentEmitTrait}を{@link Model#findModelTrait(Class)
     * 所有している}場合、それらのモデルはアダプタが指定するコメントをモデルの出力の前に出力する。
     * ただし、一部のコメントはモデルの途中で出力される場合がある。
     * </p>
     * @param model 対象のモデル
     * @param writer 出力先
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static void emit(Model model, PrintWriter writer) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        ModelEmitter emitter = new ModelEmitter(writer);
        emitter.emit(model);
    }

    /**
     * リフレクションAPIの型の表現を、モデルの型の表現に変換して返す。
     * @param factory 利用するファクトリ
     * @param type 変換対象の型
     * @return 変換後のモデル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Type toType(ModelFactory factory, java.lang.reflect.Type type) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return new ReflectionTypeMapper().dispatch(type, factory);
    }

    /**
     * 指定の文字列を名前の構造に変換する。
     * <p>
     * 名前として正しいかどうかについては検証を行わない。
     * </p>
     * @param factory 利用するファクトリ
     * @param nameString 変換対象の文字列
     * @return 変換後の名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Name toName(ModelFactory factory, String nameString) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (nameString == null) {
            throw new IllegalArgumentException("nameString must not be null"); //$NON-NLS-1$
        }
        String[] segments = nameString.trim().split("\\s*\\.\\s*"); //$NON-NLS-1$
        if (segments.length == 0 || segments[0].length() == 0) {
            throw new IllegalArgumentException("nameString is empty"); //$NON-NLS-1$
        }
        Name left = factory.newSimpleName(segments[0]);
        for (int i = 1; i < segments.length; i++) {
            SimpleName right = factory.newSimpleName(segments[i]);
            left = factory.newQualifiedName(left, right);
        }
        return left;
    }

    /**
     * 指定の定数を完全限定名に変換する。
     * @param factory 利用するファクトリ
     * @param constant 定数
     * @return 変換後の名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Name toName(ModelFactory factory, Enum<?> constant) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (constant == null) {
            throw new IllegalArgumentException("constant must not be null"); //$NON-NLS-1$
        }
        Name typeName = toName(factory, constant.getDeclaringClass().getName());
        return factory.newQualifiedName(
                typeName,
                factory.newSimpleName(constant.name()));
    }

    /**
     * 指定の値をキャストしたリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Expression toLiteral(ModelFactory factory, byte value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.intLiteralOf(value);
        return factory.newCastExpression(
            factory.newBasicType(BasicTypeKind.BYTE),
            factory.newLiteral(token));
    }

    /**
     * 指定の値をキャストしたリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Expression toLiteral(ModelFactory factory, short value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.intLiteralOf(value);
        return factory.newCastExpression(
            factory.newBasicType(BasicTypeKind.SHORT),
            factory.newLiteral(token));
    }

    /**
     * 指定の値をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toLiteral(ModelFactory factory, int value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.intLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * 指定の値をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toLiteral(ModelFactory factory, long value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.longLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * 指定の値をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toLiteral(ModelFactory factory, float value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.floatLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * 指定の値をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toLiteral(ModelFactory factory, double value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.doubleLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * 指定の値をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toLiteral(ModelFactory factory, boolean value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.booleanLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * 指定の値をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toLiteral(ModelFactory factory, char value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.charLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * 指定の値をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param value 値
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toLiteral(ModelFactory factory, String value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.stringLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * 指定の値がプリミティブのラッパー型、{@code null}、
     * {@code String}、{@code java.lang.reflect.Type}のいずれかである場合に、
     * そのリテラル表現を返す。
     * @param factory 利用するファクトリ
     * @param value 変換する値
     * @return 変換後の表現
     * @throws IllegalArgumentException 変換できなかった場合、または引数に{@code null}が指定された場合
     */
    public static Expression toLiteral(ModelFactory factory, Object value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            return toNullLiteral(factory);
        }
        Class<? extends Object> valueClass = value.getClass();
        BasicTypeKind kind = WRAPPER_TYPE_KINDS.get(valueClass);
        if (kind != null) {
            switch (kind) {
            case BYTE:
                return toLiteral(factory, (byte) (Byte) value);
            case SHORT:
                return toLiteral(factory, (short) (Short) value);
            case INT:
                return toLiteral(factory, (int) (Integer) value);
            case LONG:
                return toLiteral(factory, (long) (Long) value);
            case FLOAT:
                return toLiteral(factory, (float) (Float) value);
            case DOUBLE:
                return toLiteral(factory, (double) (Double) value);
            case CHAR:
                return toLiteral(factory, (char) (Character) value);
            case BOOLEAN:
                return toLiteral(factory, (boolean) (Boolean) value);
            default:
                throw new AssertionError(kind);
            }
        } else if (valueClass == String.class) {
            return toLiteral(factory, (String) value);
        } else if (value instanceof java.lang.reflect.Type) {
            return toClassLiteral(factory, (java.lang.reflect.Type) value);
        }
        throw new IllegalArgumentException(MessageFormat.format(
                "Cannot convert {0} to literal ({1})",
                value,
                valueClass));
    }

    /**
     * 指定の型をリテラルに変換する。
     * @param factory 利用するファクトリ
     * @param type 型
     * @return 変換後のリテラル
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ClassLiteral toClassLiteral(
            ModelFactory factory,
            java.lang.reflect.Type type) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return factory.newClassLiteral(Models.toType(factory, type));
    }

    /**
     * {@code null}リテラルを返す。
     * @param factory 利用するファクトリ
     * @return 変換後の名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static Literal toNullLiteral(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.nullLiteral();
        return factory.newLiteral(token);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            int[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (int value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            float[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (float value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            long[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (long value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            double[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (double value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            char[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (char value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            boolean[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (boolean value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            byte[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (byte value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            short[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (short value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            String[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (String value : array) {
            if (value == null) {
                literals.add(Models.toNullLiteral(factory));
            } else {
                literals.add(Models.toLiteral(factory, value));
            }
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * 指定の配列を配列初期化子に変換して返す。
     * @param factory 利用するファクトリ
     * @param array 変換する配列
     * @return 変換後の配列初期化子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static ArrayInitializer toArrayInitializer(
            ModelFactory factory,
            java.lang.reflect.Type[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<Expression>();
        for (java.lang.reflect.Type value : array) {
            if (value == null) {
                literals.add(Models.toNullLiteral(factory));
            } else {
                literals.add(Models.toClassLiteral(factory, value));
            }
        }
        return factory.newArrayInitializer(literals);
    }

    private Models() {
        throw new AssertionError();
    }
}
