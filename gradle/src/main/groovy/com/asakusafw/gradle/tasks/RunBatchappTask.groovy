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
package com.asakusafw.gradle.tasks

import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import com.asakusafw.gradle.tasks.internal.AbstractTestToolTask

/**
 * Gradle Task for Running Asakusa batch application.
 * @since 0.6.1
 * @version 0.7.3
 */
class RunBatchappTask extends AbstractTestToolTask {

    private static final String MAIN_CLASS = 'com.asakusafw.testdriver.tools.runner.BatchTestRunner'

    /**
     * The target batch ID.
     */
    @Input
    String batchId

    /**
     * Sets the target batch ID.
     * @param batchId the target batch ID
     */
    @Option(option = 'id', description = 'The target batch ID')
    void setBatchId(String batchId) {
        this.batchId = batchId
    }

    /**
     * Performs actions of this task.
     */
    @TaskAction
    void perform() {
        execute(MAIN_CLASS, ['--batch', getBatchId()])
    }
}
