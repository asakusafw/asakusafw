/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.gradle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures system properties.
 * @since 0.9.2
 */
public class PropertyConfigurator implements Consumer<BaseProject<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyConfigurator.class);

    private final Map<String, String> edit;

    /**
     * Creates a new instance.
     * @param edit edit
     */
    public PropertyConfigurator(Map<String, String> edit) {
        this.edit = new LinkedHashMap<>(edit);
    }

    /**
     * Returns a NO-OP configurator.
     * @return the created configurator
     */
    public static PropertyConfigurator nothing() {
        return of(Collections.emptyMap());
    }

    /**
     * Returns a system {@link PropertyConfigurator}.
     * @return a system {@link PropertyConfigurator}
     */
    public static PropertyConfigurator system() {
        return of(System.getProperties());
    }

    /**
     * Returns a configurator which edits the given property.
     * @param key the property name
     * @param value the property value
     * @return the created configurator
     */
    public static PropertyConfigurator of(String key, String value) {
        return of(Collections.singletonMap(key, value));
    }

    /**
     * Returns a configurator which edits the given property.
     * @param key the property name
     * @param path the target path (nullable)
     * @return the configurator
     */
    public static PropertyConfigurator of(String key, Path path) {
        return of(key, Optional.ofNullable(path).map(Path::toAbsolutePath).map(Path::toString).orElse(null));
    }

    /**
     * Returns a configurator which edits the given properties.
     * @param properties the properties
     * @return the created configurator
     */
    public static PropertyConfigurator of(Map<String, String> properties) {
        return new PropertyConfigurator(properties);
    }

    /**
     * Returns a configurator which edits the given properties.
     * @param properties the properties
     * @return the created configurator
     */
    public static PropertyConfigurator of(Properties properties) {
        return new PropertyConfigurator(properties.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> Optional.ofNullable(e.getValue()).map(String::valueOf).orElse(null))));
    }

    /**
     * Returns a configurator which edits the given URL of properties.
     * @param resource the resource URL
     * @return the created configurator
     */
    public static PropertyConfigurator of(URL resource) {
        LOG.debug("loading properties: {}", resource);
        Objects.requireNonNull(resource);
        Properties properties = new Properties();
        try (InputStream input = resource.openStream()) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "failed to load properties: {0}",
                    resource), e);
        }
        return of(properties);
    }

    /**
     * Returns a configurator which edits the given URL of properties.
     * @param resource the resource URL
     * @return the created configurator
     */
    public static PropertyConfigurator of(Optional<? extends URL> resource) {
        return resource.map(PropertyConfigurator::of).orElseGet(PropertyConfigurator::nothing);
    }

    /**
     * Returns a configurator which edits the given URL of properties.
     * @param resources the enumeration of resource URLs
     * @return the created configurator
     */
    public static PropertyConfigurator of(Enumeration<? extends URL> resources) {
        PropertyConfigurator results = nothing();
        while (resources.hasMoreElements()) {
            PropertyConfigurator next = of(resources.nextElement());
            results.edit.putAll(next.edit);
        }
        return results;
    }

    @Override
    public void accept(BaseProject<?> project) {
        project.withProperties(m -> m.putAll(edit));
    }
}
