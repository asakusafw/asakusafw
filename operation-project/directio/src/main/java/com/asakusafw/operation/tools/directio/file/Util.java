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
package com.asakusafw.operation.tools.directio.file;

import java.util.Optional;

final class Util {

    private Util() {
        return;
    }

    static org.apache.hadoop.fs.Path asHadoopPath(String path) {
        return normalize(new org.apache.hadoop.fs.Path(path));
    }

    static org.apache.hadoop.fs.Path asHadoopPath(java.nio.file.Path path) {
        org.apache.hadoop.fs.Path result = new org.apache.hadoop.fs.Path(path.toUri());
        return normalize(result);
    }

    private static org.apache.hadoop.fs.Path normalize(org.apache.hadoop.fs.Path path) {
        if (path.getName().isEmpty()) {
            return Optional.ofNullable(path.getParent()).orElse(path);
        }
        return path;
    }

    static org.apache.hadoop.fs.Path resolve(org.apache.hadoop.fs.Path base, String name) {
        return new org.apache.hadoop.fs.Path(base, name);
    }
}
