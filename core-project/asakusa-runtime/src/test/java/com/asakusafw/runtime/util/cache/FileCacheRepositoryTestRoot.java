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
package com.asakusafw.runtime.util.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.hadoop.fs.Path;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Test root for this package.
 */
public abstract class FileCacheRepositoryTestRoot {

    private static final Charset ENCODING = Charset.forName("UTF-8");

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Converts {@link Path} into {@link File}.
     * @param path the original path
     * @return the converted file
     */
    protected File file(Path path) {
        return new File(path.toUri());
    }

    /**
     * Converts {@link Path} into {@link File}.
     * @param paths the original paths
     * @return the converted files
     */
    protected Map<File, File> files(Map<Path, Path> paths) {
        Map<File, File> results = new LinkedHashMap<File, File>();
        for (Map.Entry<Path, Path> entry : paths.entrySet()) {
            results.put(file(entry.getKey()), entry.getValue() == null ? null : file(entry.getValue()));
        }
        return results;
    }

    /**
     * Converts {@link File} into {@link Path}.
     * @param file the original file
     * @return the converted path
     * @throws IOException if failed by I/O error
     */
    protected Path path(File file) throws IOException {
        return new Path(file.getCanonicalFile().toURI());
    }

    /**
     * Puts a contents into the file.
     * @param file the target file
     * @param contents the file contents
     * @return the target file
     * @throws IOException if failed by I/O error
     */
    protected File put(File file, String contents) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING));
        try {
            writer.print(contents);
        } finally {
            writer.close();
        }
        return file;
    }

    /**
     * Returns a contents in the file.
     * @param file the target file
     * @return the contents in the file
     * @throws IOException if failed by I/O error
     */
    protected String get(File file) throws IOException {
        Scanner scanner = new Scanner(file, ENCODING.name());
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            }
            throw new IOException();
        } finally {
            scanner.close();
        }
    }

    /**
     * Returns whether the {@code file} is in the {@code parent} directory recursively or not.
     * @param parent the parent file
     * @param file the target file
     * @return {@code true} the parent file includes the target file, or {@code false} otherwise
     * @throws IOException if failed by I/O error
     */
    protected boolean containsFile(File parent, File file) throws IOException {
        File r = parent.getCanonicalFile();
        for (File current = file.getCanonicalFile(); current != null; current = current.getParentFile()) {
            if (current.equals(r)) {
                return true;
            }
        }
        return false;
    }
}