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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * A command for copying Direct I/O resources.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "copy",
        commandDescriptionKey = "command.file-copy",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class FileCopyCommand extends AbstractFileCopyCommand {

    @Parameter(
            names = { "-r", "--recursive" },
            descriptionKey = "parameter.recursive-copy",
            required = false)
    boolean recursive = false;

    @Override
    Op getOp() {
        return recursive ? Op.COPY_RECURSIVE : Op.COPY_THIN;
    }
}
