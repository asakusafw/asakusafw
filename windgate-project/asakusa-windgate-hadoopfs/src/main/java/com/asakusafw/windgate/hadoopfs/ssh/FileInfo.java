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
package com.asakusafw.windgate.hadoopfs.ssh;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Represents information of transferring files in {@link FileList}.
 * @since 0.7.4
 */
public class FileInfo {

    private static final byte[] HEADER = {
        (byte) 0xf1,
        (byte) 0x1e,
    };

    static final Charset PATH_ENCODING = StandardCharsets.UTF_8;

    private final String uri;

    FileInfo(String uri) {
        this.uri = uri;
    }

    /**
     * Returns the URI of the target file.
     * @return the file URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns a serialized bytes.
     * @return bytes
     */
    byte[] toBytes() {
        byte[] path = uri.getBytes(PATH_ENCODING);
        byte[] results = new byte[HEADER.length + path.length];
        System.arraycopy(HEADER, 0, results, 0, HEADER.length);
        System.arraycopy(path, 0, results, HEADER.length, path.length);
        return results;
    }

    static FileInfo fromBytes(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length <= HEADER.length) {
            throw new IOException("invalid file info in file list"); //$NON-NLS-1$
        }
        for (int i = 0; i < HEADER.length; i++) {
            if (HEADER[i] != bytes[i]) {
                throw new IOException("invalid file info in file list"); //$NON-NLS-1$
            }
        }
        byte[] pathBytes = Arrays.copyOfRange(bytes, HEADER.length, bytes.length);
        String path = new String(pathBytes, PATH_ENCODING);
        return new FileInfo(path);
    }
}
