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
package com.asakusafw.utils.java.model.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.SimpleName;

/**
 * ソースコードを出力するためのファイルを作成する。
 */
public class Filer extends Emitter {

    private final File outputPath;

    private final Charset encoding;

    /**
     * インスタンスを生成する。
     * @param outputPath 出力先のパス
     * @param encoding エンコーディング
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Filer(File outputPath, Charset encoding) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null"); //$NON-NLS-1$
        }
        this.outputPath = outputPath;
        this.encoding = encoding;
    }

    /**
     * 指定のパッケージ宣言に関連するフォルダへのパスを返す。
     * <p>
     * 返されたパスにフォルダが実際に存在するとは限らない。
     * </p>
     * @param packageDeclOrNull パッケージ宣言、無名パッケージの場合は{@code null}
     * @return 対象のフォルダへのパス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public File getFolderFor(PackageDeclaration packageDeclOrNull) {
        if (packageDeclOrNull == null) {
            return outputPath;
        }
        return getFolderFor(packageDeclOrNull.getName());
    }

    /**
     * 指定のパッケージ名に関連するフォルダへのパスを返す。
     * <p>
     * 返されたパスにフォルダが実際に存在するとは限らない。
     * </p>
     * @param packageNameOrNull パッケージ名、無名パッケージの場合は{@code null}
     * @return 対象のフォルダへのパス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public File getFolderFor(Name packageNameOrNull) {
        if (packageNameOrNull == null) {
            return outputPath;
        }
        File path = outputPath;
        for (SimpleName segment : Models.toList(packageNameOrNull)) {
            path = new File(path, segment.getToken());
        }
        return path;
    }

    /**
     * 指定のパッケージに指定のサブパスのファイルを作成するためのライターを返す。
     * @param packageDeclOrNull 対象のパッケージ宣言、無名パッケージの場合は{@code null}
     * @param subPath パッケージ下のサブパス
     * @return 開いたライター
     * @throws IOException ファイルの作成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    @Override
    public PrintWriter openFor(
            PackageDeclaration packageDeclOrNull,
            String subPath) throws IOException {
        if (subPath == null) {
            throw new IllegalArgumentException("fileName must not be null"); //$NON-NLS-1$
        }
        File folder = getFolderFor(packageDeclOrNull);
        File file = new File(folder, subPath);
        return open(file);
    }

    private PrintWriter open(File file) throws IOException {
        assert file != null;
        File parent = file.getParentFile();
        if (parent != null) {
            if (parent.mkdirs() == false && parent.exists() == false) {
                throw new IOException(MessageFormat.format(
                        "Failed to create directory for create {0}",
                        file));
            }
        }
        return new PrintWriter(file, encoding.name());
    }
}
