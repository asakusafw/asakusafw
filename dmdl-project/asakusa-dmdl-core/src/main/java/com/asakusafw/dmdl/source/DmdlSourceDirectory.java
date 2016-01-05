/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.source;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * DMDL source directory.
 */
public class DmdlSourceDirectory implements DmdlSourceRepository {

    private File directory;

    private Charset encoding;

    private Pattern inclusionPattern;

    private Pattern exclusionPattern;

    /**
     * Creates and returns a new instance.
     * @param directory the root source directory
     * @param encoding the charset of each source file
     * @param inclusionPattern the inclusion file name pattern,
     *     which filters files if not matched
     * @param exclusionPattern the exclusion file name pattern,
     *     which filters files if matched
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DmdlSourceDirectory(
            File directory,
            Charset encoding,
            Pattern inclusionPattern,
            Pattern exclusionPattern) {
        if (directory == null) {
            throw new IllegalArgumentException("directory must not be null"); //$NON-NLS-1$
        }
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null"); //$NON-NLS-1$
        }
        if (inclusionPattern == null) {
            throw new IllegalArgumentException("inclusionPattern must not be null"); //$NON-NLS-1$
        }
        if (exclusionPattern == null) {
            throw new IllegalArgumentException("exclusionPattern must not be null"); //$NON-NLS-1$
        }
        this.directory = directory;
        this.encoding = encoding;
        this.inclusionPattern = inclusionPattern;
        this.exclusionPattern = exclusionPattern;
    }

    @Override
    public Cursor createCursor() throws IOException {
        List<File> files = collect(directory, new ArrayList<File>());
        return new DmdlSourceFile.FileListCursor(files.iterator(), encoding);
    }

    private List<File> collect(File current, List<File> files) {
        assert current != null;
        if (current.isFile()) {
            if (accept(current)) {
                files.add(current);
            }
        } else {
            for (File child : current.listFiles()) {
                collect(child, files);
            }
        }
        return files;
    }

    boolean accept(File file) {
        assert file != null;
        String name = file.getName();
        if (inclusionPattern.matcher(name).matches() == false) {
            return false;
        }
        if (exclusionPattern.matcher(name).matches()) {
            return false;
        }
        return true;
    }
}
