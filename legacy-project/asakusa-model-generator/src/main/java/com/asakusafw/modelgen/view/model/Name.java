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
package com.asakusafw.modelgen.view.model;

/**
 * 任意の名前。
 */
public class Name {

    /**
     * 名前を表すトークン。
     */
    public final String token;

    /**
     * インスタンスを生成する。
     * @param token 名前を表すトークン
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name(String token) {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null"); //$NON-NLS-1$
        }
        this.token = token;
    }

    /**
     * インスタンスを生成する。
     * @param qualifier 限定子
     * @param rest 名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Name(Name qualifier, Name rest) {
        if (qualifier == null) {
            throw new IllegalArgumentException("qualifier must not be null"); //$NON-NLS-1$
        }
        if (rest == null) {
            throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
        }
        this.token = qualifier.token + "." + rest.token;
    }

    /**
     * この名前の末尾のセグメントを返す。
     * @return この名前の末尾のセグメント
     */
    public Name getLastSegment() {
        int last = token.lastIndexOf('.');
        if (last >= 0) {
            return new Name(token.substring(last + 1));
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + token.hashCode();
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
        Name other = (Name) obj;
        if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return token;
    }
}
