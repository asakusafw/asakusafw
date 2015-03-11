/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.multidispatch;

import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.CommandScriptHandler;

/**
 * An implementation of {@link CommandScriptHandler} using {@link ExecutionScriptHandlerDispatcher}.
 * @since 0.2.6
 */
public class CommandScriptHandlerDispatcher
        extends ExecutionScriptHandlerDispatcher<CommandScript>
        implements CommandScriptHandler {

    /**
     * Creates a new instance.
     */
    public CommandScriptHandlerDispatcher() {
        super(CommandScriptHandler.class);
    }
}
