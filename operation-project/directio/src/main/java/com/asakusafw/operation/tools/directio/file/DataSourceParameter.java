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
package com.asakusafw.operation.tools.directio.file;

import static com.asakusafw.operation.tools.directio.file.Util.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.BasePath;
import com.asakusafw.operation.tools.directio.DataSourceInfo;
import com.asakusafw.operation.tools.directio.DirectIoPath;
import com.asakusafw.operation.tools.directio.common.ConfigurationParameter;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceCore;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

/**
 * Handles parameters about Direct I/O data source.
 * @since 0.10.0
 */
public class DataSourceParameter {

    static final Logger LOG = LoggerFactory.getLogger(DataSourceParameter.class);

    @ParametersDelegate
    final ConfigurationParameter configurationParameter = new ConfigurationParameter();

    private DirectDataSourceRepository repository;

    @Parameter(
            names = { "-s", "--data-source", },
            description = "Data source ID instead of inferring from path.",
            required = false
    )
    String id;

    private List<IdAndPath> declared;

    private DataSourceInfo resolved;

    /**
     * Returns the Hadoop configuration.
     * @return the Hadoop configuration
     */
    public Configuration getConfiguration() {
        return configurationParameter.getConfiguration();
    }

    /**
     * Returns the repository.
     * @return the repository
     */
    public DirectDataSourceRepository getRepository() {
        if (repository == null) {
            LOG.debug("loading data source repository");
            this.repository = HadoopDataSourceUtil.loadRepository(getConfiguration());
        }
        return repository;
    }

    /**
     * Returns the data source information.
     * @return the data source information, or {@code empty} if it is not specified.
     */
    public Optional<DataSourceInfo> getDataSourceInfo() {
        if (id == null) {
            return Optional.empty();
        }
        if (resolved == null) {
            resolved = resolve();
        }
        return Optional.of(resolved);
    }

    /**
     * Returns the data source information for the path.
     * @param path the target path
     * @return the data source information
     */
    public DataSourceInfo getDataSourceInfo(BasePath path) {
        DataSourceInfo info = getDataSourceInfo().orElse(null);
        if (info != null) {
            if (info.getPath().isPrefixOf(path) == false) {
                throw new CommandConfigurationException(MessageFormat.format(
                        "Direct I/O data source \"{0}\" (on \"{1}\") cannot accept the path \"{2}\"",
                        info.getId(),
                        info.getPath(),
                        path));
            }
            return info;
        } else {
            DirectDataSourceRepository repo = getRepository();
            try {
                String container = repo.getContainerPath(path.toString());
                String dsId = repo.getRelatedId(container);
                DirectDataSource source = repo.getRelatedDataSource(container);
                return new DataSourceInfo(dsId, source, BasePath.of(container));
            } catch (IOException | InterruptedException e) {
                throw new CommandConfigurationException(MessageFormat.format(
                        "there is no available Direct I/O data source for the path: {0}",
                        path), e);
            }
        }
    }

    /**
     * Returns the all available Direct I/O data sources.
     * @return the data sources
     */
    public List<DataSourceInfo> getAllDataSourceInfo() {
        DirectDataSourceRepository repo = getRepository();
        return getDeclared().stream()
                .flatMap(pair -> {
                    try {
                        DirectDataSource source = repo.getRelatedDataSource(pair.path);
                        return Stream.of(new DataSourceInfo(pair.id, source, BasePath.of(pair.path)));
                    } catch (IOException | InterruptedException e) {
                        LOG.warn("error occurred while loading Direct I/O data source: {}", pair.id, e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Resolves the given path.
     * @param path the path string
     * @return the corresponded Direct I/O path
     */
    public DirectIoPath resolve(String path) {
        LOG.debug("resolving path: {}", path);

        BasePath basePath = BasePath.headOf(path);
        LOG.debug("base path candidate: {}", basePath);

        DataSourceInfo info = getDataSourceInfo(basePath);
        LOG.debug("data source: {} ({})", info.getId(), info.getPath());

        return new DirectIoPath(info, basePath, basePath.restOf(path).orElse(null));
    }

    /**
     * Resolves the given path as Hadoop file system path.
     * @param path the Direct I/O path string
     * @return the corresponded Hadoop file system path
     */
    public org.apache.hadoop.fs.Path resolveAsHadoopPath(String path) {
        DirectIoPath result = resolve(path);
        if (result.getSource().getEntity().findProperty(HadoopDataSourceCore.class).isPresent() == false) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "unsupported target path \"{0}\" (type: {1}): {2}",
                    result.getSource().getId(),
                    result.getSource().getEntity().getClass().getName(),
                    result));
        }
        if (result.getResourcePattern().isPresent()) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "target path must not contain any meta-characters: {0}",
                    result));
        }
        return asHadoopPath(result.getBarePath());
    }

    /**
     * Returns the Hadoop file system for the path.
     * @param path the target path
     * @return the corresponded file system object
     */
    public org.apache.hadoop.fs.FileSystem getHadoopFileSystem(org.apache.hadoop.fs.Path path) {
        try {
            return path.getFileSystem(getConfiguration());
        } catch (IOException e) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "error occurred while resolving Hadoop path: {0} ({1})",
                    path,
                    Optional.ofNullable(e.getMessage()).orElseGet(() -> e.toString())), e);
        }
    }

    private List<IdAndPath> getDeclared() {
        DirectDataSourceRepository repo = getRepository();
        if (declared == null) {
            try {
                declared = repo.getContainerPaths().stream()
                        .flatMap(path -> {
                            try {
                                return Stream.of(new IdAndPath(repo.getRelatedId(path), path));
                            } catch (IOException e) {
                                LOG.warn("error occurred while resolving data source: {}", path, e);
                            }
                            return Stream.empty();
                        })
                        .collect(Collectors.toList());
            } catch (IOException | InterruptedException e) {
                throw new CommandConfigurationException(
                        "error occurred while collecting Direct I/O data sources", e);
            }
        }
        return declared;
    }

    private DataSourceInfo resolve() {
        String path = getDeclared().stream()
                .filter(it -> it.id.equals(id))
                .map(it -> it.path)
                .findFirst()
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        "Direct I/O data source \"{0}\" is not defined (available IDs: {1})",
                        id,
                        getDeclared().stream()
                                .map(it -> it.id)
                                .sorted()
                                .collect(Collectors.joining(", ")))));

        DirectDataSourceRepository repo = getRepository();
        try {
            DirectDataSource source = repo.getRelatedDataSource(path);
            return new DataSourceInfo(id, source, BasePath.of(path));
        } catch (IOException | InterruptedException e) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "error occurred while loading Direct I/O data source \"{0}\"",
                    id), e);
        }
    }

    private static class IdAndPath {

        final String id;

        final String path;

        IdAndPath(String id, String path) {
            this.id = id;
            this.path = path;
        }
    }
}
