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
package com.asakusafw.utils.jcommander.common;

import java.io.PrintWriter;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Provides verbose flag.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.utils.jcommander.common.jcommander")
public class VerboseParameter {

    /**
     * Whether or not the verbose message is required.
     */
    @Parameter(
            names = { "-v", "--verbose", },
            descriptionKey = "parameter.verbose",
            required = false
    )
    public boolean required = false;

    /**
     * Returns the required.
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Executes the given action only if verbose messages are required.
     * @param action the action
     */
    public void ifRequired(Runnable action) {
        if (required) {
            action.run();
        }
    }

    /**
     * Prints message only if verbose messages are required.
     * @param writer the target writer
     * @param format the message format
     * @param arguments the message arguments
     */
    public void printf(PrintWriter writer, String format, Object... arguments) {
        if (required) {
            writer.printf(format, arguments);
        }
    }
}
