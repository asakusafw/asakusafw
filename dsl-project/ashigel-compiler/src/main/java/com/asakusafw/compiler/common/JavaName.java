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
package com.asakusafw.compiler.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;

/**
 * Javaで使用する名前。
 */
public class JavaName {

    private static final Set<String> RESERVED;
    static {
        // see http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.9
        Set<String> set = Sets.create();
        set.add("abstract");
        set.add("continue");
        set.add("for");
        set.add("new");
        set.add("switch");
        set.add("assert");
        set.add("default");
        set.add("if");
        set.add("package");
        set.add("synchronized");
        set.add("boolean");
        set.add("do");
        set.add("goto");
        set.add("private");
        set.add("this");
        set.add("break");
        set.add("double");
        set.add("implements");
        set.add("protected");
        set.add("throw");
        set.add("byte");
        set.add("else");
        set.add("import");
        set.add("public");
        set.add("throws");
        set.add("case");
        set.add("enum");
        set.add("instanceof");
        set.add("return");
        set.add("transient");
        set.add("catch");
        set.add("extends");
        set.add("int");
        set.add("short");
        set.add("try");
        set.add("char");
        set.add("final");
        set.add("interface");
        set.add("static");
        set.add("void");
        set.add("class");
        set.add("finally");
        set.add("long");
        set.add("strictfp");
        set.add("volatile");
        set.add("const");
        set.add("float");
        set.add("native");
        set.add("super");
        set.add("while");
        RESERVED = Collections.unmodifiableSet(set);
    }

    private static final String EMPTY_NAME = "_";

    private final List<String> words;

    JavaName(List<? extends String> words) {
        if (words == null) {
            throw new NullPointerException("words"); //$NON-NLS-1$
        }
        this.words = Lists.create();
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
        if (nameString.isEmpty()) {
            throw new IllegalArgumentException("nameString must not be empty"); //$NON-NLS-1$
        } else if (nameString.indexOf('_') >= 0 || nameString.toUpperCase().equals(nameString)) {
            String[] segments = nameString.split(EMPTY_NAME);
            return new JavaName(normalize(Arrays.asList(segments)));
        } else {
            List<String> segments = Lists.create();
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
        return Lists.from(words);
    }

    /**
     * 型の名前と同様の形式に変換して返す ({@code CamelCase})。
     * @return 型の名前と同様の形式
     */
    public String toTypeName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
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
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(words.get(0).toLowerCase());
        for (int i = 1, n = words.size(); i < n; i++) {
            buf.append(capitalize(words.get(i)));
        }
        String result = buf.toString();
        if (RESERVED.contains(result) || Character.isJavaIdentifierStart(result.charAt(0)) == false) {
            return escape(result);
        }
        return result;
    }

    private String escape(String result) {
        assert result != null;
        return result + '_';
    }

    /**
     * 定数の名前と同様の形式に変換して返す ({@code UPPER_CASE})。
     * @return 定数の名前と同様の形式
     */
    public String toConstantName() {
        if (words.isEmpty()) {
            return EMPTY_NAME;
        }
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
     * この名前の先頭のセグメントを削除する。
     * @throws IllegalStateException この名前が空の場合
     */
    public void removeFirst() {
        if (words.isEmpty()) {
            throw new IllegalStateException();
        }
        words.remove(0);
    }

    /**
     * この名前の末尾に指定のセグメントを追加する。
     * @param segment 追加するセグメント
     * @throws IllegalArgumentException 不正なセグメントが指定された場合
     */
    public void addLast(String segment) {
        words.add(normalize(segment));
    }

    /**
     * この名前の末尾のセグメントを削除する。
     * @throws IllegalStateException この名前が空の場合
     */
    public void removeLast() {
        if (words.isEmpty()) {
            throw new IllegalStateException();
        }
        words.remove(words.size() - 1);
    }

    private String capitalize(String segment) {
        assert segment != null;
        StringBuilder buf = new StringBuilder(segment.toLowerCase());
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        return buf.toString();
    }

    private static String normalize(String segment) {
        Precondition.checkMustNotBeNull(segment, "segment"); //$NON-NLS-1$
        if (segment.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return segment.toLowerCase();
    }

    private static List<String> normalize(List<String> segments) {
        List<String> results = Lists.create();
        for (String segment : segments) {
            if (segment.isEmpty() == false) {
                results.add(segment);
            }
        }
        return results;
    }
}
