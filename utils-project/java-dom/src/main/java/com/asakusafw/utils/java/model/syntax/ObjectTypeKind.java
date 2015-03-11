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
package com.asakusafw.utils.java.model.syntax;

/**
 * オブジェクト型の種類。
 */
public enum ObjectTypeKind implements DeclarationKind {

    /**
     * 通常の(列挙でない)クラス型。
     */
    CLASS(true),

    /**
     * 通常の(注釈でない)インターフェース型。
     */
    INTERFACE(false),

    /**
     * 列挙型。
     */
    ENUM(true),

    /**
     * 注釈型。
     */
    ANNOTATION(false),
    ;

    private final boolean classLike;

    /**
     * インスタンスを生成する。
     * @param classLike クラスに分類される
     */
    private ObjectTypeKind(boolean classLike) {
        this.classLike = classLike;
    }

    /**
     * この種類がクラス型に分類される場合のみ{@code true}を返す。
     * @return この種類がクラス型に分類される場合に{@code true}、そうでない場合は{@code false}
     */
    public boolean isClassLike() {
        return classLike;
    }
}
