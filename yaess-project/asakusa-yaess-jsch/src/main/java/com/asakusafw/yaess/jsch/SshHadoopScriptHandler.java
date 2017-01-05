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
package com.asakusafw.yaess.jsch;

import java.io.IOException;
import java.text.MessageFormat;

import com.asakusafw.yaess.basic.ProcessExecutor;
import com.asakusafw.yaess.basic.ProcessHadoopScriptHandler;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.jcraft.jsch.JSchException;

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
 * Additionally, the handler lanuches a command if {@code hadoop.cleanup} is true.
 * <ol>
 * <li> {@link #CLEANUP_STAGE_CLASS} </li>
 * <li> {@link ExecutionContext#getBatchId() batch-id} </li>
 * <li> {@link ExecutionContext#getFlowId() flow-id} </li>
 * <li> {@link ExecutionContext#getExecutionId() execution-id} </li>
 * <li> {@link ExecutionContext#getArgumentsAsString() batch-arguments} </li>
 * <li> hadoop properties (with "-D") </li>
 * </ol>
 *
 * <h3> Profile format </h3>
<pre><code>
# &lt;position&gt; = 0, 1, 2, ...
# &lt;prefix command token&gt; can contain "&#64;[position],"
# this will be replaced as original command tokens (0-origin position)
hadoop = &lt;this class name&gt;
hadoop.command.&lt;position&gt; = $&lt;prefix command token&gt;
hadoop.ssh.user=asakusa
hadoop.ssh.host=localhost
hadoop.ssh.port=22
hadoop.ssh.privateKey=${HOME}/.ssh/id_dsa
hadoop.ssh.passPhrase=
hadoop.env.ASAKUSA_HOME = ${ASAKUSA_HOME}
hadoop.cleanup = whether enables cleanup
hadoop.env.&lt;key&gt; = $&lt;extra environment variables&gt;
hadoop.prop.&lt;key&gt; = $&lt;extra Hadoop properties&gt;
</code></pre>
 * @since 0.2.3
 */
public class SshHadoopScriptHandler extends ProcessHadoopScriptHandler {

    private volatile JschProcessExecutor executor;

    @Override
    protected void configureExtension(ServiceProfile<?> profile) throws InterruptedException, IOException {
        try {
            this.executor = JschProcessExecutor.extract(
                    profile.getPrefix(),
                    profile.getConfiguration(),
                    profile.getContext().getContextParameters());
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure SSH: {0}",
                    profile.getPrefix()), e);
        } catch (JSchException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure SSH: {0}",
                    profile.getPrefix()), e);
        }
    }

    @Override
    protected ProcessExecutor getCommandExecutor() {
        return executor;
    }
}
