/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.jcommander.common.LocalPath;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Handles parameters about local file system path.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.operation.tools.directio.jcommander")
public class LocalPathParameter {

    static final Logger LOG = LoggerFactory.getLogger(LocalPathParameter.class);

    @Parameter(
            names = { "--working-directory" },
            descriptionKey = "parameter.working-directory",
            required = false,
            hidden = true)
    Path workingDirectory;

    /**
     * Resolves the given path.
     * @param path the path string
     * @return the corresponded local file path
     */
    public Path resolve(String path) {
        return LocalPath.of(path, workingDirectory);
    }
}
