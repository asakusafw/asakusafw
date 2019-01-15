/*
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
package com.asakusafw.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * An abstract super interface of Gradle plug-in participants.
 * @since 0.9.0
 */
interface PluginParticipant {

    /**
     * Returns the name of this participant.
     * @return the participant name
     */
    String getName()

    /**
     * Returns a descriptor of the target participant.
     * The participant will be applied by {@code project.apply plugin: participant.getDescriptor()}.
     * @return a participant descriptor
     */
    Object getDescriptor()
}
