/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage;

import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;

/**
 * A program entry for launching Asakusa tools.
 * @since 0.1.0
 * @version 0.7.0
 * @see ApplicationLauncher
 */
public final class ToolLauncher {

    /**
     * The exit status value for successful jobs.
     */
    public static final int JOB_SUCCEEDED = 0;

    /**
     * The exit status value for failed jobs.
     */
    public static final int JOB_FAILED = 1;

    /**
     * The exit status value for launching errors.
     */
    public static final int LAUNCH_ERROR = -2;

    /**
     * The exit status value for client errors.
     */
    public static final int CLIENT_ERROR = -1;

    /**
     *
     * Launches the tool.
     * @param args {@code Tool-class-name [optional-arguments]}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static void main(String...args) {
        if (args == null) {
            throw new IllegalArgumentException("args must not be null"); //$NON-NLS-1$
        }
        int result = ApplicationLauncher.exec(args);
        System.exit(result);
    }

    private ToolLauncher() {
        return;
    }
}
