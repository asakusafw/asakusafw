/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;

/**
 * A java file object implementation of {@link CompilationUnit}.
 */
public class CompilationUnitJavaFile extends SimpleJavaFileObject {

    /**
     * The scheme name.
     */
    public static final String URI_SCHEME = CompilationUnitJavaFile.class.getName();

    private final CompilationUnit unit;

    /**
     * Creates a new instance.
     * @param unit the target compilation unit
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public CompilationUnitJavaFile(CompilationUnit unit) {
        super(toUri(unit), JavaFileObject.Kind.SOURCE);
        this.unit = unit;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return unit.toString();
    }

    private static URI toUri(CompilationUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null"); //$NON-NLS-1$
        }
        String path = toPath(unit);
        try {
            return new URI(URI_SCHEME, null, "/" + path, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String toPath(CompilationUnit unit) {
        assert unit != null;
        StringBuilder buf = new StringBuilder();
        PackageDeclaration packageDeclaration = unit.getPackageDeclaration();
        if (packageDeclaration != null) {
            String pkg = packageDeclaration.getName().toNameString().replace('.', '/');
            buf.append(pkg);
            buf.append('/');
        }
        TypeDeclaration primaryType = findPrimaryType(unit);
        if (primaryType != null) {
            buf.append(primaryType.getName().toNameString());
        } else {
            buf.append("package-info");
        }
        buf.append(".java");
        return buf.toString();
    }

    private static TypeDeclaration findPrimaryType(CompilationUnit unit) {
        assert unit != null;
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
}
