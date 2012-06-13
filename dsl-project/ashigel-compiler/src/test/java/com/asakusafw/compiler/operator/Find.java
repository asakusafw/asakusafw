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
package com.asakusafw.compiler.operator;

import java.util.EnumSet;
import java.util.Set;

import com.ashigeru.lang.java.model.syntax.Attribute;
import com.ashigeru.lang.java.model.syntax.ConstructorDeclaration;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelKind;
import com.ashigeru.lang.java.model.syntax.Modifier;
import com.ashigeru.lang.java.model.syntax.ModifierKind;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeDeclaration;
import com.ashigeru.lang.java.model.syntax.VariableDeclarator;

/**
 * ツリー探索用のユーティリティ。
 */
public class Find {

    /**
     * 全ての修飾子を返す。
     * @param decl メンバ宣言
     * @return 全ての修飾子
     */
    public static Set<ModifierKind> modifiers(TypeBodyDeclaration decl) {
        Set<ModifierKind> results = EnumSet.noneOf(ModifierKind.class);
        for (Attribute attribute : decl.getModifiers()) {
            if (attribute.getModelKind() != ModelKind.MODIFIER) {
                continue;
            }
            Modifier modifier = (Modifier) attribute;
            results.add(modifier.getModifierKind());
        }
        return results;
    }

    /**
     * メソッドを探して返す。
     * @param type 対象の型
     * @param name メソッドの名前
     * @return 発見したメソッド、存在しない場合は{@code null}
     */
    public static MethodDeclaration method(TypeDeclaration type, String name) {
        for (TypeBodyDeclaration member : type.getBodyDeclarations()) {
            if (member.getModelKind() != ModelKind.METHOD_DECLARATION) {
                continue;
            }
            MethodDeclaration method = (MethodDeclaration) member;
            if (method.getName().getToken().equals(name)) {
                return method;
            }
        }
        return null;
    }

    /**
     * フィールドを探して返す。
     * @param type 対象の型
     * @param name フィールドの名前
     * @return 発見したフィールド、存在しない場合は{@code null}
     */
    public static FieldDeclaration field(TypeDeclaration type, String name) {
        for (TypeBodyDeclaration member : type.getBodyDeclarations()) {
            if (member.getModelKind() != ModelKind.FIELD_DECLARATION) {
                continue;
            }
            FieldDeclaration field = (FieldDeclaration) member;
            for (VariableDeclarator var : field.getVariableDeclarators()) {
                if (var.getName().getToken().equals(name)) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * コンストラクタを探して返す。
     * @param type 対象の型
     * @return 発見したコンストラクタ、存在しない場合は{@code null}
     */
    public static ConstructorDeclaration constructor(TypeDeclaration type) {
        for (TypeBodyDeclaration member : type.getBodyDeclarations()) {
            if (member.getModelKind() != ModelKind.CONSTRUCTOR_DECLARATION) {
                continue;
            }
            return (ConstructorDeclaration) member;
        }
        return null;
    }

    /**
     * 型を探して返す。
     * @param type 対象の型
     * @param name 型の名前
     * @return 発見した型、存在しない場合は{@code null}
     */
    public static TypeDeclaration type(TypeDeclaration type, String name) {
        for (TypeBodyDeclaration member : type.getBodyDeclarations()) {
            if ((member instanceof TypeDeclaration) == false) {
                continue;
            }
            TypeDeclaration inner = (TypeDeclaration) member;
            if (inner.getName().getToken().equals(name)) {
                return inner;
            }
        }
        return null;
    }
}
