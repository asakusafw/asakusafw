/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.asakusafw.yaess.core.Blob;
import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionScript;

/**
 * Utilities for {@link Blob}.
 * @since 0.8.0
 */
public final class BlobUtil {

    private BlobUtil() {
        return;
    }

    /**
     * Builds {@link ExecutionContext#getExtensions() extensions} for the script.
     * @param context the current context
     * @param script the target script
     * @return the extended arguments
     */
    public static Map<String, Blob> getExtensions(ExecutionContext context, ExecutionScript script) {
        Map<String, Blob> results = new LinkedHashMap<>();
        Map<String, Blob> extensions = context.getExtensions();
        Set<String> supported = script.getSupportedExtensions();
        for (Map.Entry<String, Blob> entry : extensions.entrySet()) {
            if (supported.contains(entry.getKey())) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }

    /**
     * Returns the file suffix for the extension.
     * @param extension the extension name
     * @param blob the target BLOB
     * @return the target file name suffix
     */
    public static String getSuffix(String extension, Blob blob) {
        return String.format(".%s.%s", extension, blob.getFileExtension());
    }
}
