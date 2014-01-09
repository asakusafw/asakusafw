/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.util.HashMap;
import java.util.Map;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.core.vocabulary.StreamProcess;

/**
 * An abstract super class that describes exporter process using Local FS/WindGate.
 * Each subclass must satisfy following requirements:
 * <ul>
 * <li> declared as {@code public} </li>
 * <li> not declared as {@code abstract} </li>
 * <li> not declared type parameters </li>
 * <li> not declared any explicit constructors </li>
 * </ul>
 * @since 0.2.4
 */
public abstract class FsExporterDescription extends WindGateExporterDescription {

    /**
     * Returns the path of the export target (relative path from {@code resource.local.basePath} in the profile).
     * This path can include variables as <code>${&lt;variable-name&gt;}</code>.
     * @return target path
     */
    public abstract String getPath();

    /**
     * Returns an implementation of {@link DataModelStreamSupport} class.
     * @return the class of {@link DataModelStreamSupport}
     */
    public abstract Class<? extends DataModelStreamSupport<?>> getStreamSupport();

    @Override
    public final DriverScript getDriverScript() {
        String descriptionClass = getClass().getName();
        Class<?> modelType = getModelType();
        String path = getPath();
        Class<? extends DataModelStreamSupport<?>> supportClass = getStreamSupport();

        FsDescriptionUtil.checkCommonConfig(descriptionClass, modelType, supportClass, path);

        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put(FileProcess.FILE.key(), path);
        configuration.put(StreamProcess.STREAM_SUPPORT.key(), supportClass.getName());
        return new DriverScript(Constants.LOCAL_FILE_RESOURCE_NAME, configuration);
    }
}
