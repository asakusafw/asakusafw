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

import java.io.IOException;
import java.io.PrintWriter;

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;

/**
 * ソースコードを出力するための出力を作成する。
 */
public abstract class Emitter {

    private static final String EXTENSION = ".java"; //$NON-NLS-1$

    private static final String PACKAGE_INFO = "package-info" + EXTENSION; //$NON-NLS-1$

    /**
     * 指定のコンパイル単位に書き出すためのライターを返す。
     * @param unit 対象のコンパイル単位
     * @return 開いたライター
     * @throws IOException ファイルの作成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 指定のコンパイル単位に含まれる、ファイル名に利用できそうな型の宣言を返す。
     * <p>
     * 型の宣言が指定のコンパイル単位に含まれない場合、このメソッドは{@code null}を返す。
     * </p>
     * @param unit 対象のコンパイル単位
     * @return ファイル名に利用できそうな型の宣言、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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

    private PrintWriter openFor(
            PackageDeclaration packageDecl,
            TypeDeclaration typeDecl) throws IOException {
        assert typeDecl != null;
        String fileName = typeDecl.getName().getToken() + EXTENSION;
        return openFor(packageDecl, fileName);
    }

    /**
     * 指定のパッケージに指定のサブパスのファイルを作成するためのライターを返す。
     * @param packageDeclOrNull 対象のパッケージ宣言、無名パッケージの場合は{@code null}
     * @param subPath パッケージ下のサブパス
     * @return 開いたライター
     * @throws IOException ファイルの作成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public abstract PrintWriter openFor(
            PackageDeclaration packageDeclOrNull,
            String subPath) throws IOException;
}
