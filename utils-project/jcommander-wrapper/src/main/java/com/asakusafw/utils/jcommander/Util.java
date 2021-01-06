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
package com.asakusafw.utils.jcommander;

import java.text.MessageFormat;

import com.beust.jcommander.Parameters;

final class Util {

    private Util() {
        return;
    }

    static String getCommandName(Object command) {
        Parameters parameters = command.getClass().getAnnotation(Parameters.class);
        if (parameters == null || parameters.commandNames().length != 1) {
            throw new IllegalStateException(MessageFormat.format(
                    "there are no valid command name information: {0}", //$NON-NLS-1$
                    command.getClass().getName()));
        }
        return parameters.commandNames()[0];
    }
}
