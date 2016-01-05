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
package com.asakusafw.runtime.compatibility.hadoop1;

import org.apache.hadoop.fs.FileStatus;

import com.asakusafw.runtime.compatibility.hadoop.FileSystemCompatibilityHadoop;

/**
 * Compatibility for file system APIs (Hadoop {@code 1.x}).
 * Clients should not use this class directly.
 * @since 0.7.4
 */
public final class FileSystemCompatibilityHadoop1 extends FileSystemCompatibilityHadoop {

    @Override
    public boolean isDirectory(FileStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        return status.isDir();
    }
}
