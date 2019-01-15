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
package com.asakusafw.workflow.hadoop;

final class Constants {

    /**
     * The Hadoop bridge script location (relative from framework installation path).
     */
    public static final String PATH_BRIDGE_SCRIPT = "tools/libexec/workflow/hadoop-bridge"; //$NON-NLS-1$

    /**
     * {@link #PATH_BRIDGE_SCRIPT} for Windows.
     */
    public static final String PATH_BRIDGE_SCRIPT_WINDOWS = PATH_BRIDGE_SCRIPT + ".cmd"; //$NON-NLS-1$

    /**
     * The Hadoop bridge library location (relative from framework installation path).
     */
    public static final String PATH_BRIDGE_LIBRARY = "tools/lib/asakusa-workflow-hadoop.jar"; //$NON-NLS-1$

    /**
     * The Asakusa application launcher library location (relative from framework installation path).
     */
    public static final String PATH_LAUNCHER_LIBRARY = "core/lib/asakusa-runtime-all.jar";

    private Constants() {
        return;
    }
}
