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
package com.asakusafw.windgate.stream.file;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.util.ProcessUtil;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.stream.StreamDrainDriver;
import com.asakusafw.windgate.stream.StreamSourceDriver;

/**
 * An implementation of {@link ResourceManipulator} using local file system.
 * @since 0.2.4
 */
public class FileResourceManipulator extends ResourceManipulator {

    static final Logger LOG = LoggerFactory.getLogger(FileResourceManipulator.class);

    private final FileProfile profile;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param profile current profile
     * @param arguments current arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileResourceManipulator(FileProfile profile, ParameterList arguments) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        this.arguments = arguments;
    }

    @Override
    public String getName() {
        return profile.getResourceName();
    }

    @Override
    public void cleanupSource(ProcessScript<?> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.SOURCE);
        delete(path);
    }

    @Override
    public void cleanupDrain(ProcessScript<?> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.DRAIN);
        delete(path);
    }

    private void delete(File path) {
        assert path != null;
        LOG.info("Deleting file: {}",
                path.getAbsolutePath());
        if (path.delete()) {
            LOG.info("Deleted file: {}",
                    path.getAbsolutePath());
        } else {
            LOG.info("Failed to delete file: {}",
                    path.getAbsolutePath());
        }
    }

    @Override
    public <T> SourceDriver<T> createSourceForSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.SOURCE);
        DataModelStreamSupport<? super T> support = FileResourceUtil.loadSupport(
                profile, script, DriverScript.Kind.SOURCE);
        T model = ProcessUtil.newDataModel(profile.getResourceName(), script);
        FileInputStreamProvider provider = new FileInputStreamProvider(path);
        return new StreamSourceDriver<T>(profile.getResourceName(), script.getName(), provider, support, model);
    }

    @Override
    public <T> DrainDriver<T> createDrainForSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.SOURCE);
        DataModelStreamSupport<? super T> support = FileResourceUtil.loadSupport(
                profile, script, DriverScript.Kind.SOURCE);
        FileOutputStreamProvider provider = new FileOutputStreamProvider(path);
        return new StreamDrainDriver<T>(profile.getResourceName(), script.getName(), provider, support);
    }

    @Override
    public <T> SourceDriver<T> createSourceForDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.DRAIN);
        DataModelStreamSupport<? super T> support = FileResourceUtil.loadSupport(
                profile, script, DriverScript.Kind.DRAIN);
        T model = ProcessUtil.newDataModel(profile.getResourceName(), script);
        FileInputStreamProvider provider = new FileInputStreamProvider(path);
        return new StreamSourceDriver<T>(profile.getResourceName(), script.getName(), provider, support, model);
    }

    @Override
    public <T> DrainDriver<T> createDrainForDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        File path = FileResourceUtil.getPath(profile, script, arguments, DriverScript.Kind.DRAIN);
        DataModelStreamSupport<? super T> support = FileResourceUtil.loadSupport(
                profile, script, DriverScript.Kind.DRAIN);
        FileOutputStreamProvider provider = new FileOutputStreamProvider(path);
        return new StreamDrainDriver<T>(profile.getResourceName(), script.getName(), provider, support);
    }
}
