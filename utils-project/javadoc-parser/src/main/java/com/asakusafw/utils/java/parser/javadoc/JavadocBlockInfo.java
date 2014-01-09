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
package com.asakusafw.utils.java.parser.javadoc;

import java.text.MessageFormat;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;

/**
 * 内部ブロックの情報。
 * {@link JavadocBlockParserUtil#fetchBlockInfo(JavadocScanner)}の結果に利用される。
 */
public class JavadocBlockInfo {

    private IrLocation location;
    private String tagName;
    private JavadocScanner blockScanner;

    JavadocBlockInfo(String tagName, JavadocScanner blockScanner, IrLocation location) {
        super();
        this.tagName = tagName;
        this.blockScanner = blockScanner;
        this.location = location;
    }

    /**
     * ブロックのレンジを返す。
     * @return ブロックのレンジ
     */
    public IrLocation getLocation() {
        return this.location;
    }

    /**
     * タグ名を返す。
     * @return タグ名
     */
    public String getTagName() {
        return this.tagName;
    }

    /**
     * ブロックに対するスキャナを返す。
     * 返されるスキャナは、ブロック内のタグの次のトークンから、ブロック内部の終端トークンまでを保持する。
     * @return ブロックに対するスキャナ
     */
    public JavadocScanner getBlockScanner() {
        return this.blockScanner;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
            "'{'{0} {1}'}' ({2})", //$NON-NLS-1$
            getTagName(),
            getBlockScanner(),
            getLocation());
    }
}