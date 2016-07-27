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
package com.asakusafw.runtime.io.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.MessageFormat;

/**
 * Installs a temporary file.
 * @since 0.9.0
 */
public final class TemporaryFileInstaller {

    private final byte[] bytes;

    private final boolean executable;

    private TemporaryFileInstaller(byte[] bytes, boolean executable) {
        this.bytes = bytes;
        this.executable = executable;
    }

    /**
     * Creates a new instance.
     * @param contents the file contents
     * @param executable {@code true} to install as an executable file, otherwise {@code false}
     * @return the created instance
     * @throws IOException if failed to load contents
     */
    public static TemporaryFileInstaller newInstance(InputStream contents, boolean executable) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        while (true) {
            int read = contents.read(buf);
            if (read < 0) {
                break;
            }
            output.write(buf, 0, read);
        }
        byte[] bytes = output.toByteArray();
        return new TemporaryFileInstaller(bytes, executable);
    }

    /**
     * Installs contents into the target file.
     * @param target the target file
     * @param reuse {@code true} to reuse installed file, otherwise {@code false}
     * @return {@code true} if target file is successfully installed,
     *     or {@code false} if the file is already installed
     * @throws IOException if error occurred while installing the target file
     */
    public boolean install(File target, boolean reuse) throws IOException {
        File parent = target.getAbsoluteFile().getParentFile();
        if (parent.mkdirs() == false && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "failed to create a file: {0}",
                    target));
        }
        try (RandomAccessFile file = new RandomAccessFile(target, "rw"); //$NON-NLS-1$
                FileLock lock = file.getChannel().lock(0, 0, false)) {
            if (reuse && isReusable(target, file)) {
                return false;
            }
            doInstall(target, file);
            return true;
        }
    }

    private boolean isReusable(File target, RandomAccessFile file) throws IOException {
        if (executable && target.canExecute() == false) {
            return false;
        }
        if (bytes.length != file.length()) {
            return false;
        }
        int offset = 0;
        file.seek(offset);
        byte[] buf = new byte[256];
        while (true) {
            int read = file.read(buf);
            if (read < 0) {
                break;
            }
            for (int i = 0; i < read; i++) {
                if (bytes[i + offset] != buf[i]) {
                    return false;
                }
            }
            offset += read;
        }
        return true;
    }

    private void doInstall(File target, RandomAccessFile file) throws IOException {
        file.setLength(0L);
        file.write(bytes);
        if (executable) {
            target.setExecutable(true);
        }
    }
}
