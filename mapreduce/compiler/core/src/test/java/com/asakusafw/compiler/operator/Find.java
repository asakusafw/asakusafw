/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.VariableDeclarator;

/**
 * Utility of searching language elements.
 */
public class Find {

    /**
     * Returns all modifiers.
     * @param decl the member declaration
     * @return modifiers
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
     * Returns a method.
     * @param type the owner type
     * @param name the method name
     * @return the target method, or {@code null} if it is not found
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
     * Returns a field.
     * @param type the owner type
     * @param name the field name
     * @return the target field, or {@code null} if it is not found
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
     * Returns a constructor.
     * @param type the owner type
     * @return the target constructor, or {@code null} if it is not found
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
     * Returns a member type.
     * @param type the owner type
     * @param name the simple name
     * @return the target member type, or {@code null} if it is not found
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
