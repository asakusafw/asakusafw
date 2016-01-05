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
package com.asakusafw.yaess.flowlog;

import java.io.IOException;
import java.text.MessageFormat;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitorProvider;
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * An implementation of {@link ExecutionMonitorProvider} to save log each jobflow.
 * @since 0.2.6
 * @version 0.4.0
 */
public class FlowLoggerProvider extends ExecutionMonitorProvider {

    private volatile FlowLoggerProfile logProfile;

    @Override
    protected void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        try {
            this.logProfile = FlowLoggerProfile.convert(profile);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure \"{0}\" ({1})",
                    profile.getPrefix(),
                    profile.getServiceClass().getName()), e);
        }
    }

    @Override
    public PhaseMonitor newInstance(ExecutionContext context) throws InterruptedException, IOException {
        return new FlowLogger(context, logProfile);
    }
}
