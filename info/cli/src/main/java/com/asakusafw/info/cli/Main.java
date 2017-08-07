/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.info.cli;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.cli.common.GroupUsageCommand;
import com.asakusafw.info.cli.draw.DrawGroup;
import com.asakusafw.info.cli.draw.DrawJobflowCommand;
import com.asakusafw.info.cli.draw.DrawOperatorCommand;
import com.asakusafw.info.cli.draw.DrawPlanCommand;
import com.asakusafw.info.cli.list.ListBatchCommand;
import com.asakusafw.info.cli.list.ListDirectFileInputCommand;
import com.asakusafw.info.cli.list.ListDirectFileOutputCommand;
import com.asakusafw.info.cli.list.ListDirectIoGroup;
import com.asakusafw.info.cli.list.ListGroup;
import com.asakusafw.info.cli.list.ListHiveGroup;
import com.asakusafw.info.cli.list.ListHiveInputCommand;
import com.asakusafw.info.cli.list.ListHiveOutputCommand;
import com.asakusafw.info.cli.list.ListJobflowCommand;
import com.asakusafw.info.cli.list.ListOperatorCommand;
import com.asakusafw.info.cli.list.ListParameterCommand;
import com.asakusafw.info.cli.list.ListPlanCommand;
import com.asakusafw.info.cli.list.ListWindGateGroup;
import com.asakusafw.info.cli.list.ListWindGateInputCommand;
import com.asakusafw.info.cli.list.ListWindGateOutputCommand;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.JCommanderWrapper;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * CLI entry for information models.
 * @since 0.10.0
 */
@Parameters(
        commandDescription = "An Asakusa Framework information viewer tool."
)
public final class Main extends GroupUsageCommand {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    static final String KEY_COMMAND_NAME = "cli.name";

    private Main() {
        return;
    }

    /**
     * Program entry.
     * @param args command line tokens
     */
    public static void main(String... args) {
        try {
            exec(args);
        } catch (CommandExecutionException e) {
            LOG.error("error occurred while executing command", e);
            System.exit(1);
        } catch (CommandConfigurationException e) {
            LOG.error("{}", e.getMessage());
            LOG.debug("configuration error detail: {}", Arrays.toString(args), e);
            System.exit(2);
        } catch (ParameterException e) {
            LOG.error("cannot recognize arguments: {}", Arrays.toString(args), e);
            System.exit(3);
        }
    }

    static void exec(String... args) {
        String programName = Optional.ofNullable(System.getProperty(KEY_COMMAND_NAME))
                .orElse("java -jar <self.jar>");
        new JCommanderWrapper<Runnable>(programName, new Main())
            .addGroup(new ListGroup(), list -> list
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
                            .addCommand(new ListHiveOutputCommand())))
            .addGroup(new DrawGroup(), draw -> draw
                    .addCommand(new DrawJobflowCommand())
                    .addCommand(new DrawOperatorCommand())
                    .addCommand(new DrawPlanCommand()))
            .parse(args)
            .ifPresent(Runnable::run);
    }
}
