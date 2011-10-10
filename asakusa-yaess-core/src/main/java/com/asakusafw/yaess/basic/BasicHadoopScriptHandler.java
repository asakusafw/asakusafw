/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.io.IOException;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;

/**
 * Basic implementation of {@link HadoopScriptHandler} using local processes.
 * This handler just launches a command with following arguments in its tail.
 * <ol>
 * <li> {@link HadoopScript#getClassName() class name} </li>
 * <li> {@link ExecutionContext#getBatchId() batch-id} </li>
 * <li> {@link ExecutionContext#getFlowId() flow-id} </li>
 * <li> {@link ExecutionContext#getExecutionId() execution-id} </li>
 * <li> {@link ExecutionContext#getArgumentsAsString() batch-arguments} </li>
 * <li> {@link HadoopScript#getHadoopProperties() hadoop properties (with "-D")} </li>
 * </ol>
 * Additionally, the handler lanuches a command if {@code hadoop.workingDirectory} is defined.
 * <ol>
 * <li> Specified in {@code hadoop.workingDirectory} </li>
 * <li> {@link ExecutionContext#getBatchId() batch-id} </li>
 * <li> {@link ExecutionContext#getFlowId() flow-id} </li>
 * <li> {@link ExecutionContext#getExecutionId() execution-id} </li>
 * <li> {@link ExecutionContext#getArgumentsAsString() batch-arguments} </li>
 * <li> {@link HadoopScript#getHadoopProperties() hadoop properties (with "-D")} </li>
 * </ol>
 * You must specify a executable file for above arguments,
 * by setting {@code hadoop.command.0 = <executable-file>} in the profile set.
 *
 * <h3> Profile format </h3>
<pre><code>
# &lt;position&gt; = 0, 1, 2, ...
# &lt;prefix command token&gt; can contain "&#64;[position],"
# this will be replaced as original command tokens (0-origin position)
hadoop = &lt;this class name&gt;
hadoop.env.ASAKUSA_HOME = ${ASAKUSA_HOME}
hadoop.command.0 = ${ASAKUSA_HOME}/yaess-basic/bin/hadoop-command.sh
hadoop.command.&lt;position&gt; = $&lt;prefix command token&gt;
hadoop.cleanup.0 = ${ASAKUSA_HOME}/yaess-basic/bin/hadoop-cleanup.sh
hadoop.workingDirectory = $<cluster working directory>
hadoop.env.&lt;key&gt; = $&lt;extra environment variables&gt;
</code></pre>
 * @since 0.2.3
 */
public class BasicHadoopScriptHandler extends ProcessHadoopScriptHandler {

    @Override
    protected void configureExtension(
            ServiceProfile<?> profile,
            VariableResolver variables) throws InterruptedException, IOException {
        return;
    }

    @Override
    protected ProcessExecutor getCommandExecutor() {
        return ProcessUtil.getProcessExecutor();
    }
}
