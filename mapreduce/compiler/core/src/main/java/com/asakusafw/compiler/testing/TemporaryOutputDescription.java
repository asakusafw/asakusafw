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
package com.asakusafw.compiler.testing;

import java.text.MessageFormat;

import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * An {@link ExporterDescription} to output result to temporary storage.
 * @since 0.2.5
 */
public abstract class TemporaryOutputDescription implements ExporterDescription {

    /**
     * Returns the export target path prefix.
     * <p>
     * Each path segment must be separated by {@code "/"}.
     * The file name (the last segment of the path prefix) must consist of digits and alphabets,
     * and must end with {@code "-*"}.
     * </p>
     * @return the export target path prefix
     */
    public abstract String getPathPrefix();

    @Override
    public String toString() {
        return MessageFormat.format(
                "TemporaryExporter({0})", //$NON-NLS-1$
                getPathPrefix());
    }
}
