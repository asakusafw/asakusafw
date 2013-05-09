/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.windgate.core;

/**
 * A script describes transfering data from source to drain.
 * @param <T> the type to be processed
 * @since 0.2.2
 */
public class ProcessScript<T> {

    /**
     * Key name of data class.
     * @see #getDataClass()
     */
    public static final String KEY_DATA_CLASS = "class";

    /**
     * Key name of process type.
     * @see #getProcessType()
     */
    public static final String KEY_PROCESS_TYPE = "process";

    private final String name;

    private final String processType;

    private final Class<T> dataClass;

    private final DriverScript sourceScript;

    private final DriverScript drainScript;

    /**
     * Creates a new instance.
     * @param name the name of this process
     * @param processType the kind of this process
     * @param dataClass the data model class to be transferred
     * @param sourceScript the script which describes a source driver
     * @param drainScript the script which describes a drain driver
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ProcessScript(
            String name,
            String processType,
            Class<T> dataClass,
            DriverScript sourceScript,
            DriverScript drainScript) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (processType == null) {
            throw new IllegalArgumentException("processName must not be null"); //$NON-NLS-1$
        }
        if (dataClass == null) {
            throw new IllegalArgumentException("dataClass must not be null"); //$NON-NLS-1$
        }
        if (sourceScript == null) {
            throw new IllegalArgumentException("sourceScript must not be null"); //$NON-NLS-1$
        }
        if (drainScript == null) {
            throw new IllegalArgumentException("drainScript must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.processType = processType;
        this.dataClass = dataClass;
        this.sourceScript = sourceScript;
        this.drainScript = drainScript;
    }

    /**
     * Returns the name of this process.
     * @return the name of this process
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the kind of this process.
     * This represents a {@code process name} defined in profile.
     * @return the kind of this process
     */
    public String getProcessType() {
        return processType;
    }

    /**
     * Returns the data model class to be transferred.
     * @return the data model class
     */
    public Class<T> getDataClass() {
        return dataClass;
    }

    /**
     * Returns the script which describes a source driver.
     * @return the source script
     */
    public DriverScript getSourceScript() {
        return sourceScript;
    }

    /**
     * Returns the script which describes a drain driver.
     * @return the drain script
     */
    public DriverScript getDrainScript() {
        return drainScript;
    }

    /**
     * Returns the script which describes a specified driver.
     * @param kind the driver kind
     * @return the target script
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DriverScript getDriverScript(DriverScript.Kind kind) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        switch (kind) {
        case SOURCE:
            return getSourceScript();
        case DRAIN:
            return getDrainScript();
        default:
            throw new AssertionError(kind);
        }
    }
}
