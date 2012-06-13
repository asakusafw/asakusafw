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
package com.asakusafw.compiler.flow;

import java.util.LinkedList;
import java.util.regex.Pattern;

import com.asakusafw.compiler.common.Precondition;

/**
 * リソースの位置を表す。
 */
public class Location {

    /**
     * パス接頭辞を表現する場合の接尾辞を表す。
     */
    public static final String WILDCARD_SUFFIX = "-*";

    private final Location parent;

    private final String name;

    private boolean prefix;

    /**
     * インスタンスを生成する。
     * @param parent 親リソースの位置、ルートの場合は{@code null}
     * @param name リソースの名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location(Location parent, String name) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        if (parent != null && parent.isPrefix()) {
            throw new IllegalArgumentException();
        }
        this.parent = parent;
        this.name = name;
        this.prefix = false;
    }

    /**
     * この位置の末尾にワイルドカードを付与した位置を返す。
     * @return この位置の末尾にワイルドカードを付与した位置
     */
    public Location asPrefix() {
        Location copy = new Location(parent, name);
        copy.prefix = true;
        return copy;
    }

    /**
     * この位置がパスの接頭辞を表している場合のみ{@code true}を返す。
     * @return パスの接頭辞を表している場合のみ{@code true}
     */
    public boolean isPrefix() {
        return prefix;
    }

    /**
     * 親リソースの位置を返す。
     * @return 親リソースの位置、ルートの場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location getParent() {
        return parent;
    }

    /**
     * リソースの名前を返す。
     * @return リソースの名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public String getName() {
        return name;
    }

    /**
     * この位置の末尾に指定の名前を付与した位置を返す。
     * @param lastName 末尾の名前
     * @return 新しい位置
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location append(String lastName) {
        Precondition.checkMustNotBeNull(lastName, "lastName"); //$NON-NLS-1$
        return new Location(this, lastName);
    }

    /**
     * この位置の末尾に指定の相対パスを付与した位置を返す。
     * @param suffix 相対パス
     * @return 新しい位置
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location append(Location suffix) {
        Precondition.checkMustNotBeNull(suffix, "suffix"); //$NON-NLS-1$
        LinkedList<String> segments = new LinkedList<String>();
        Location current = suffix;
        while (current != null) {
            segments.addFirst(current.name);
            current = current.parent;
        }
        current = this;
        for (String segment : segments) {
            current = new Location(current, segment);
        }
        if (suffix.isPrefix()) {
            current = current.asPrefix();
        }
        return current;
    }

    /**
     * パス文字列を元にリソースの位置を構築して返す。
     * <p>
     * 区切り文字が先頭に来たり、区切り文字が連続しているなどして
     * リソースの名前が0文字になるような場合、それらのリソースの名前は無視される。
     * 結果として、全てのリソースの名前が0文字になる場合には、このメソッドは失敗する。
     * </p>
     * @param pathString パス文字列
     * @param separator 区切り文字
     * @return 生成した位置
     * @throws IllegalArgumentException 位置が不正である場合、
     *     または引数に{@code null}が指定された場合
     */
    public static Location fromPath(String pathString, char separator) {
        Precondition.checkMustNotBeNull(pathString, "pathString"); //$NON-NLS-1$
        boolean prefix = pathString.endsWith(WILDCARD_SUFFIX);
        String normalized = prefix
            ? pathString.substring(0, pathString.length() - WILDCARD_SUFFIX.length())
            : pathString;
        String[] segments = normalized.split(Pattern.quote(String.valueOf(separator)));
        Location current = null;
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            current = new Location(current, segment);
        }
        assert current != null;
        if (prefix) {
            current = current.asPrefix();
        }
        return current;
    }

    /**
     * このリソースの位置を、指定の区切り文字を挿入したパス文字列に変換して返す。
     * <p>
     * 先頭には区切り文字が挿入されない。
     * </p>
     * @param separator 区切り文字
     * @return パス文字列
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public String toPath(char separator) {
        LinkedList<String> segments = new LinkedList<String>();
        Location current = this;
        while (current != null) {
            segments.addFirst(current.name);
            current = current.parent;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(segments.removeFirst());
        for (String segment : segments) {
            buf.append(separator);
            buf.append(segment);
        }
        if (prefix) {
            buf.append(WILDCARD_SUFFIX);
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        Location current = this;
        result = prime * result + (prefix ? 0 : 1);
        while (current != null) {
            result = prime * result + current.name.hashCode();
            current = current.parent;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Location other = (Location) obj;
        Location thisCur = this;
        Location otherCur = other;
        if (thisCur.prefix != otherCur.prefix) {
            return false;
        }
        while (thisCur != null && otherCur != null) {
            if (thisCur == otherCur) {
                return true;
            }
            if (thisCur.name.equals(otherCur.name) == false) {
                return false;
            }
            thisCur = thisCur.parent;
            otherCur = otherCur.parent;
        }
        return thisCur == otherCur;
    }

    @Override
    public String toString() {
        return toPath('/');
    }
}
