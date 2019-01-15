/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.info.task;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a task.
 * @since 0.9.2
 */
public class TaskInfo {

    private static final String ID_ID = "id";

    private static final String ID_PHASE = "phase";

    private static final String ID_MODULE = "module";

    private static final String ID_PROFILE = "profile";

    private static final String ID_BLOCKERS = "blockers";

    @JsonProperty(ID_ID)
    private final String id;

    @JsonProperty(ID_PHASE)
    private final Phase phase;

    @JsonProperty(ID_MODULE)
    private final String moduleName;

    @JsonProperty(ID_PROFILE)
    private final String profileName;

    @JsonProperty(ID_BLOCKERS)
    @JsonInclude(Include.NON_EMPTY)
    private final Set<String> blockers;

    /**
     * Creates a new instance.
     * @param id the task ID
     * @param phase the task phase
     * @param moduleName the module name
     * @param profileName the profile name
     * @param blockers the blocker task ID
     */
    @JsonCreator
    public TaskInfo(
            @JsonProperty(ID_ID) String id,
            @JsonProperty(ID_PHASE) Phase phase,
            @JsonProperty(ID_MODULE) String moduleName,
            @JsonProperty(ID_PROFILE) String profileName,
            @JsonProperty(ID_BLOCKERS) Collection<String> blockers) {
        this.id = id;
        this.phase = phase;
        this.moduleName = moduleName;
        this.profileName = profileName;
        this.blockers = Optional.ofNullable(blockers)
                .map(it -> Collections.unmodifiableSet(new LinkedHashSet<>(blockers)))
                .orElse(Collections.emptySet());
    }

    /**
     * Returns the task ID.
     * @return the task ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the phase.
     * @return the phase
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * Returns the module name.
     * @return the module name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns the profile name.
     * @return the profile name, or {@code null} if it is not defined
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the blocker task IDs.
     * @return the blocker task IDs
     */
    public Set<String> getBlockers() {
        return blockers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(id);
        result = prime * result + Objects.hashCode(phase);
        result = prime * result + Objects.hashCode(moduleName);
        result = prime * result + Objects.hashCode(profileName);
        result = prime * result + Objects.hashCode(blockers);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TaskInfo other = (TaskInfo) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(phase, other.phase)
                && Objects.equals(moduleName, other.moduleName)
                && Objects.equals(profileName, other.profileName)
                && Objects.equals(blockers, other.blockers);
    }

    @Override
    public String toString() {
        return String.format(
                "Task(id=%s, phase=%s, module=%s, profile=%s, blockers=%s)",
                id, phase, moduleName, profileName, blockers);
    }

    /**
     * Represents kind of task phase.
     * @since 0.9.2
     */
    public enum Phase {

        /**
         * Initialization.
         */
        INITIALIZE,

        /**
         * Importing input data.
         */
        IMPORT,

        /**
         * Pre-processing input data.
         */
        PROLOGUE,

        /**
         * Processing data.
         */
        MAIN,

        /**
         * Post-processing output data.
         */
        EPILOGUE,

        /**
         * Exporting output data.
         */
        EXPORT,

        /**
         * Finalization.
         */
        FINALIZE,
        ;

        /**
         * Returns the symbol of this phase.
         * @return the symbol of this phase
         */
        public String getSymbol() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
