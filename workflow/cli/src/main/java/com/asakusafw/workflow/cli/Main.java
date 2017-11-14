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
package com.asakusafw.workflow.cli;

import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.JCommanderWrapper;
import com.asakusafw.utils.jcommander.common.CommandProvider;
import com.asakusafw.utils.jcommander.common.GroupUsageCommand;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * CLI entry of Asakusa workflow.
 * @since 0.10.0
 */
@Parameters(
        commandDescription = "An Asakusa Framework workflow tool."
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
                .configure(it -> ServiceLoader.load(CommandProvider.class)
                        .forEach(provider -> provider.accept(it)))
                .parse(args)
                .ifPresent(Runnable::run);
    }
}
