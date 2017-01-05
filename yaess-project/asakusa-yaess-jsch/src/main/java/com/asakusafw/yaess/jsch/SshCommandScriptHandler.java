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

import com.asakusafw.yaess.basic.ProcessCommandScriptHandler;
import com.asakusafw.yaess.basic.ProcessExecutor;
import com.asakusafw.yaess.core.CommandScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;
import com.jcraft.jsch.JSchException;

/**
 * Basic implementation of {@link CommandScriptHandler} using local processes.
 * <h3> Profile format </h3>
<pre><code>
# &lt;position&gt; = 0, 1, 2, ...
# &lt;prefix command token&gt; can contain "&#64;[position],"
# this will be replaced as original command tokens (0-origin position)
command.&lt;profile-name&gt; = &lt;this class name&gt;
command.&lt;profile-name&gt;.command.&lt;position&gt; = $&lt;prefix command token&gt;
command.&lt;profile-name&gt;.ssh.user=asakusa
command.&lt;profile-name&gt;.ssh.host=localhost
command.&lt;profile-name&gt;.ssh.port=22
command.&lt;profile-name&gt;.ssh.privateKey=${HOME}/.ssh/id_dsa
command.&lt;profile-name&gt;.ssh.passPhrase=
command.&lt;profile-name&gt;.env.ASAKUSA_HOME = ${ASAKUSA_HOME}
command.&lt;profile-name&gt;.env.&lt;key&gt; = $&lt;extra environment variables&gt;
</code></pre>
 * @since 0.2.3
 */
public class SshCommandScriptHandler extends ProcessCommandScriptHandler {

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
