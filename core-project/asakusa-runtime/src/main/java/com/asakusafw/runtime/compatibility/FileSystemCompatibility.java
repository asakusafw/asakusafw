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
package com.asakusafw.runtime.compatibility;

import org.apache.hadoop.fs.FileStatus;

/**
 * Compatibility for file system APIs.
 * @since 0.5.0
 */
public final class FileSystemCompatibility {

    private FileSystemCompatibility() {
        return;
    }

    /**
     * Returns whether the target file status represents a directory.
     * @param status target file status
     * @return {@code true} iff it is a directory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static boolean isDirectory(FileStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        return status.isDirectory();
    }
}
