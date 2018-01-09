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
package com.asakusafw.operation.tools.directio;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.conf.ConfigurationGroup;
import com.asakusafw.operation.tools.directio.file.FileGroup;
import com.asakusafw.operation.tools.directio.transaction.TransactionGroup;
import com.asakusafw.runtime.windows.WindowsConfigurator;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.JCommanderWrapper;
import com.asakusafw.utils.jcommander.common.GroupUsageCommand;
import com.beust.jcommander.ParameterException;

/**
 * CLI entry of Asakusa Direct I/O tools.
 * @since 0.10.0
 */
public class DirectIo extends GroupUsageCommand {

    static {
        SimpleLoggerUtil.configure();
        WindowsConfigurator.install();
    }

    static final Logger LOG = LoggerFactory.getLogger(DirectIo.class);

    static final String KEY_COMMAND_NAME = "cli.name";

    static final String DEFAULT_COMMAND_NAME = "directio";

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
            JCommanderWrapper.handle(e, s -> LOG.error("{}", s)); //$NON-NLS-1$
            System.exit(3);
        }
    }

    static void exec(String... args) {
        String programName = System.getProperty(KEY_COMMAND_NAME, DEFAULT_COMMAND_NAME);
        new JCommanderWrapper<Runnable>(programName, new DirectIo())
                .configure(new FileGroup().flattern())
                .configure(new ConfigurationGroup())
                .configure(new TransactionGroup())
                .parse(args)
                .ifPresent(Runnable::run);
    }
}
