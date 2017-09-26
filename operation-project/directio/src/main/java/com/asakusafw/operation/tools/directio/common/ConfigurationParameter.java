/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.directio.common;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.jcommander.common.LocalPath;
import com.beust.jcommander.Parameter;

/**
 * Handles parameters about Hadoop configuration.
 * @since 0.10.0
 */
public class ConfigurationParameter {

    static final Logger LOG = LoggerFactory.getLogger(ConfigurationParameter.class);

    static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME";

    static final String PATH_CONFIGURATION = "core/conf/asakusa-resources.xml";

    @Parameter(
            names = { "--conf", "--configuration", },
            description = "Hadoop custom configuration file path.",
            required = false
    )
    String path = Optional.ofNullable(System.getenv(ENV_ASAKUSA_HOME))
            .map(Paths::get)
            .map(it -> it.resolve(PATH_CONFIGURATION))
            .map(Path::toString)
            .orElse(null);

    private Configuration configuration;

    /**
     * Returns the configuration path.
     * @return the configuration path
     */
    public Optional<Path> getPath() {
        return Optional.ofNullable(path).map(LocalPath::of);
    }

    /**
     * Returns the configuration.
     * @return the configuration
     */
    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
            if (path == null) {
                LOG.warn("environment variable {} is not defined", ENV_ASAKUSA_HOME);
            } else {
                Path p = LocalPath.of(path);
                LOG.debug("loading configuration: {}", p);
                if (Files.isRegularFile(p)) {
                    try {
                        configuration.addResource(p.toUri().toURL());
                    } catch (MalformedURLException e) {
                        LOG.warn("cannot resolve configuration file path: {}", p, e);
                    }
                } else {
                    LOG.warn("configuration file not found: {}", p);
                }
            }
        }
        return configuration;
    }
}
