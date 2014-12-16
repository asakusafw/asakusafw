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
package com.asakusafw.utils.java.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * モデルにコメント出力を付与するトレイト。
 */
public final class CommentEmitTrait {

    private static final String REGEX_LINE_DELIMITER = "\\n|\\r|\\r\\n"; //$NON-NLS-1$

    private List<String> contents;

    /**
     * インスタンスを生成する。
     * @param contents コメントの内容 (行区切りの文字列)
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public CommentEmitTrait(List<String> contents) {
        if (contents == null) {
            throw new IllegalArgumentException("contents must not be null"); //$NON-NLS-1$
        }
        this.contents = new ArrayList<String>(contents.size());
        for (String line: contents) {
            String[] splitted = line.split(REGEX_LINE_DELIMITER);
            for (String s : splitted) {
                this.contents.add(s);
            }
        }
        this.contents = Collections.unmodifiableList(this.contents);
    }

    /**
     * コメントの内容を返す。
     * @return コメントの内容
     */
    public List<String> getContents() {
        return this.contents;
    }
}
