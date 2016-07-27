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
package com.asakusafw.vocabulary.windgate;

/**
 * The common constants of WindGate vocabularies.
 * @since 0.2.2
 * @version 0.9.0
 */
public final class Constants {

    /**
     * The module name.
     * @since 0.9.0
     */
    public static final String MODULE_NAME = "windgate";

    /**
     * The abstract resource name of JDBC.
     */
    public static final String JDBC_RESOURCE_NAME = "jdbc"; //$NON-NLS-1$

    /**
     * The abstract resource name of hadoop file systems.
     */
    public static final String HADOOP_FILE_RESOURCE_NAME = "hadoop"; //$NON-NLS-1$

    /**
     * The abstract resource name of local file systems.
     */
    public static final String LOCAL_FILE_RESOURCE_NAME = "local"; //$NON-NLS-1$

    /**
     * The abstract resource name of void inputs/outputs.
     * @since 0.8.1
     */
    public static final String VOID_RESOURCE_NAME = "void"; //$NON-NLS-1$

    /**
     * The default process provider name.
     */
    public static final String DEFAULT_PROCESS_NAME = "basic"; //$NON-NLS-1$

    private Constants() {
        return;
    }
}
