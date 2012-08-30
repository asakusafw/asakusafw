/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.SimulationSupport;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitorProvider;
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Basic implementation of {@link ExecutionMonitorProvider}.
 * @since 0.2.3
 */
@SimulationSupport
public class BasicMonitorProvider extends ExecutionMonitorProvider {

    static final Logger LOG = LoggerFactory.getLogger(BasicMonitorProvider.class);

    private volatile double stepUnit;

    /**
     * The key name of step unit (optional).
     */
    public static final String KEY_STEP_UNIT = "stepUnit";

    @Override
    protected void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        configureStepUnit(profile);
    }

    private void configureStepUnit(ServiceProfile<?> profile) throws IOException {
        assert profile != null;
        String stepUnitString = profile.getConfiguration(KEY_STEP_UNIT, false, true);
        if (stepUnitString == null) {
            LOG.debug("{} is not defined in {}", KEY_STEP_UNIT, profile.getPrefix());
        } else {
            try {
                stepUnit = Double.parseDouble(stepUnitString);
            } catch (NumberFormatException e) {
                throw new IOException(MessageFormat.format(
                        "{0}.{1} must be a number: {2}",
                        profile.getPrefix(),
                        KEY_STEP_UNIT,
                        stepUnitString));
            }
        }
    }

    @Override
    public PhaseMonitor newInstance(ExecutionContext context) throws InterruptedException, IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return new LoggingExecutionMonitor(context, stepUnit);
    }
}
