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
package com.asakusafw.utils.jcommander.common;

import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParametersDelegate;

/**
 * An abstract super class of displaying usage of the group.
 * @since 0.10.0
 */
public abstract class GroupUsageCommand implements Runnable {

    /**
     * Whether or not the help message is required (hidden parameter).
     */
    @ParametersDelegate
    HiddenHelpParameter helpParameter = new HiddenHelpParameter();

    /**
     * The context commander.
     */
    @Inject
    public JCommander commander;

    @Override
    public void run() {
        if (commander == null) {
            throw new IllegalStateException();
        }
        print(commander);
    }

    /**
     * Returns whether or not displaying help messages is required.
     * @return {@code true} if help is required, otherwise {@code false}
     */
    public boolean isHelp() {
        return helpParameter.isRequired();
    }

    /**
     * Prints the group usage.
     * @param commander the current commander
     */
    public static void print(JCommander commander) {
        System.out.printf(Messages.getString("GroupUsageCommand.title"), commander.getProgramName()); //$NON-NLS-1$
        System.out.println();
        System.out.println(Messages.getString("GroupUsageCommand.headerCommandList")); //$NON-NLS-1$
        commander.getCommands().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(it -> System.out.printf("    %s - %s%n", //$NON-NLS-1$
                        it.getKey(),
                        commander.getCommandDescription(it.getKey())));
        System.out.println();
        System.out.printf(Messages.getString("GroupUsageCommand.footerCommandList"), //$NON-NLS-1$
                commander.getProgramName());
    }
}
