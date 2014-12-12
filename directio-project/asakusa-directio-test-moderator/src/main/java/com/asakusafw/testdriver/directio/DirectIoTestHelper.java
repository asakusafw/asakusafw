/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.FilePattern.PatternElement;
import com.asakusafw.runtime.directio.FilePattern.Segment;
import com.asakusafw.runtime.directio.FilePattern.Selection;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.SimpleDataDefinition;
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
 * @version 0.7.0
 */
public final class DirectIoTestHelper {

    private static final FilePattern ALL = FilePattern.compile("**");

    private static final String WILDCARD_REPLACEMENT = "__testing__";

    private static final String ENV_FRAMEWORK_HOME = "ASAKUSA_HOME";

    static final Logger LOG = LoggerFactory.getLogger(DirectIoTestHelper.class);

    private static final WeakHashMap<TestContext, DirectDataSourceRepository> REPOSITORY_CACHE =
        new WeakHashMap<TestContext, DirectDataSourceRepository>();

    private final TestContext context;

    private final Configuration hadoopConfiguration;

    final VariableTable variables;

    final DirectDataSource dataSource;

    final String id;

    final String fullPath;

    final String containerPath;

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
        this.context = context;
        this.hadoopConfiguration = createConfiguration();
        LOG.debug("Creating test helper for Direct I/O (basePath={})", rootPath);
        this.variables = new VariableTable(RedefineStrategy.ERROR);
        variables.defineVariables(context.getArguments());
        String resolvedRootPath = resolve(rootPath);
        LOG.debug("Resolved base path: {} -> {}", rootPath, resolvedRootPath);
        DirectDataSourceRepository repo = getRepository();
        try {
            this.id = repo.getRelatedId(resolvedRootPath);
            this.dataSource = repo.getRelatedDataSource(resolvedRootPath);
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to initialize Direct I/O for \"{0}\", please check configuration ({1})",
                    resolvedRootPath,
                    findExtraConfiguration()), e);
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException("interrupted").initCause(e);
        }
        this.fullPath = resolvedRootPath;
        this.containerPath = repo.getContainerPath(resolvedRootPath);
        this.basePath = repo.getComponentPath(resolvedRootPath);
        LOG.debug("Direct I/O Mapping: {} -> id={}", resolvedRootPath, id);
    }

    private synchronized DirectDataSourceRepository getRepository() {
        assert context != null;
        DirectDataSourceRepository cached = REPOSITORY_CACHE.get(context);
        if (cached != null) {
            return cached;
        }
        DirectDataSourceRepository repo = createRepository();
        REPOSITORY_CACHE.put(context, repo);
        return repo;
    }

    private Configuration createConfiguration() throws IOException {
        Configuration conf;
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            conf = ConfigurationFactory.getDefault().newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
        URL extra = findExtraConfiguration();
        if (extra != null) {
            conf.addResource(extra);
        }
        return conf;
    }

    private DirectDataSourceRepository createRepository() {
        return HadoopDataSourceUtil.loadRepository(hadoopConfiguration);
    }

    private URL findExtraConfiguration() throws IOException {
        File home = getHomePath(context);
        if (home == null) {
            throw new IOException(MessageFormat.format(
                    "Environment variable \"{0}\" is not set",
                    ENV_FRAMEWORK_HOME));
        } else if (home.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "Asakusa Framework is not installed: {0}",
                    home));
        }
        File file = new File(home, RuntimeResourceManager.CONFIGURATION_FILE_PATH);
        if (file.exists() == false) {
            throw new IOException(MessageFormat.format(
                    "Direct I/O configuration file is not found: {0}",
                    file));
        }
        return file.toURI().toURL();
    }

    private static File getHomePath(TestContext context) {
        String home = context.getEnvironmentVariables().get(ENV_FRAMEWORK_HOME);
        if (home == null || home.trim().isEmpty()) {
            return null;
        }
        File file = new File(home);
        return file;
    }

    /**
     * Truncates the current target (deletes all on base path).
     * @throws IOException if failed to perform by I/O error
     */
    public void truncate() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Truncating Direct I/O resources: {0} (id={1})",
                    fullPath,
                    id));
        }
        try {
            dataSource.delete(basePath, ALL, true, new Counter());
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
        final OutputAttemptContext outputContext = createOutputContext();
        DataFormat<T> format = createFormat(dataType, description.getFormat());
        String outputPath = toOutputName(description.getResourcePattern());
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Opening {0}/{1} for output (id={2}, description={3})",
                    fullPath,
                    outputPath,
                    id,
                    description.getClass().getName()));
        }
        DataDefinition<T> definition = SimpleDataDefinition.newInstance(dataType, format);
        try {
            dataSource.setupTransactionOutput(outputContext.getTransactionContext());
            dataSource.setupAttemptOutput(outputContext);
            Counter counter = new Counter();
            final ModelOutput<T> output = dataSource.openOutput(
                    outputContext, definition, basePath, outputPath, counter);
            return new ModelOutput<T>() {
                @Override
                public void write(T model) throws IOException {
                    output.write(model);
                }
                @Override
                public void close() throws IOException {
                    output.close();
                    try {
                        dataSource.commitAttemptOutput(outputContext);
                        dataSource.cleanupAttemptOutput(outputContext);
                        dataSource.commitTransactionOutput(outputContext.getTransactionContext());
                        dataSource.cleanupTransactionOutput(outputContext.getTransactionContext());
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
            Class<T> dataType,
            DirectFileOutputDescription description) throws IOException {
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        DataFormat<T> format = createFormat(dataType, description.getFormat());
        final DataDefinition<T> definition = SimpleDataDefinition.newInstance(dataType, format);
        final Counter counter = new Counter();
        try {
            FilePattern pattern = toInputPattern(description.getResourcePattern());
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Opening {0}/{1} for input (id={2}, description={3})",
                        fullPath,
                        pattern,
                        id,
                        description.getClass().getName()));
            }
            final List<DirectInputFragment> fragments = dataSource.findInputFragments(definition, basePath, pattern);
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
                                current = dataSource.openInput(definition, fragment, counter);
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
        FilePattern pattern = FilePattern.compile(patternString);
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

    private FilePattern toInputPattern(String outputResourcePattern) {
        assert outputResourcePattern != null;
        // FIXME more specific
        return ALL;
    }

    private String resolve(String string) {
        assert string != null;
        return variables.parse(string);
    }

    private OutputAttemptContext createOutputContext() {
        String tx = UUID.randomUUID().toString();
        String attempt = UUID.randomUUID().toString();
        return new OutputAttemptContext(tx, attempt, id, new Counter());
    }

    @SuppressWarnings("unchecked")
    private <T> DataFormat<T> createFormat(
            Class<T> dataType,
            Class<? extends DataFormat<?>> formatClass) throws IOException {
        assert dataType != null;
        assert formatClass != null;
        DataFormat<?> format;
        try {
            format = ReflectionUtils.newInstance(formatClass, hadoopConfiguration);
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
