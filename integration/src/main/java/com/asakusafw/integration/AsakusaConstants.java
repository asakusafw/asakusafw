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
package com.asakusafw.integration;

/**
 * Provides constants about Asakusa Framework.
 * @since 0.10.0
 */
public final class AsakusaConstants {

    /**
     * The path of portal command.
     */
    public static final String CMD_PORTAL = "bin/asakusa";

    /**
     * The path of YAESS command.
     */
    public static final String CMD_YAESS = "yaess/bin/yaess-batch.sh";

    private AsakusaConstants() {
        return;
    }
}
