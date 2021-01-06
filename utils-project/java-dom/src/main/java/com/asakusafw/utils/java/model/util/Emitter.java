/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.IOException;
import java.io.PrintWriter;

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;

/**
 * An abstract super class of Java source code emitters.
 */
public abstract class Emitter {

    private static final String EXTENSION = ".java"; //$NON-NLS-1$

    private static final String PACKAGE_INFO = "package-info" + EXTENSION; //$NON-NLS-1$

    /**
     * Creates a new Java source file and returns the writer for writing the contents of the file.
     * @param unit the target compilation unit
     * @return the created writer
     * @throws IOException if error was occurred while creating the target source file
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public PrintWriter openFor(CompilationUnit unit) throws IOException {
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null"); //$NON-NLS-1$
        }
        TypeDeclaration primary = findPrimaryType(unit);
        if (primary == null) {
            return openFor(unit.getPackageDeclaration(), PACKAGE_INFO);
        }
        return openFor(unit.getPackageDeclaration(), primary);
    }

    /**
     * Returns a primary type declaration in the compilation unit.
     * Primary type is a type which is declared as top-level {@code public} type,
     * or the first type in the compilation unit.
     * @param unit the target compilation unit
     * @return the primary type declaration, or {@code null} if the compilation unit does not contain any types
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static TypeDeclaration findPrimaryType(CompilationUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null"); //$NON-NLS-1$
        }
        TypeDeclaration first = null;
        for (TypeDeclaration decl : unit.getTypeDeclarations()) {
            if (first == null) {
                first = decl;
            }
            for (Attribute attribute : decl.getModifiers()) {
                if (attribute instanceof Modifier) {
                    Modifier modifier = (Modifier) attribute;
                    if (modifier.getModifierKind() == ModifierKind.PUBLIC) {
                        return decl;
                    }
                }
            }
        }
        return first;
    }

    private PrintWriter openFor(PackageDeclaration packageDecl, TypeDeclaration typeDecl) throws IOException {
        assert typeDecl != null;
        String fileName = typeDecl.getName().getToken() + EXTENSION;
        return openFor(packageDecl, fileName);
    }

    /**
     * Creates a new resource file and returns the writer for writing the contents of the resource.
     * @param packageDeclOrNull the base package, or {@code null} if it is the default (unnamed) package
     * @param subPath the relative path from the base package
     * @return the created writer
     * @throws IOException if error was occurred while creating the target resource
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public abstract PrintWriter openFor(PackageDeclaration packageDeclOrNull, String subPath) throws IOException;
}
