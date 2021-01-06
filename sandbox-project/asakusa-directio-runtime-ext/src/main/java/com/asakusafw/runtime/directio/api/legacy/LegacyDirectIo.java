/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.api.legacy;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.core.legacy.RuntimeResource;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.api.DirectIo;
import com.asakusafw.runtime.directio.api.DirectIoApi;
import com.asakusafw.runtime.directio.api.DirectIoDelegate;
import com.asakusafw.runtime.io.ModelInput;

/**
 * A legacy implementation of Direct I/O API entry class.
 * @since 0.9.0
 */
public final class LegacyDirectIo {

    /**
     * The API of this implementation.
     */
    public static final DirectIoApi API = new DirectIoApi() {
        @Override
        public <T> ModelInput<T> open(
                Class<? extends DataFormat<T>> formatClass,
                String basePath, String resourcePattern) throws IOException {
            return LegacyDirectIo.open(formatClass, basePath, resourcePattern);
        }
    };

    static final ThreadLocal<DirectIoDelegate> DELEGATE = ThreadLocal.withInitial(() -> {
        throw new IllegalStateException("Direct I/O API is not yet initialized");
    });

    private LegacyDirectIo() {
        return;
    }

    /**
     * Returns data model objects from Direct I/O data sources.
     * @param <T> the data model object type
     * @param formatClass the Direct I/O data format class
     * @param basePath the base path (must not contain variables)
     * @param resourcePattern the resource pattern (must not contain variables)
     * @return the data model objects
     * @throws IOException if failed to open data model objects on the data source
     * @see DirectIo#open(Class, String, String)
     */
    public static <T> ModelInput<T> open(
            Class<? extends DataFormat<T>> formatClass,
            String basePath,
            String resourcePattern) throws IOException {
        return DELEGATE.get().open(formatClass, basePath, resourcePattern);
    }

    static void set(Configuration configuration) {
        DELEGATE.set(new DirectIoDelegate(configuration));
    }

    static void clear() {
        DELEGATE.remove();
    }

    /**
     * Initializes {@link LegacyDirectIo}.
     * @since 0.9.0
     */
    public static final class Initializer extends Configured implements RuntimeResource {

        @Override
        public void setup(ResourceConfiguration configuration) {
            LegacyDirectIo.set(getConf());
        }

        @Override
        public void cleanup(ResourceConfiguration configuration) {
            LegacyDirectIo.clear();
        }
    }
}
