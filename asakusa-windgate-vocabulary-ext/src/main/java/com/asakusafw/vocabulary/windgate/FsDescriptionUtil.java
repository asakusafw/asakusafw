/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.text.MessageFormat;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;

/**
 * Common utilities for Local FS.
 * @since 0.2.2
 */
final class FsDescriptionUtil {

    static void checkCommonConfig(
            String descriptionClass,
            Class<?> modelType,
            Class<? extends DataModelStreamSupport<?>> supportClass,
            String path) {
        if (path == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "{1} must not be null: {0}",
                    descriptionClass,
                    "getPath()"));
        }
        if (path.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "{1} must not be empty string: {0}",
                    descriptionClass,
                    "getPath()"));
        }
        if (supportClass == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "{1} must not be null: {0}",
                    descriptionClass,
                    "getStreamSupport()"));
        }
        DataModelStreamSupport<?> support;
        try {
            support = supportClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to instantiate {1}: {0}",
                    descriptionClass,
                    supportClass.getName()), e);
        }
        if (support.getSupportedType().isAssignableFrom(modelType) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "{1} must support {2}: {0}",
                    descriptionClass,
                    supportClass.getName(),
                    modelType.getName()));
        }
    }

    private FsDescriptionUtil() {
        return;
    }
}
