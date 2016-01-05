/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;

/**
 * An implementation of {@link HadoopScriptHandler} using {@link ExecutionScriptHandlerDispatcher}.
 * @since 0.2.6
 */
public class HadoopScriptHandlerDispatcher
        extends ExecutionScriptHandlerDispatcher<HadoopScript>
        implements HadoopScriptHandler {

    /**
     * Creates a new instance.
     */
    public HadoopScriptHandlerDispatcher() {
        super(HadoopScriptHandler.class);
    }
}
