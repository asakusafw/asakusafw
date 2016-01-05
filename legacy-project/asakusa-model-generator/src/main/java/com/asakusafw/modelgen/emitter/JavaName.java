/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.modelgen.emitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Javaで使用する名前。
 */
public class JavaName {

    private final List<String> words;

    JavaName(List<? extends String> words) {
        if (words == null) {
            throw new NullPointerException("words"); //$NON-NLS-1$
        }
        if (words.isEmpty()) {
            throw new IllegalArgumentException("words"); //$NON-NLS-1$
        }
        this.words = new ArrayList<String>();
        for (String word : words) {
            this.words.add(normalize(word));
        }
    }

    /**
     * 名前を表す文字列をこのオブジェクトに変換して返す。
     * @param nameString 対象の文字列
     * @return 対応するこのクラスのオブジェクト
     */
    public static JavaName of(String nameString) {
        if (nameString.indexOf('_') >= 0 || nameString.toUpperCase().equals(nameString)) {
            String[] segments = nameString.split("_");
            return new JavaName(normalize(Arrays.asList(segments)));
        } else {
            List<String> segments = new ArrayList<String>();
            int start = 0;
            for (int i = 1, n = nameString.length(); i < n; i++) {
                if (Character.isUpperCase(nameString.charAt(i))) {
                    segments.add(nameString.substring(start, i));
                    start = i;
                }
            }
            segments.add(nameString.substring(start));
            return new JavaName(normalize(segments));
        }
    }

    /**
     * この名前のセグメント一覧を、全て小文字で返す。
     * @return セグメント一覧
     */
    public List<String> getSegments() {
        return new ArrayList<String>(words);
    }

    /**
     * 型の名前と同様の形式に変換して返す ({@code CamelCase})。
     * @return 型の名前と同様の形式
     */
    public String toTypeName() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = words.size(); i < n; i++) {
            buf.append(capitalize(words.get(i)));
        }
        return buf.toString();
    }

    /**
     * メンバーの名前と同様の形式に変換して返す ({@code camelCase})。
     * @return メンバーの名前と同様の形式
     */
    public String toMemberName() {
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0).toLowerCase());
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append(capitalize(words.get(i)));
        }
        return buf.toString();
    }

    /**
     * 定数の名前と同様の形式に変換して返す ({@code UPPER_CASE})。
     * @return 定数の名前と同様の形式
     */
    public String toConstantName() {
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0).toUpperCase());
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append('_');
            buf.append(words.get(i).toUpperCase());
        }
        return buf.toString();
    }

    /**
     * この名前の先頭に指定のセグメントを追加する。
     * @param segment 追加するセグメント
     * @throws IllegalArgumentException 不正なセグメントが指定された場合
     */
    public void addFirst(String segment) {
        words.add(0, normalize(segment));
    }

    /**
     * この名前の末尾に指定のセグメントを追加する。
     * @param segment 追加するセグメント
     * @throws IllegalArgumentException 不正なセグメントが指定された場合
     */
    public void addLast(String segment) {
        words.add(normalize(segment));
    }

    private String capitalize(String segment) {
        assert segment != null;
        StringBuilder buf = new StringBuilder(segment.toLowerCase());
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        return buf.toString();
    }

    private static String normalize(String segment) {
        if (segment == null) {
            throw new IllegalArgumentException("segment must not be null"); //$NON-NLS-1$
        }
        if (segment.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return segment.toLowerCase();
    }

    private static List<String> normalize(List<String> segments) {
        List<String> results = new ArrayList<String>();
        for (String segment : segments) {
            if (segment.isEmpty() == false) {
                results.add(segment);
            }
        }
        return results;
    }
}
