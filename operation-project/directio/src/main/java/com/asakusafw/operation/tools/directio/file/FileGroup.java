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

import com.asakusafw.utils.jcommander.CommandBuilder;
import com.asakusafw.utils.jcommander.common.CommandProvider;
import com.asakusafw.utils.jcommander.common.GroupUsageCommand;
import com.beust.jcommander.Parameters;

/**
 * A group command for file.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "file",
        commandDescription = "Manipulates Direct I/O resources.",
        commandDescriptionKey = "command.file",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class FileGroup extends GroupUsageCommand implements CommandProvider {

    @Override
    public void accept(CommandBuilder<Runnable> builder) {
        builder.addGroup(new FileGroup(), flattern()::accept);
    }

    /**
     * Returns a {@link CommandProvider} which adds the group commands.
     * @return flattened {@link CommandProvider} of this
     */
    public CommandProvider flattern() {
        return builder -> builder
                .addCommand(new FileListCommand())
                .addCommand(new FileGetCommand())
                .addCommand(new FilePutCommand())
                .addCommand(new FileCopyCommand())
                .addCommand(new FileMoveCommand())
                .addCommand(new FileDeleteCommand())
                .addCommand(new FileMakeDirectoryCommand());
    }
}
