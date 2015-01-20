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
package com.asakusafw.windgate.stream;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.StreamProcess;

/**
 * Common utilities for this package.
 * @since 0.2.4
 */
public final class StreamResourceUtil {

    static final WindGateLogger WGLOG = new WindGateStreamLogger(StreamResourceUtil.class);

    static final Logger LOG = LoggerFactory.getLogger(StreamResourceUtil.class);

    /**
     * Creates a new instance.
     * @param <T> type of data model object
     * @param classLoader the class loader
     * @param resourceName owner's resource name
     * @param script current script
     * @param direction the target direction
     * @return the created instance
     * @throws IOException if failed to create a valid support object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> DataModelStreamSupport<? super T> loadSupport(
            ClassLoader classLoader,
            String resourceName,
            ProcessScript<T> script,
            DriverScript.Kind direction) throws IOException {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        Map<String, String> configuration = script.getDriverScript(direction).getConfiguration();
        String supportClassName = configuration.get(StreamProcess.STREAM_SUPPORT.key());
        if (supportClassName == null) {
            WGLOG.error("E01001",
                    resourceName,
                    script.getName(),
                    StreamProcess.STREAM_SUPPORT.key(),
                    null);
            throw new IOException(MessageFormat.format(
                    "Invalid process configuration: key={3}, value={4} (resource={0}, process={1}, direction={2})",
                    resourceName,
                    script.getName(),
                    StreamProcess.STREAM_SUPPORT.key(),
                    null));
        }
        LOG.debug("Creating stream support object: {} (resource={}, process={})", new Object[] {
                supportClassName,
                resourceName,
                script.getName(),
        });
        Class<?> supportClass;
        try {
            supportClass = Class.forName(supportClassName, true, classLoader);
        } catch (ClassNotFoundException e) {
            WGLOG.error(e, "E01002",
                    resourceName,
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "Failed to load a stream support class: {2} (resource={0}, process={1})",
                    resourceName,
                    script.getName(),
                    supportClassName), e);
        }
        if (DataModelStreamSupport.class.isAssignableFrom(supportClass) == false) {
            WGLOG.error("E01002",
                    resourceName,
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "Stream support class must be a subtype of {3}: {2} (resource={0}, process={1})",
                    resourceName,
                    script.getName(),
                    supportClass.getName(),
                    DataModelStreamSupport.class.getName()));
        }
        DataModelStreamSupport<?> obj;
        try {
            obj = supportClass.asSubclass(DataModelStreamSupport.class).newInstance();
        } catch (Exception e) {
            WGLOG.error(e, "E01002",
                    resourceName,
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "Failed to create a stream support object: {2} (resource={0}, process={1})",
                    resourceName,
                    script.getName(),
                    supportClass.getName()), e);
        }
        if (obj.getSupportedType().isAssignableFrom(script.getDataClass()) == false) {
            WGLOG.error("E01002",
                    resourceName,
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "Stream support class {2} does not support data model: {3} (resource={0}, process={1})",
                    resourceName,
                    script.getName(),
                    supportClass.getName(),
                    script.getDataClass().getName()));
        }
        return (DataModelStreamSupport<? super T>) obj;
    }


    private StreamResourceUtil() {
        return;
    }
}
