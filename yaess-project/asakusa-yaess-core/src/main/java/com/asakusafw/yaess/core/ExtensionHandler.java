/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.io.IOException;

/**
 * Handles argument pair and creates its {@link Extension}.
 * @since 0.8.0
 */
@FunctionalInterface
public interface ExtensionHandler {

    /**
     * Handles the tag and value pair, and returns the corresponded {@link Extension} if it exists.
     * @param tag the tag
     * @param value the value
     * @return the corresponded {@link Extension}, or {@code null} if this can not handle the target label
     * @throws IOException if I/O error occurred while creating the extension
     */
    Extension handle(String tag, String value) throws IOException;
}
