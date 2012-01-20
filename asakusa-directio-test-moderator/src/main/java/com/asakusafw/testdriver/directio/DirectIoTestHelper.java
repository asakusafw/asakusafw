/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.directio;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.SearchPattern;
import com.asakusafw.runtime.directio.SearchPattern.PatternElement;
import com.asakusafw.runtime.directio.SearchPattern.Segment;
import com.asakusafw.runtime.directio.SearchPattern.Selection;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.flow.RuntimeResourceManager;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;

/**
 * Utilities for this package.
 * @since 0.2.5
 */
public final class DirectIoTestHelper {

    private static final SearchPattern ALL = SearchPattern.compile("**");

    private static final String WILDCARD_REPLACEMENT = "__testing__";

    static final Logger LOG = LoggerFactory.getLogger(DirectIoTestHelper.class);

    private static final WeakHashMap<TestContext, DirectDataSourceRepository> REPOSITORY_CACHE =
        new WeakHashMap<TestContext, DirectDataSourceRepository>();

    final VariableTable variables;

    final DirectDataSource dataSource;

    final String id;

    final String basePath;

    /**
     * Creates a new instance.
     * @param context current test context
     * @param rootPath the bare original base path
     * @throws IOException if failed to create a new instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectIoTestHelper(TestContext context, String rootPath) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (rootPath == null) {
            throw new IllegalArgumentException("rootPath must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating test helper for directio: {}", rootPath);
        this.variables = new VariableTable(RedefineStrategy.ERROR);
        variables.defineVariables(context.getArguments());
        String resolvedRootPath = resolve(rootPath);
        LOG.debug("Resolved base path: {} -> {}", rootPath, resolvedRootPath);
        DirectDataSourceRepository repo = getRepository(context);
        try {
            this.id = repo.getRelatedId(resolvedRootPath);
            this.dataSource = repo.getRelatedDataSource(resolvedRootPath);
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to initialize Direct I/O for \"{0}\", please check configuration ({1})",
                    resolvedRootPath,
                    findExtraConfiguration(context)), e);
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException("interrupted").initCause(e);
        }
        this.basePath = repo.getComponentPath(resolvedRootPath);
        LOG.debug("Direct IO Mapping: {} -> {}", resolvedRootPath, id);
    }

    private synchronized DirectDataSourceRepository getRepository(TestContext context) throws IOException {
        assert context != null;
        DirectDataSourceRepository cached = REPOSITORY_CACHE.get(context);
        if (cached != null) {
            return cached;
        }
        DirectDataSourceRepository repo = createRepository(context);
        REPOSITORY_CACHE.put(context, repo);
        return repo;
    }

    private DirectDataSourceRepository createRepository(TestContext context) throws IOException {
        assert context != null;
        Configuration conf;
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            conf = ConfigurationFactory.getDefault().newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
        URL extra = findExtraConfiguration(context);
        if (extra != null) {
            conf.addResource(extra);
        }
        return HadoopDataSourceUtil.loadRepository(conf);
    }

    private URL findExtraConfiguration(TestContext context) throws IOException {
        assert context != null;
        File file = findFileOnHomePath(context, RuntimeResourceManager.CONFIGURATION_FILE_PATH);
        if (file == null) {
            throw new IOException(MessageFormat.format(
                    "Direct I/O configuration is not set: {0}/{1}",
                    "$ASAKUSA_HOME",
                    RuntimeResourceManager.CONFIGURATION_FILE_PATH));
        }
        return file.toURI().toURL();
    }

    private static File findFileOnHomePath(TestContext context, String path) {
        assert context != null;
        assert path != null;
        String home = context.getEnvironmentVariables().get("ASAKUSA_HOME");
        if (home != null) {
            File file = new File(home, path);
            if (file.exists()) {
                return file;
            }
        } else {
            LOG.warn("ASAKUSA_HOME is not defined");
        }
        return null;
    }

    /**
     * Truncates the current target (deletes all on base path).
     * @throws IOException if failed to perform by I/O error
     */
    public void truncate() throws IOException {
        LOG.info("Truncating : {}:{}", id, basePath);
        try {
            dataSource.delete(basePath, ALL);
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException("interrupted").initCause(e);
        }
    }

