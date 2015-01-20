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
package com.asakusafw.yaess.tools.log;

import java.io.IOException;
import java.util.Map;

import com.asakusafw.utils.io.Source;

/**
 * Provides {@link YaessLogRecord}s.
 * @since 0.6.2
 */
public interface YaessLogInput {

    /**
     * Returns the option names and their descriptions.
     * @return option name and description pair
     */
    Map<String, String> getOptionsInformation();

    /**
     * Creates a source which provides {@link YaessLogRecord}s.
     * @param options the source options
     * @return the source which provides log records
     * @throws IllegalArgumentException if the specified options are invalid
     * @throws IOException if failed to create source by I/O error
     * @throws InterruptedException if interrupted while creating source
     */
    Source<? extends YaessLogRecord> createSource(
            Map<String, String> options) throws IOException, InterruptedException;
}
