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
 * An implementation of {@link Emitter} which generates files into the local file system.
 */
public class Filer extends Emitter {

    private final File outputPath;

    private final Charset encoding;

    /**
     * Creates a new instance.
     * @param outputPath the base output directory
     * @param encoding the character set encoding of generating source files
     * @throws IllegalArgumentException if the parameters are {@code null}
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
     * Returns the folder for the target package.
     * The returning folder may not exist.
     * @param packageDeclOrNull the target package, or {@code null} for the default (unnamed) package
     * @return the corresponded folder path
     */
    public File getFolderFor(PackageDeclaration packageDeclOrNull) {
        if (packageDeclOrNull == null) {
            return outputPath;
        }
        return getFolderFor(packageDeclOrNull.getName());
    }

    /**
     * Returns the folder for the target package.
     * The returning folder may not exist.
     * @param packageNameOrNull the target package, or {@code null} for the default (unnamed) package
     * @return the corresponded folder path
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
        if (parent != null && parent.mkdirs() == false && parent.exists() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create directory for create {0}",
                    file));
        }
        return new PrintWriter(file, encoding.name());
    }
}
