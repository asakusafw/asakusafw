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
package com.asakusafw.vocabulary.windgate;

import java.text.MessageFormat;

import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;

/**
 * Common utilities for Local FS.
 * @since 0.2.4
 */
final class FsDescriptionUtil {

    static void checkCommonConfig(
            String descriptionClass,
            Class<?> modelType,
            Class<? extends DataModelStreamSupport<?>> supportClass,
            String path) {
        if (path == null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("FsDescriptionUtil.errorNullProperty"), //$NON-NLS-1$
                    descriptionClass,
                    "getPath()")); //$NON-NLS-1$
        }
        if (path.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("FsDescriptionUtil.errorEmptyStringProperty"), //$NON-NLS-1$
                    descriptionClass,
                    "getPath()")); //$NON-NLS-1$
        }
        if (supportClass == null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("FsDescriptionUtil.errorNullProperty"), //$NON-NLS-1$
                    descriptionClass,
                    "getStreamSupport()")); //$NON-NLS-1$
        }
        DataModelStreamSupport<?> support;
        try {
            support = supportClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("FsDescriptionUtil.errorFailedToInstantiate"), //$NON-NLS-1$
                    descriptionClass,
                    supportClass.getName()), e);
        }
        if (support.getSupportedType().isAssignableFrom(modelType) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("FsDescriptionUtil.errorIncompatibleDataType"), //$NON-NLS-1$
                    descriptionClass,
                    supportClass.getName(),
                    modelType.getName()));
        }
    }

    private FsDescriptionUtil() {
        return;
    }
}
