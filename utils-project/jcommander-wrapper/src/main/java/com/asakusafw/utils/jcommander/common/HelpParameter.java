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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Provides help flag.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.utils.jcommander.common.jcommander")
public class HelpParameter {

    /**
     * Whether or not the help message is required.
     */
    @Parameter(
            names = { "-h", "--help", },
            descriptionKey = "parameter.help",
            help = true,
            required = false
    )
    public boolean required = false;

    /**
     * Returns whether or not displaying help messages is required.
     * @return {@code true} if help is required, otherwise {@code false}
     */
    public boolean isRequired() {
        return required;
    }
}
