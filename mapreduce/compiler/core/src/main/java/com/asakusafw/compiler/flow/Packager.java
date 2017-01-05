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
package com.asakusafw.compiler.flow;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.jar.JarFile;

import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * An abstract super interface of packager that creates jobflow packages.
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
    Location FRAMEWORK_INFO = PACKAGE_META_INFO.append("asakusa"); //$NON-NLS-1$

    /**
     * The location of fragment marker file in each class library.
     * @since 0.4.0
     */
    Location FRAGMENT_MARKER_PATH = FRAMEWORK_INFO.append("fragment"); //$NON-NLS-1$

    /**
     * Creates a new Java source file.
     * @param source the target compilation unit
     * @return the writer to output contents of the source file
     * @throws IOException if error occurred while creating the file
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    PrintWriter openWriter(CompilationUnit source) throws IOException;

    /**
     * Creates a new resource file.
     * @param packageNameOrNull the target package name, or {@code null} for the root package
     * @param relativePath the relative path from the target package
     * @return the output stream to output contents of the resource
     * @throws IOException if error occurred while creating the resource
     * @throws IllegalArgumentException if the {@code relativePath} is {@code null}
     */
    OutputStream openStream(Name packageNameOrNull, String relativePath) throws IOException;

    /**
     * Creates a jobflow package from the previously added source files and resources.
     * @param output the output target
     * @throws IOException if error occurred while building the source files
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void build(OutputStream output) throws IOException;

    /**
     * Creates a source package from the previously added source files.
     * @param output the output target
     * @throws IOException if error occurred while collecting the source files
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    void packageSources(OutputStream output) throws IOException;
}
