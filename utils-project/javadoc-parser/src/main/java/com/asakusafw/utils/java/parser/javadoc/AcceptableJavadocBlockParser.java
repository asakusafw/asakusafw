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
package com.asakusafw.utils.java.parser.javadoc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 必要なタグを選択して受け入れ可能なパーサの基底。
 */
public abstract class AcceptableJavadocBlockParser extends JavadocBlockParser {

    private Set<String> acceptable;

    /**
     * インスタンスを生成する。
     */
    public AcceptableJavadocBlockParser() {
        super();
        this.acceptable = Collections.emptySet();
    }

    /**
     * インスタンスを生成する。
     * @param tagName 処理可能なタグ名
     * @param tagNames 処理可能なタグ名の一覧
     */
    public AcceptableJavadocBlockParser(String tagName, String...tagNames) {
        super();
        this.acceptable = new HashSet<String>();
        this.acceptable.add(tagName);
        this.acceptable.addAll(Arrays.asList(tagNames));
    }

    /**
     * インスタンスを生成する。
     * @param tagNames 処理可能なタグ名の一覧
     */
    public AcceptableJavadocBlockParser(String[] tagNames) {
        super();
        this.acceptable = new HashSet<String>();
        this.acceptable.addAll(Arrays.asList(tagNames));
    }

    @Override
    public boolean canAccept(String tag) {
        return acceptable.contains(tag);
    }
}
