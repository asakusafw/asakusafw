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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;

/**
 * Null implementation of {@link BatchFileCacheRepository}.
 * This implementation does not create any cache files, always returns original file path.
 * @since 0.7.0
 */
public class NullBatchFileCacheRepository implements BatchFileCacheRepository {

    @Override
    public Map<Path, Path> resolve(List<? extends Path> files) throws IOException, InterruptedException {
        Map<Path, Path> results = new LinkedHashMap<Path, Path>();
        for (Path path : files) {
            results.put(path, null);
        }
        return results;
    }
}
