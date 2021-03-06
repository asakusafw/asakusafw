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
package com.asakusafw.info.cli.list;

import com.asakusafw.utils.jcommander.CommandBuilder;
import com.asakusafw.utils.jcommander.common.CommandProvider;
import com.asakusafw.utils.jcommander.common.GroupUsageCommand;
import com.beust.jcommander.Parameters;

/**
 * A group command for list.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "list",
        commandDescriptionKey = "command.generate-list",
        resourceBundle = "com.asakusafw.info.cli.jcommander"
)
public class ListGroup extends GroupUsageCommand implements CommandProvider {

    @Override
    public void accept(CommandBuilder<Runnable> builder) {
        builder.addGroup(this, group -> group
                .addCommand(new ListJobflowCommand())
                .addCommand(new ListBatchCommand())
                .addCommand(new ListParameterCommand())
                .addCommand(new ListOperatorCommand())
                .addCommand(new ListPlanCommand())
                .addGroup(new ListDirectIoGroup(), directio -> directio
                        .addCommand(new ListDirectFileInputCommand())
                        .addCommand(new ListDirectFileOutputCommand()))
                .addGroup(new ListWindGateGroup(), windgate -> windgate
                        .addCommand(new ListWindGateInputCommand())
                        .addCommand(new ListWindGateOutputCommand()))
                .addGroup(new ListHiveGroup(), hive -> hive
                        .addCommand(new ListHiveInputCommand())
                        .addCommand(new ListHiveOutputCommand())));
    }
}
