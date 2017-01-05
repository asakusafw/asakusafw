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
package com.asakusafw.compiler.trace;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.trace.model.TraceSetting;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;

/**
 * An implementation of {@link FlowElementAttribute} which holds tracepoint information.
 * @since 0.5.1
 */
public class TraceSettingAttribute implements FlowElementAttribute {

    private final TraceSetting setting;

    private final int serialNumber;

    /**
     * Creates a new instance.
     * @param setting tracepoint setting
     * @param serialNumber the serial number for the tracepoint target
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TraceSettingAttribute(TraceSetting setting, int serialNumber) {
        Precondition.checkMustNotBeNull(setting, "setting"); //$NON-NLS-1$
        this.setting = setting;
        this.serialNumber = serialNumber;
    }

    @Override
    public Class<? extends FlowElementAttribute> getDeclaringClass() {
        return TraceSettingAttribute.class;
    }

    /**
     * Returns the tracepoint setting.
     * @return the setting
     */
    public TraceSetting getSetting() {
        return setting;
    }

    /**
     * Returns the serial number of the tracepoint target.
     * @return the serial number
     */
    public int getSerialNumber() {
        return serialNumber;
    }
}
