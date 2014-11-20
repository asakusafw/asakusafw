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
package com.asakusafw.dmdl.util;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.analyzer.DmdlAnalyzer;
import com.asakusafw.dmdl.analyzer.DmdlAnalyzerEnhancer;
import com.asakusafw.dmdl.analyzer.DmdlSemanticException;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstScript;
import com.asakusafw.dmdl.parser.DmdlParser;
import com.asakusafw.dmdl.parser.DmdlSyntaxException;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceRepository.Cursor;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;

/**
 * Analyzes DMDL models from input DMDL scripts.
 * @since 0.2.0
 * @version 0.7.1
 */
public class AnalyzeTask {

    static final Logger LOG = LoggerFactory.getLogger(AnalyzeTask.class);

    private final String processName;

    private final DmdlAnalyzerEnhancer analyzerEnhancer;

    private final ClassLoader serviceClassLoader;

    /**
     * Creates and returns a new instance.
     * @param processName the parent process name
     * @param serviceClassLoader class loader to load the plug-ins
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AnalyzeTask(String processName, ClassLoader serviceClassLoader) {
        this(processName, DmdlAnalyzerEnhancer.NULL, serviceClassLoader);
    }

    /**
     * Creates and returns a new instance.
     * @param processName the parent process name
     * @param analyzerEnhancer enhances behavior of {@link DmdlAnalyzer}
     * @param serviceClassLoader class loader to load the plug-ins
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public AnalyzeTask(String processName, DmdlAnalyzerEnhancer analyzerEnhancer, ClassLoader serviceClassLoader) {
        if (processName == null) {
            throw new IllegalArgumentException("processName must not be null"); //$NON-NLS-1$
        }
        if (analyzerEnhancer == null) {
            throw new IllegalArgumentException("analyzerEnhancer must not be null"); //$NON-NLS-1$
        }
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.processName = processName;
        this.analyzerEnhancer = analyzerEnhancer;
        this.serviceClassLoader = serviceClassLoader;
    }

    /**
     * Analyzes all models from source repository in the current configuration.
     * @param repository the source repository
     * @return the analyzed model
     * @throws IOException if failed to process DMDL scripts
     */
    public DmdlSemantics process(DmdlSourceRepository repository) throws IOException {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null"); //$NON-NLS-1$
        }
        DmdlAnalyzer analyzer = parse(repository);
        try {
            LOG.debug(Messages.getString("AnalyzeTask.monitorResolveStarting")); //$NON-NLS-1$
            return analyzer.resolve();
        } catch (DmdlSemanticException e) {
            LOG.error(Messages.getString("AnalyzeTask.monitorResolveFailed"), e); //$NON-NLS-1$
            for (Diagnostic diagnostic : e.getDiagnostics()) {
                switch (diagnostic.level) {
                case INFO:
                    LOG.info("{} ({})", diagnostic.message, diagnostic.region); //$NON-NLS-1$
                    break;
                case WARN:
                    LOG.warn("{} ({})", diagnostic.message, diagnostic.region); //$NON-NLS-1$
                    break;
                case ERROR:
                    LOG.error("{} ({})", diagnostic.message, diagnostic.region); //$NON-NLS-1$
                    break;
                default:
                    LOG.warn(Messages.getString("AnalyzeTask.monitorUnknownDiagnostic"), diagnostic); //$NON-NLS-1$
                    break;
                }
            }
            throw new IOException(MessageFormat.format(
                    Messages.getString("AnalyzeTask.errorResolve"), //$NON-NLS-1$
                    processName));
        }
    }

    private DmdlAnalyzer parse(DmdlSourceRepository source) throws IOException {
        assert source != null;
        boolean green = true;
        DmdlParser parser = new DmdlParser();
        DmdlAnalyzer analyzer = new DmdlAnalyzer(
                analyzerEnhancer,
                ServiceLoader.load(TypeDriver.class, serviceClassLoader),
                ServiceLoader.load(AttributeDriver.class, serviceClassLoader));
        int count = 0;
        Cursor cursor = source.createCursor();
        try {
            while (cursor.next()) {
                URI name = cursor.getIdentifier();
                LOG.info(Messages.getString("AnalyzeTask.monitorParseStarting"), name); //$NON-NLS-1$
                Reader resource = cursor.openResource();
                try {
                    AstScript script = parser.parse(resource, name);
                    for (AstModelDefinition<?> model : script.models) {
                        LOG.debug(Messages.getString("AnalyzeTask.monitorFoundModel"), model.name); //$NON-NLS-1$
                        analyzer.addModel(model);
                        count++;
                    }
                } catch (DmdlSyntaxException e) {
                    LOG.error(MessageFormat.format(
                            Messages.getString("AnalyzeTask.monitorParseFailed"), //$NON-NLS-1$
                            name), e);
                    green = false;
                } finally {
                    resource.close();
                }
            }
            LOG.debug(Messages.getString("AnalyzeTask.monitorCountModel"), count); //$NON-NLS-1$
        } finally {
            cursor.close();
        }
        if (green == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("AnalyzeTask.errorParse"), //$NON-NLS-1$
                    processName));
        }
        if (count == 0) {
            throw new IOException(Messages.getString("AnalyzeTask.errorMissingModels")); //$NON-NLS-1$
        }
        return analyzer;
    }
}
