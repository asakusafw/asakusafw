/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.jar.JarFile;

import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * ジョブフローのパッケージングを行う。
 * @since 0.1.0
 * @version 0.4.0
 */
public interface Packager extends FlowCompilingEnvironment.Initializable {

    /**
     * The location of manifest file in package.
     * @since 0.4.0
     */
    Location MANIFEST_FILE = Location.fromPath(JarFile.MANIFEST_NAME, '/');

    /**
     * The location of package meta info.
     * @since 0.4.0
     */
    Location PACKAGE_META_INFO = MANIFEST_FILE.getParent();

    /**
     * The location of framework info.
     * @since 0.4.0
     */
    Location FRAMEWORK_INFO = PACKAGE_META_INFO.append("asakusa");

    /**
     * The location of fragment marker file in each class library.
     * @since 0.4.0
     */
    Location FRAGMENT_MARKER_PATH = FRAMEWORK_INFO.append("fragment");

    /**
     * 指定のJavaソースプログラムを出力するためのストリームを開いて返す。
     * @param source 対象のソースプログラムの内容
     * @return ソースプログラムが配置されるべき適切なリソースへの出力ストリーム
     * @throws IOException ストリームの作成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    PrintWriter openWriter(CompilationUnit source) throws IOException;

    /**
     * 指定位置のリソースを出力するためのストリームを開いて返す。
     * @param packageNameOrNull 対象のパッケージ名 、ルートパッケージを基点にする場合は{@code null}
     * @param relativePath 対象のパッケージからの相対パス
     * @return リソースへの出力ストリーム
     * @throws IOException ストリームの作成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    OutputStream openStream(Name packageNameOrNull, String relativePath) throws IOException;

    /**
     * ここまでに追加したソースプログラムやリソースファイル等を、コンパイルしてパッケージングして出力する。
     * @param output 出力先
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    void build(OutputStream output) throws IOException;

    /**
     * これまでに追加したソースプログラムを、そのままパッケージングして出力する。
     * @param output 出力先
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    void packageSources(OutputStream output) throws IOException;
}
