/*
 * Copyright 2011-2021 Asakusa Framework Team.
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

import org.gradle.api.InvalidUserDataException
import org.gradle.process.ExecSpec

import com.asakusafw.gradle.tasks.internal.AbstractTestToolTask
import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * Gradle Task for using Asakusa testing tools.
 * @since 0.7.3
 * @version 0.9.1
 */
class TestToolTask extends AbstractTestToolTask {

    private static final String RUNNER_CLASS = 'com.asakusafw.testdriver.tools.runner.BatchTestRunner'

    private static final String TRUNCATOR_CLASS = 'com.asakusafw.testdriver.tools.runner.BatchTestTruncator'

    private static final String PREPARATOR_CLASS = 'com.asakusafw.testdriver.tools.runner.BatchTestPreparator'

    private static final String VERIFIER_CLASS = 'com.asakusafw.testdriver.tools.runner.BatchTestVerifier'

    private static final String COLLECTOR_CLASS = 'com.asakusafw.testdriver.tools.runner.BatchTestCollector'

    /**
     * Adds run operation to this task.
     * @param batchId the target batch ID
     */
    void run(String batchId) {
        if (batchId == null) {
            throw new InvalidUserDataException("run.batch must not be empty")
        }
        doLast {
            execute(RUNNER_CLASS, [
                '--batch', batchId,
            ])
        }
    }

    /**
     * Adds run operation to this task.
     * @param map {@code 'batch'}: the target batch ID
     */
    void run(Map<String, String> map) {
        String batchId = map.get('batch')
        if (batchId == null) {
            throw new IllegalArgumentException("'batch' must be specified")
        }
        run(batchId)
    }

    /**
     * Adds YAESS operation to this task.
     * @param batchId the target batch ID
     */
    void yaess(String batchId) {
        if (batchId == null) {
            throw new InvalidUserDataException("yaess.batch must not be empty")
        }
        doLast {
            if (System.env['ASAKUSA_HOME'] == null) {
                throw new InvalidUserDataException('ASAKUSA_HOME must be set')
            }
            File home = new File(System.env['ASAKUSA_HOME'])
            File cmd = new File(home, 'yaess/bin/yaess-batch.sh')
            project.exec { ExecSpec spec ->
                spec.commandLine cmd.getAbsolutePath()
                spec.args batchId
                ResolutionUtils.resolveToStringMap(getBatchArguments()).each { String key, String value ->
                    spec.args '-A', "${key}=${value}"
                }
            }
        }
    }

    /**
     * Adds YAESS operation to this task.
     * @param map {@code 'batch'}: the target batch ID
     */
    void yaess(Map<String, String> map) {
        String batchId = map.get('batch')
        if (batchId == null) {
            throw new IllegalArgumentException("'batch' must be specified")
        }
        yaess(batchId)
    }

    /**
     * Adds prepare operation to this task.
     * @param descriptionClass the target description class name
     */
    void clean(String descriptionClass) {
        if (descriptionClass == null) {
            throw new InvalidUserDataException("clean.description must not be empty")
        }
        doLast {
            execute(TRUNCATOR_CLASS, [
                '--description', descriptionClass,
            ])
        }
    }

    /**
     * Adds prepare operation to this task.
     * @param map {@code 'description'}: the target description class
     */
    void clean(Map<String, String> map) {
        String desc = map.get('description')
        if (desc == null) {
            throw new IllegalArgumentException("'description' must be specified")
        }
        clean(desc)
    }

    /**
     * Adds prepare operation to this task.
     * @param descriptionClass the target importer description class name
     * @param dataPath the input data URI
     */
    void prepare(String descriptionClass, String dataPath) {
        if (descriptionClass == null) {
            throw new InvalidUserDataException("prepare.importer must not be empty")
        }
        if (dataPath == null) {
            throw new InvalidUserDataException("prepare.data must not be empty")
        }
        doLast {
            execute(PREPARATOR_CLASS, [
                '--importer', descriptionClass,
                '--data', dataPath,
            ])
        }
    }

    /**
     * Adds prepare operation to this task.
     * @param map {@code 'importer'}: the target importer description class,
     *     {@code 'data'}: the input data URI
     */
    void prepare(Map<String, String> map) {
        String desc = map.get('importer')
        String data = map.get('data')
        if (desc == null) {
            throw new IllegalArgumentException("'importer' must be specified")
        }
        if (data == null) {
            throw new IllegalArgumentException("'data' must be specified")
        }
        prepare(desc, data)
    }

    /**
     * Adds verify operation to this task.
     * @param descriptionClass the target exporter description class name
     * @param dataPath the expected data URI
     * @param rulePath the verification rule URI
     */
    void verify(String descriptionClass, String dataPath, String rulePath) {
        if (descriptionClass == null) {
            throw new InvalidUserDataException("verify.exporter must not be empty")
        }
        if (dataPath == null) {
            throw new InvalidUserDataException("verify.data must not be empty")
        }
        if (rulePath == null) {
            throw new InvalidUserDataException("verify.rule must not be empty")
        }
        doLast {
            execute(VERIFIER_CLASS, [
                '--exporter', descriptionClass,
                '--data', dataPath,
                '--rule', rulePath,
            ])
        }
    }

    /**
     * Adds verify operation to this task.
     * @param map {@code 'exporter'}: the target exporter description class,
     *     {@code 'data'}: the expected data URI,
     *     {@code 'rule'}: the verification rule URI
     */
    void verify(Map<String, String> map) {
        String desc = map.get('exporter')
        String data = map.get('data')
        String rule = map.get('rule')
        if (desc == null) {
            throw new IllegalArgumentException("'exporter' must be specified")
        }
        if (data == null) {
            throw new IllegalArgumentException("'data' must be specified")
        }
        if (rule == null) {
            throw new IllegalArgumentException("'rule' must be specified")
        }
        verify(desc, data, rule)
    }

    /**
     * Adds dump operation to this task.
     * @param descriptionClass the target importer description class name
     * @param dataPath the input data URI
     * @since 0.9.1
     */
    void dump(String descriptionClass, String outputPath) {
        if (descriptionClass == null) {
            throw new InvalidUserDataException("dump.exporter must not be empty")
        }
        if (outputPath == null) {
            throw new InvalidUserDataException("dump.output must not be empty")
        }
        doLast {
            execute(COLLECTOR_CLASS, [
                '--exporter', descriptionClass,
                '--output', outputPath,
            ])
        }
    }

    /**
     * Adds dump operation to this task.
     * @param map {@code 'exporter'}: the target exporter description class,
     *     {@code 'output'}: the output URI
     * @since 0.9.1
     */
    void dump(Map<String, String> map) {
        String desc = map.get('exporter')
        String output = map.get('output')
        if (desc == null) {
            throw new IllegalArgumentException("'exporter' must be specified")
        }
        if (output == null) {
            throw new IllegalArgumentException("'output' must be specified")
        }
        dump(desc, output)
    }
}
