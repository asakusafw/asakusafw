/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

import java.io.DataInput;
import java.io.DataOutput;
import java.text.MessageFormat;
import java.util.Arrays;

import com.asakusafw.compiler.common.Precondition;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.util.CommentEmitTrait;
import com.ashigeru.lang.java.model.util.Models;

/**
 * エミッタが取り扱うデータの型。
 */
public interface DataClass {

    /**
     * このデータ型のJavaでの表現を返す。
     * @return このデータ型のJavaでの表現
     */
    java.lang.reflect.Type getType();

    /**
     * 指定の名前を持つプロパティを返す。
     * @param propertyName 対象のプロパティ名
     * @return 対象のプロパティを表すオブジェクト、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    Property findProperty(String propertyName);

    /**
     * このデータ型に対する新しいインスタンスを生成する式を返す。
     * @param type 対象のDOMでの表現
     * @return 新しいインスタンスを生成する式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    Expression createNewInstance(Type type);

    /**
     * この型のデータを代入する文を返す。
     * @param target 代入先
     * @param source 代入元
     * @return データを代入する文
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    Statement assign(Expression target, Expression source);

    /**
     * このデータオブジェクトの内容を消去する文を返す。
     * @param object 対象のオブジェクト
     * @return 対象の文
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    Statement reset(Expression object);

    /**
     * この型のデータを指定の{@link DataOutput}に書き出す文を返す。
     * @param object 書き出すオブジェクト
     * @param dataOutput {@link DataOutput}型の式
     * @return 指定の{@link DataOutput}に書き出す文
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    Statement createWriter(Expression object, Expression dataOutput);

    /**
     * この型のデータを指定の{@link DataInput}から読み出す式を返す。
     * @param object 書き出し先のオブジェクト
     * @param dataInput {@link DataInput}型の式
     * @return 指定の{@link DataInput}から読み出す式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    Statement createReader(Expression object, Expression dataInput);

    /**
     * 解決に失敗したことを表す{@link DataClass}の実装。
     */
    class Unresolved implements DataClass {

        private ModelFactory factory;

        private java.lang.reflect.Type runtimeType;

        /**
         * インスタンスを生成する。
         * @param factory ファクトリ
         * @param runtimeType 対象の型
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Unresolved(ModelFactory factory, java.lang.reflect.Type runtimeType) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(runtimeType, "runtimeType"); //$NON-NLS-1$
            this.factory = factory;
            this.runtimeType = runtimeType;
        }

        @Override
        public java.lang.reflect.Type getType() {
            return runtimeType;
        }

        @Override
        public Property findProperty(String propertyName) {
            return null;
        }

        @Override
        public Statement reset(Expression object) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"reset\": {0}",
                            runtimeType)
            )));
            return statement;
        }

        @Override
        public Expression createNewInstance(Type type) {
            Expression expression = Models.toNullLiteral(factory);
            expression.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"createNewInstance\": {0}",
                            runtimeType)
            )));
            return expression;
        }

        @Override
        public Statement assign(Expression target, Expression source) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"assign\": {0}",
                            runtimeType)
            )));
            return statement;
        }

        @Override
        public Statement createWriter(Expression object, Expression dataOutput) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"createWriter\": {0}",
                            runtimeType)
            )));
            return statement;
        }

        @Override
        public Statement createReader(Expression object, Expression dataInput) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"createReader\": {0}",
                            runtimeType)
            )));
            return statement;
        }
    }

    /**
     * それぞれのデータが有するプロパティ。
     */
    interface Property {

        /**
         * このプロパティの名前を返す。
         * @return 名前
         */
        String getName();

        /**
         * このプロパティのJavaでの型を返す。
         * @return Javaでの型
         */
        java.lang.reflect.Type getType();

        /**
         * このプロパティが{@code null}になりうる場合のみ{@code true}を返す。
         * @return {@code null}になりうる場合のみ{@code true}
         */
        boolean canNull();

        /**
         * このプロパティに対する新しいデータを生成する式を返す。
         * @param target 対象の型
         * @return 新しいデータを生成する式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Expression createNewInstance(Type target);

        /**
         * 指定のオブジェクトを表す式が有するこのプロパティが、{@code null}である場合に
         * {@code true}を返すような式を返す。
         * @param object オブジェクト
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Expression createIsNull(Expression object);

        /**
         * 指定のオブジェクトを表す式が有するこのプロパティをそのまま返す式を返す。
         * @param object オブジェクト
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Expression createGetter(Expression object);

        /**
         * この型のデータを代入する文を返す。
         * @param target 代入先
         * @param source 代入元
         * @return データを代入する文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Statement assign(Expression target, Expression source);

        /**
         * 指定のオブジェクトを表す式が有するこのプロパティを、
         * 指定の左辺式に設定する文を返す。
         * @param object オブジェクト
         * @param target 左辺式
         * @return 生成した文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Statement createGetter(Expression object, Expression target);

        /**
         * 指定のオブジェクトを表す式が有するこのプロパティに対し、
         * 指定の式を設定する文を返す。
         * @param object オブジェクト
         * @param value 設定する式
         * @return 生成した文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Statement createSetter(Expression object, Expression value);

        /**
         * 指定のオブジェクトを表す式が有するこのプロパティを、
         * 指定の{@code java.io.DataOutput}を表す式に出力する。
         * @param object オブジェクト
         * @param dataOutput {@code java.io.DataOutput}を表す式
         * @return 生成した文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Statement createWriter(Expression object, Expression dataOutput);

        /**
         * 指定のオブジェクトを表す式が有するこのプロパティに、
         * 指定の{@code java.io.DataInput}から読み出した値を設定する。
         * @param object オブジェクト
         * @param dataInput {@code java.io.DataInput}を表す式
         * @return 生成した文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Statement createReader(Expression object, Expression dataInput);

        /**
         * 指定のオブジェクトを表す式が有するこのプロパティに対する、
         * ハッシュコードを表す式を返す。
         * @param object オブジェクト
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Expression createHashCode(Expression object);

        /**
         * このプロパティと同じ型の値が格納されたバイト列から、その値の格納バイト数を計算する式を返す。
         * @param bytes 対象のバイト列を表す式
         * @param start バイト列内の開始位置を表す式
         * @param length 開始位置から見たバイト列の有効長を表す式
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Expression createBytesSize(
                Expression bytes,
                Expression start,
                Expression length);

        /**
         * このプロパティと同じ型の値が格納された二つのバイト列を、その値の部分だけ比較する式を返す。
         * @param bytes1 比較されるバイト列
         * @param start1 {@code bytes1}内の開始位置
         * @param length1 {@code bytes1}内の長さ
         * @param bytes2 比較するバイト列
         * @param start2 {@code bytes2}内の開始位置
         * @param length2 {@code bytes2}内の長さ
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Expression createBytesDiff(
                Expression bytes1, Expression start1, Expression length1,
                Expression bytes2, Expression start2, Expression length2);

        /**
         * このプロパティと同じ型を有する2つの値を比較する式を返す。
         * @param value1 比較される値を表す式
         * @param value2 比較する値を表す式
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        Expression createValueDiff(Expression value1, Expression value2);
    }
}
