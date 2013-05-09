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
package com.asakusafw.compiler.tool.analysis;

/**
 * Constants for this package.
 * @since 0.2.6
 */
final class Constants {

    public static final String PATH_PREFIX = "opt/dsl-analysis/";

    public static final String PATH_BATCH = PATH_PREFIX + "batch/";

    public static final String PATH_JOBFLOW = PATH_PREFIX + "jobflow/";

    private Constants() {
        return;
    }
}
