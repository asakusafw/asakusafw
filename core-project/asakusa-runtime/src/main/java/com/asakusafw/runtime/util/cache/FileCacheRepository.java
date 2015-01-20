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

import java.io.IOException;

import org.apache.hadoop.fs.Path;

/**
 * An abstract super interface of remote cache.
 * @since 0.7.0
 */
public interface FileCacheRepository {

    /**
     * Put a file into remote cache and returns the cached path.
     * @param file the target file path
     * @return the resolved path, or {@code null} path if this does not support the target file
     * @throws IOException if failed to put the file into cache
     * @throws InterruptedException if interrupted while resolving cache file
     */
    Path resolve(Path file) throws IOException, InterruptedException;
}