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
package com.asakusafw.runtime.util.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.hadoop.fs.Path;

/**
 * Mock {@link FileCacheRepository}.
 */
public class MockFileCacheRepository implements FileCacheRepository {

    private final File repository;

    /**
     * Creates a new instance
     * @param repository the repository folder
     */
    public MockFileCacheRepository(File repository) {
        this.repository = repository;
    }

    @Override
    public Path resolve(Path file) throws IOException, InterruptedException {
        URI uri = file.toUri();
        if (uri.getScheme() == null || uri.getScheme().equals("file") == false) {
            return null;
        }
        File source = new File(uri);
        File target = new File(repository, source.getName());
        InputStream in = new FileInputStream(source);
        try {
            OutputStream out = new FileOutputStream(target);
            try {
                byte[] buf = new byte[256];
                while (true) {
                    int read = in.read(buf);
                    if (read < 0) {
                        break;
                    }
                    out.write(buf, 0, read);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        return new Path(target.toURI());
    }
}
