/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.windgate.stream.file;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.stream.StreamResourceUtil;
import com.asakusafw.windgate.stream.WindGateStreamLogger;

/**
 * Utilities for this package.
 * @since 0.2.4
 */
final class FileResourceUtil {

    static final WindGateLogger WGLOG = new WindGateStreamLogger(FileResourceUtil.class);

    static final Logger LOG = LoggerFactory.getLogger(FileResourceUtil.class);

    /**
     * Creates a new data model instance.
     * @param <T> type of data model object
     * @param profile current profile
     * @param script current script
     * @param direction the target direction
     * @return the created instance
     * @throws IOException if failed to create a valid support object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T> DataModelStreamSupport<? super T> loadSupport(
            FileProfile profile,
            ProcessScript<T> script,
            DriverScript.Kind direction) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        return StreamResourceUtil.loadSupport(profile.getClassLoader(), profile.getResourceName(), script, direction);
    }

    /**
     * Extracts the path from current process.
     * @param profile current profile
     * @param process target process
     * @param arguments current parameters
     * @param direction target direction in process
     * @return the extracted path
     * @throws IOException if failed to extract path from the process configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static File getPath(
            FileProfile profile,
            ProcessScript<?> process,
            ParameterList arguments,
            DriverScript.Kind direction) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (process == null) {
            throw new IllegalArgumentException("process must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        Map<String, String> configuration = process.getDriverScript(direction).getConfiguration();
        String rawPath = configuration.get(FileProcess.FILE.key());
        if (rawPath == null) {
            WGLOG.error("E01001",
                    profile.getResourceName(),
                    process.getName(),
                    direction.prefix,
                    FileProcess.FILE.key(),
                    null);
            throw new IOException(MessageFormat.format(
                    "Resource \"{0}\" requires config \"{3}\" (process={1}, direction={2})",
                    profile.getResourceName(),
                    process.getName(),
                    direction,
                    FileProcess.FILE.key()));
        }
        LOG.debug("Resolving variables in path: {}",
                rawPath);
        String path;
        try {
            path = arguments.replace(rawPath, true);
        } catch (IllegalArgumentException e) {
            WGLOG.error(e, "E01001",
                    profile.getResourceName(),
                    process.getName(),
                    direction.prefix,
                    FileProcess.FILE.key(),
                    rawPath);
            throw new IOException(MessageFormat.format(
                    "Failed to resolve variables in path: {4} (resource={0}, process={1}, direction={2})",
                    profile.getResourceName(),
                    process.getName(),
                    direction,
                    FileProcess.FILE.key(),
                    rawPath), e);
        }
        return new File(profile.getBasePath(), path);
    }

    private FileResourceUtil() {
        return;
    }
}
