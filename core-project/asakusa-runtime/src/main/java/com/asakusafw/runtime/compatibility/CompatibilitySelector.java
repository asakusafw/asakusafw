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
package com.asakusafw.runtime.compatibility;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.runtime.compatibility.hadoop.CompatibilityHadoop;
import com.asakusafw.runtime.compatibility.hadoop.CoreCompatibility.FrameworkVersion;
import com.asakusafw.runtime.compatibility.hadoop.FileSystemCompatibilityHadoop;
import com.asakusafw.runtime.compatibility.hadoop.JobCompatibilityHadoop;
import com.asakusafw.runtime.compatibility.hadoop.SequenceFileCompatibilityHadoop;
import com.asakusafw.runtime.compatibility.hadoop2.FileSystemCompatibilityHadoop2;
import com.asakusafw.runtime.compatibility.hadoop2.JobCompatibilityHadoop2;
import com.asakusafw.runtime.compatibility.hadoop2.SequenceFileCompatibilityHadoop2;

/**
 * Selects implementations of {@link CompatibilityHadoop} objects.
 */
final class CompatibilitySelector {

    static final Map<Class<?>, Class<?>> IMPLEMENTATIONS;
    static {
        FrameworkVersion version = FrameworkVersion.get();
        Map<Class<?>, Class<?>> map = new HashMap<>();
        if (version.isCompatibleTo(FrameworkVersion.HADOOP_V2)) {
            map.put(FileSystemCompatibilityHadoop.class, FileSystemCompatibilityHadoop2.class);
            map.put(JobCompatibilityHadoop.class, JobCompatibilityHadoop2.class);
            map.put(SequenceFileCompatibilityHadoop.class, SequenceFileCompatibilityHadoop2.class);
        }
        IMPLEMENTATIONS = Collections.unmodifiableMap(map);
    }

    private CompatibilitySelector() {
        return;
    }

    /**
     * Returns an implementation for the target type.
     * @param <T> the interface type
     * @param type the interface type
     * @return the corresponded implementation
     * @throws IllegalStateException if failed to create the implementation object
     */
    public static <T extends CompatibilityHadoop> T getImplementation(Class<T> type) {
        Class<?> implementation = IMPLEMENTATIONS.get(type);
        if (implementation == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "missing compatibility implementation for {0}: {1}",
                    FrameworkVersion.get(),
                    type.getName()));
        }
        try {
            return type.cast(implementation.newInstance());
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    "error occurred while instantiating compatibility implementation: {0}",
                    type.getName()));
        }
    }
}
