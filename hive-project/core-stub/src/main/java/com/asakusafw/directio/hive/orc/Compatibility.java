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
package com.asakusafw.directio.hive.orc;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat.InputContext;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * compatibility layer for Direct I/O ORC File support.
 * @since 0.10.3
 */
public abstract class Compatibility {

    private static final Log LOG = LogFactory.getLog(Compatibility.class);

    private static final Compatibility INSTANCE;
    static {
        SortedMap<Integer, Compatibility> candidates = new TreeMap<>();
        Iterator<Compatibility> services = ServiceLoader.load(Compatibility.class).iterator();
        while (true) {
            try {
                if (!services.hasNext()) {
                    break;
                }
                Compatibility candidate = services.next();
                int priority = candidate.getPriority();
                if (priority >= 0) {
                    candidates.put(priority, candidate);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "{0}: priority={1}",
                            candidate.getClass().getName(),
                            priority));
                }
            } catch (ServiceConfigurationError e) {
                LOG.warn("cannot initialize ORC file compatibility layer", e);
            }
        }
        Compatibility instance = null;
        if (!candidates.isEmpty()) {
            instance = candidates.get(candidates.lastKey());
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        "using ORC file compatibility layer: {0}",
                        instance.getClass().getName()));
            }
        } else {
            LOG.warn("ORC file compatibility layer is not available");
        }
        INSTANCE = instance;
    }

    /**
     * returns the compatibility instance for this environment.
     * @return the compatibility instance
     */
    public static Compatibility getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ORC file compatibility layer is not initialized");
        }
        return INSTANCE;
    }

    /**
     * returns the priority of this compatibility layer.
     * @return the priority (higher value is higher priority), or negative value if it is not available
     */
    protected abstract int getPriority();

    /**
     * returns extra table property map from the format configuration.
     * @param format the format configuration
     * @return the extra table property map
     */
    protected abstract Map<String, String> collectPropertyMap(OrcFormatConfiguration format);

    /**
     * delegate from {@link AbstractOrcFileFormat#computeInputFragments(InputContext)}.
     * @param format the source format
     * @param context the current input context
     * @return the computed result
     * @throws IOException if failed to compute fragments by I/O error
     * @throws InterruptedException if interrupted while computing fragments
     */
    public abstract List<DirectInputFragment> computeInputFragments(
            AbstractOrcFileFormat<?> format,
            InputContext context) throws IOException, InterruptedException;

    /**
     * delegate from {@link AbstractOrcFileFormat#createInput(Class, FileSystem, Path, long, long, Counter)}.
     * @param <T> the data type
     * @param format the source format
     * @param dataType the target data type
     * @param fileSystem the file system to open the target path
     * @param path the path to the target file
     * @param offset starting stream offset
     * @param fragmentSize suggested fragment bytes count, or {@code -1} as infinite
     * @param counter the current counter
     * @return the created reader
     * @throws IOException if failed to create reader
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if this does not support target property sequence,
     *     or any parameter is {@code null}
     */
    public abstract <T> ModelInput<T> createInput(
            AbstractOrcFileFormat<T> format,
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            long offset,
            long fragmentSize,
            Counter counter) throws IOException, InterruptedException;

    /**
     * delegate from {@link AbstractOrcFileFormat#createOutput(Class, FileSystem, Path, Counter)}.
     * @param <T> the data type
     * @param format the source format
     * @param dataType the target data type
     * @param fileSystem the file system to open the target path
     * @param path the path to the target file
     * @param counter the current counter
     * @return the created writer
     * @throws IOException if failed to create writer
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if this does not support property sequence,
     *     or any parameter is {@code null}
     */
    public abstract <T> ModelOutput<T> createOutput(
            AbstractOrcFileFormat<T> format,
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            Counter counter) throws IOException, InterruptedException;

    /**
     * returns the version symbol if the name.
     * @param name the version name
     * @return the related symbol, or empty if it is not found
     */
    public abstract Optional<? extends Enum<?>> findVersionId(String name);

    /**
     * returns the compression kind class.
     * @return the compression kind class
     */
    public abstract Class<? extends Enum<?>> getCompressionKindClass();
}
