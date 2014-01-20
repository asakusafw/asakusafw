/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.util;

import java.util.List;

/**
 * 出力に関するコンテキストオブジェクト。
 */
public interface EmitContext {

    /**
     * このコンテキストに登録されたすべてのコメントを出力する。
     */
    void flushComments();

    /**
     * このコンテキストに登録されたコメントのうち、指定の位置よりも手前にある(排他的)ものを出力する。
     * @param location 対象の位置
     */
    void flushComments(int location);

    /**
     * キーワードを出力する。
     * @param keyword 出力する文字列
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void keyword(String keyword);

    /**
     * シンボルを出力する。
     * @param symbol 出力する文字列
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void symbol(String symbol);

    /**
     * 通常の文字列を出力する。
     * @param immediate 出力する文字列
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void immediate(String immediate);

    /**
     * 演算子を出力する。
     * @param symbol 演算子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void operator(String symbol);

    /**
     * 区切り子を出力する。
     * @param symbol 区切り子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void separator(String symbol);

    /**
     * 強制的にパディングを挿入する。
     */
    void padding();

    /**
     * コメントを登録する。
     * <p>
     * 登録されたコメントは{@link #flushComments(int)}等を呼び出した際に
     * 必要に応じて出力される。
     * </p>
     * @param location コメントの位置
     * @param content コメントの内容
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void comment(int location, String content);

    /**
     * クラスブロックへの出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void classBlock(EmitDirection direction);

    /**
     * 配列初期化子ブロックへの出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void arrayInitializerBlock(EmitDirection direction);

    /**
     * 文としてのブロックへの出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void statementBlock(EmitDirection direction);

    /**
     * {@code switch}ラベルにぶら下がる文の列への出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void switchLabel(EmitDirection direction);

    /**
     * 文への出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void statement(EmitDirection direction);

    /**
     * 型やメンバ等の宣言への出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void declaration(EmitDirection direction);

    /**
     * クラスブロックへの出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void docComment(EmitDirection direction);

    /**
     * クラスブロックへの出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void docBlock(EmitDirection direction);

    /**
     * クラスブロックへの出入りを出力する。
     * @param direction 出入りの方向
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void docInlineBlock(EmitDirection direction);

    /**
     * ブロックコメントを追加する。
     * @param contents コメントの内容
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void putBlockComment(List<String> contents);

    /**
     * 行コメントを追加する。
     * @param content コメントの内容
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void putLineComment(String content);

    /**
     * 行内コメントを追加する。
     * @param content コメントの内容
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    void putInlineComment(String content);
}
