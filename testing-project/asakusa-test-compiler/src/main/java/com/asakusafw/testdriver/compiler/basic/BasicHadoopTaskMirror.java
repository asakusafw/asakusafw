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
package com.asakusafw.testdriver.compiler.basic;

import java.util.Objects;

import com.asakusafw.testdriver.compiler.HadoopTaskMirror;

/**
 * A basic implementation of {@link HadoopTaskMirror}.
 * @since 0.8.0
 */
public class BasicHadoopTaskMirror extends BasicTaskMirror implements HadoopTaskMirror {

    private final String moduleName;

    private final String className;

    /**
     * Creates a new instance.
     * @param className the class name
     */
    public BasicHadoopTaskMirror(String className) {
        this(DEFAULT_MODULE_NAME, className);
    }

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param className the class name
     */
    public BasicHadoopTaskMirror(String moduleName, String className) {
        Objects.requireNonNull(moduleName);
        Objects.requireNonNull(className);
        this.moduleName = moduleName;
        this.className = className;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
