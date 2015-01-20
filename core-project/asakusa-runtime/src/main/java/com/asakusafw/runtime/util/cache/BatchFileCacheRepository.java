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
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;

/**
 * Batch processing for {@link FileCacheRepository}.
 * @since 0.7.0
 */
public interface BatchFileCacheRepository {

    /**
     * Put files into remote cache and returns a mapping original files into cached path.
     * @param files the target file paths
     * @return the resolved mapping original files into cached path;
     *     the cached path may be {@code null} if this cannot prepare cache for the corresponding original file
     * @throws IOException if failed to put the file into cache
     * @throws InterruptedException if interrupted while resolving cache file
     */
    Map<Path, Path> resolve(List<? extends Path> files) throws IOException, InterruptedException;
}
