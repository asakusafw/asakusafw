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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.io.Serializable;

/**
 * Javadocのトークン。
 */
public final class JavadocToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private final JavadocTokenKind kind;
    private final String text;
    private final int start;

    /**
     * インスタンスを生成する。
     * @param kind トークンの種類
     * @param text トークンを構成する文字列
     * @param start 開始位置
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocToken(JavadocTokenKind kind, String text, int start) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (text == null) {
            throw new IllegalArgumentException("text must not be null"); //$NON-NLS-1$
        }
        this.kind = kind;
        this.text = text;
        this.start = start;
    }

    /**
     * このトークンの種類を返す。
     * @return このトークンの種類
     */
    public JavadocTokenKind getKind() {
        return this.kind;
    }

    /**
     * このトークンを構成する文字列を返す。
     * @return このトークンを構成する文字列
     */
    public String getText() {
        return this.text;
    }

    /**
     * このトークンの開始オフセットを返す。
     * @return このトークンの開始オフセット
     */
    public int getStartPosition() {
        return this.start;
    }

    /**
     * このトークンの位置を返す。
     * @return このトークンの位置
     */
    public IrLocation getLocation() {
        return new IrLocation(getStartPosition(), getText().length());
    }

    @Override
    public String toString() {
        return getText();
    }
}
