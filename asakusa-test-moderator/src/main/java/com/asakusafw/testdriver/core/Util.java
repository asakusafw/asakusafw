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
package com.asakusafw.testdriver.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utilities for this package.
 * @since 0.2.0
 */
final class Util {

    static final Logger LOG = LoggerFactory.getLogger(Util.class);

    static <T> List<T> loadService(Class<T> spi, ClassLoader loader) {
        assert spi != null;
        assert loader != null;
        LOG.info("{}のプラグインを登録しています", spi.getSimpleName());
        ServiceLoader<T> services = ServiceLoader.load(spi, loader);
        List<T> results = new ArrayList<T>();
        for (T service : services) {
            LOG.debug("Activating {}", service.getClass().getName());
            results.add(service);
        }
        Collections.sort(results, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
        });
        return results;
    }

    private Util() {
        return;
    }
}
