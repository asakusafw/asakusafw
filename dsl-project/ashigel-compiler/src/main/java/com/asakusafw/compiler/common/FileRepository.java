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
package com.asakusafw.compiler.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.ResourceRepository;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.utils.collections.Lists;

/**
 * ファイルシステム上のディレクトリをリソースのリポジトリとする。
 */
public class FileRepository implements ResourceRepository {

    static final Logger LOG = LoggerFactory.getLogger(FileRepository.class);

    private final File root;

    /**
     * インスタンスを生成する。
     * @param root リポジトリのルートディレクトリ
     * @throws IOException if failed to canonicalize path
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FileRepository(File root) throws IOException {
        Precondition.checkMustNotBeNull(root, "root"); //$NON-NLS-1$
        if (root.isDirectory() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("FileRepository.errorInputNotDirectory"), //$NON-NLS-1$
                    root));
        }
        this.root = root.getAbsoluteFile().getCanonicalFile();
    }

    @Override
    public Cursor createCursor() throws IOException {
        List<Resource> results = Lists.create();
        collect(results, null, root);
        return new ResourceCursor(results.iterator());
    }

    private void collect(
            List<Resource> results,
            Location location,
            File file) {
        assert results != null;
        assert file != null;
        if (file.isFile()) {
            results.add(new Resource(file, location));
        } else if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                Location enter = new Location(location, child.getName());
                collect(results, enter, child);
            }
        }
    }

    private static class Resource {

        public final File file;

        public final Location location;

        Resource(File file, Location location) {
            assert file != null;
            assert location != null;
            this.file = file;
            this.location = location;
        }
    }

    private static class ResourceCursor implements Cursor {

        private final Iterator<Resource> iterator;

        private Resource current;

        ResourceCursor(Iterator<Resource> iterator) {
            assert iterator != null;
            this.iterator = iterator;
        }

        @Override
        public boolean next() throws IOException {
            if (iterator.hasNext() == false) {
                return false;
            }
            current = iterator.next();
            return true;
        }

        @Override
        public Location getLocation() {
            return current.location;
        }

        @Override
        public InputStream openResource() throws IOException {
            return new FileInputStream(current.file);
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
