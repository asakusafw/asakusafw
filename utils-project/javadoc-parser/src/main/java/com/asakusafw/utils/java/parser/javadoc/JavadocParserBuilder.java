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
package com.asakusafw.utils.java.parser.javadoc;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link JavadocParser}を生成する。
 */
public class JavadocParserBuilder {

    private boolean generated;
    private List<JavadocBlockParser> inlines;
    private List<JavadocBlockParser> toplevels;

    /**
     * インスタンスを生成する。
     */
    public JavadocParserBuilder() {
        super();
        this.generated = false;
        this.inlines = new ArrayList<JavadocBlockParser>();
        this.toplevels = new ArrayList<JavadocBlockParser>();
    }

    /**
     * 特別な処理を行うインラインブロックの解析器を追加する。
     * このメソッドによって先に追加された解析器より優先度は低くなる。
     * @param parser 追加する解析器
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @throws IllegalStateException {@link #build()}を呼び出した後にこのメソッドが呼び出された場合
     */
    public synchronized void addSpecialInlineBlockParser(JavadocBlockParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser"); //$NON-NLS-1$
        }
        if (generated) {
            throw new IllegalStateException();
        }
        inlines.add(parser);
    }

    /**
     * 特別な処理を行うトップレベルブロックの解析器を追加する。
     * このメソッドによって先に追加された解析器より優先度は低くなる。
     * @param parser 追加する解析器
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @throws IllegalStateException {@link #build()}を呼び出した後にこのメソッドが呼び出された場合
     */
    public synchronized void addSpecialStandAloneBlockParser(JavadocBlockParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser"); //$NON-NLS-1$
        }
        if (generated) {
            throw new IllegalStateException();
        }
        toplevels.add(parser);
    }

    /**
     * ここまでの設定を利用して{@link JavadocParser}を作成し、返す。
     * @return 作成した解析器
     * @throws IllegalStateException このメソッドが同一インスタンスに対して二回以上呼び出された場合
     */
    public synchronized JavadocParser build() {
        if (generated) {
            throw new IllegalStateException();
        }
        generated = true;

        // 任意のインラインブロックを処理できるように
        inlines.add(new DefaultJavadocBlockParser());

        // トップレベルブロックがインラインブロックを処理できるように
        for (JavadocBlockParser p: toplevels) {
            p.setBlockParsers(inlines);
        }
        // 任意のトップレベルブロックを処理できるように
        toplevels.add(new DefaultJavadocBlockParser(inlines));

        // トップレベルブロックを処理できるように
        JavadocParser parser = new JavadocParser(toplevels);
        return parser;
    }
}