    /**
     * Opens output for the target input.
     * @param <T> data type
     * @param dataType data type
     * @param description target description
     * @return the opened output
     * @throws IOException if failed to perform by I/O error
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> ModelOutput<T> openOutput(
            Class<T> dataType,
            DirectFileInputDescription description) throws IOException {
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        final OutputAttemptContext context = createContext();
        DataFormat<T> format = createFormat(dataType, description.getFormat());
        String outputPath = toOutputName(description.getResourcePattern());
        LOG.info("Opening direct output: {}:{}/{}", new Object[] {
                id,
                basePath,
                outputPath,
        });
        try {
            dataSource.setupTransactionOutput(context.getTransactionContext());
            dataSource.setupAttemptOutput(context);
            Counter counter = new Counter();
            final ModelOutput<T> output =
                dataSource.openOutput(context, dataType, format, basePath, outputPath, counter);
            return new ModelOutput<T>() {
                @Override
                public void write(T model) throws IOException {
                    output.write(model);
                }
                @Override
                public void close() throws IOException {
                    output.close();
                    try {
                        dataSource.commitAttemptOutput(context);
                        dataSource.commitTransactionOutput(context.getTransactionContext());
                    } catch (InterruptedException e) {
                        throw (IOException) new InterruptedIOException("interrupted").initCause(e);
                    }
                }
            };
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException("interrupted").initCause(e);
        }
    }

    /**
     * Opens input for the target output.
     * @param <T> data type
     * @param dataType data type
     * @param description target description
     * @return the opened output
     * @throws IOException if failed to perform by I/O error
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> ModelInput<T> openInput(
            final Class<T> dataType,
            DirectFileOutputDescription description) throws IOException {
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        final DataFormat<T> format = createFormat(dataType, description.getFormat());
        final Counter counter = new Counter();
        try {
            SearchPattern pattern = toInputPattern(description.getResourcePattern());
            LOG.info("Opening direct input: {}:{}/{}", new Object[] {
                    id,
                    basePath,
                    pattern
            });
            final List<DirectInputFragment> fragments =
                dataSource.findInputFragments(dataType, format, basePath, pattern);
            return new ModelInput<T>() {

                private final Iterator<DirectInputFragment> iterator = fragments.iterator();

                private ModelInput<T> current = null;

                @Override
                public boolean readTo(T model) throws IOException {
                    while (true) {
                        if (current == null) {
                            if (iterator.hasNext() == false) {
                                return false;
                            }
                            DirectInputFragment fragment = iterator.next();
                            try {
                                current = dataSource.openInput(dataType, format, fragment, counter);
                            } catch (InterruptedException e) {
                                throw (IOException) new InterruptedIOException("interrupted").initCause(e);
                            }
                        }
                        assert current != null;
                        if (current.readTo(model)) {
                            return true;
                        }
                        current.close();
                        current = null;
                    }
                }

                @Override
                public void close() throws IOException {
                    if (current != null) {
                        current.close();
                    }
                }
            };
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException("interrupted").initCause(e);
        }
    }

    private String toOutputName(String inputResourcePattern) throws IOException {
        assert inputResourcePattern != null;
        String patternString = resolve(inputResourcePattern);
        SearchPattern pattern = SearchPattern.compile(patternString);
        if (pattern.containsVariables()) {
            throw new IOException(MessageFormat.format(
                    "Input resource pattern contains variables (original=[{0}], expanded=[{1}])",
                    inputResourcePattern,
                    patternString));
        }
        StringBuilder buf = new StringBuilder();
        for (Segment segment : pattern.getSegments()) {
            if (buf.length() != 0) {
                buf.append('/');
            }
            if (segment.isTraverse()) {
                buf.append(WILDCARD_REPLACEMENT);
            }
            for (PatternElement element : segment.getElements()) {
                switch (element.getKind()) {
                case TOKEN:
                    buf.append(element.getToken());
                    break;
                case SELECTION:
                    buf.append(((Selection) element).getContents().get(0));
                    break;
                default:
                    buf.append(WILDCARD_REPLACEMENT);
                    break;
                }
            }
        }
        return buf.toString();
    }

    private SearchPattern toInputPattern(String outputResourcePattern) {
        assert outputResourcePattern != null;
        // FIXME more specific
        return ALL;
    }

    private String resolve(String string) {
        assert string != null;
        return variables.parse(string);
    }

    private OutputAttemptContext createContext() {
        String uuid = UUID.randomUUID().toString();
        return new OutputAttemptContext(uuid, uuid, uuid);
    }

    @SuppressWarnings("unchecked")
    private <T> DataFormat<T> createFormat(
            Class<T> dataType,
            Class<? extends DataFormat<?>> formatClass) throws IOException {
        assert dataType != null;
        assert formatClass != null;
        DataFormat<?> format;
        try {
            format = formatClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to create data format: {0}",
                    formatClass.getName()), e);
        }
        if (format.getSupportedType().isAssignableFrom(dataType) == false) {
            throw new IOException(MessageFormat.format(
                    "The data format does not support {1}: {0}",
                    formatClass.getName(),
                    dataType.getName()));
        }
        return (DataFormat<T>) format;
    }
}
